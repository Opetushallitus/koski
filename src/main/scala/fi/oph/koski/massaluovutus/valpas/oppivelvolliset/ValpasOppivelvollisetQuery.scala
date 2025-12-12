package fi.oph.koski.massaluovutus.valpas.oppivelvolliset

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.util.Futures
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.massaluovutus.{ValpasMassaluovutusOppija, ValpasMassaluovutusResult}
import fi.oph.koski.valpas.rouhinta.{RouhintaOpiskeluoikeus, ValpasKuntarouhintaService, ValpasRouhintaOppivelvollinen}
import fi.oph.scalaschema.annotation.{Description, Title}

import scala.concurrent.Future
import scala.util.control.NonFatal

@Title("Kunnan oppivelvolliset oppijat")
@Description("Palauttaa kunnan oppijat, jotka ovat oppivelvollisuuden piirissä.")
case class ValpasOppivelvollisetQuery(
  @EnumValues(Set("oppivelvolliset"))
  `type`: String = "oppivelvolliset",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Kunnan organisaatio-oid")
  kuntaOid: String
) extends ValpasMassaluovutusQueryParameters with GlobalExecutionContext {

  override def run(application: KoskiApplication, writer: QueryResultWriter)
    (implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = withValpasSession { implicit valpasSession =>
    timed("ValpasOppivelvollisetQuery:run") {
      val oppijalistatService = application.valpasOppijalistatService
      val kuntarouhinta = new ValpasKuntarouhintaService(application)
      val kuntailmoitusService = application.valpasKuntailmoitusService
      val rouhintaOvKeskeytyksetService = application.valpasRouhintaOppivelvollisuudenKeskeytysService
      val kunta = getKuntaKoodiByKuntaOid(application, kuntaOid)
        .getOrElse(throw new IllegalArgumentException(s"ValpasOppivelvollisetQuery: getKuntaKoodiByKuntaOid palautti None kuntaOid:lla $kuntaOid"))

      // Hae oppivelvolliset kotikunnalla ONR:stä, vaikka eivät suorita nyt oppivelvollisuutta
      val onrOppivelvollisetOppijat = Future {
        try {
          kuntarouhinta.rouhiOppivelvollisuuttaSuorittamattomatOppijanumerorekisteristä(kunta)
        } catch {
          case NonFatal(e) =>
            val msg = "Tietoja ei saatu haettua oppijanumerorekisteristä"
            logger.error(e)(msg)
            throw e
        }
      }

      // Hae oppivelvolliset Koskesta kotikunnalla
      val oppijaOids = oppijalistatService.getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta).map(_.masterOid)

      // Hae oppijatiedot ja lisää kuntailmoitus- ja keskeytystiedot
      oppijalistatService
        .getOppijalistaIlmanOikeustarkastusta(oppijaOids)
        .left.map(_.errorString.getOrElse("Tuntematon virhe"))
        .flatMap { oppijat =>
          def oppijanAktiivisetOpiskeluoikeudet(oid: String) = oppijat
            .find(o => o.oppija.henkilö.kaikkiOidit.contains(oid))
            .map(_.oppija.opiskeluoikeudet.filter(_.isOpiskelu).map(RouhintaOpiskeluoikeus.apply))
            .getOrElse(Seq.empty)
            .flatten

          // Täydennä kuntailmoitukset oppijoille
          kuntailmoitusService.withKuntailmoituksetIlmanKäyttöoikeustarkistusta(oppijat)
            .left.map(_.errorString.getOrElse("Tuntematon virhe"))
            .map(_.map(ValpasRouhintaOppivelvollinen.apply))
            // Täydennä keskeytykset oppijoille
            .map(oppijat => rouhintaOvKeskeytyksetService.fetchOppivelvollisuudenKeskeytykset(oppijat))
            // Täydennä aktiiviset oppivelvollisuuteen kelpaavat opiskeluoikeudet oppijoille
            .map(oppijat => oppijat.map(oppija => ValpasMassaluovutusOppija.apply(oppija, oppijanAktiivisetOpiskeluoikeudet(oppija.oppijanumero))))
        }
        .map { oppijat =>
          val onrOppijatResult = Futures.await(onrOppivelvollisetOppijat.map(_.map(ValpasMassaluovutusOppija.apply)))
          val oppijatResult = oppijat ++ onrOppijatResult

          // Rikastetaan oppijat oppivelvollisuustiedoilla
          val oppijatOppivelvollisuustiedoilla = withOppivelvollisuustiedot(oppijatResult, application)

          writer.predictFileCount(oppijatOppivelvollisuustiedoilla.size / sivukoko)

          oppijatOppivelvollisuustiedoilla.grouped(sivukoko).zipWithIndex.foreach { case (oppijatSivu, index) =>
            val oppijaOids = oppijatSivu.map(_.oppijanumero)
            ValpasAuditLog.auditLogMassaluovutusKunnalla(kunta, oppijaOids)
            val result = ValpasMassaluovutusResult(oppijatSivu)
            writer.putJson(s"$index", result)
          }
        }
    }
  }
}
