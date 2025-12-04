package fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.massaluovutus.{ValpasMassaluovutusOppija, ValpasMassaluovutusResult}
import fi.oph.koski.valpas.rouhinta.{ValpasKuntarouhintaService, ValpasRouhintaOppivelvollinen}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Title}

@Title("Kunnan ei-oppivelvollisuutta suorittavat oppijat")
@Description("Palauttaa kunnan oppijat, jotka eivät suorita oppivelvollisuutta.")
case class ValpasEiOppivelvollisuuttaSuorittavatQuery(
  @EnumValues(Set("eiSuoritaOppivelvollisuutta"))
  `type`: String = "eiSuoritaOppivelvollisuutta",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Kunnan organisaatio-oid")
  kuntaOid: String,
  @Description("Palautetaanko tuloksissa vain sellaiset oppijat, joista on aktiivinen kuntailmoitus")
  @DefaultValue(false)
  vainAktiivisetKuntailmoitukset: Boolean
) extends ValpasMassaluovutusQueryParameters {

  override def run(application: KoskiApplication, writer: QueryResultWriter)
    (implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = withValpasSession { implicit valpasUser =>
    val kuntarouhintaService = new ValpasKuntarouhintaService(application)
    val kuntailmoitusService = application.valpasKuntailmoitusService
    val ovKeskeytysService = application.valpasRouhintaOppivelvollisuudenKeskeytysService
    val kunta = getKuntaKoodiByKuntaOid(application, kuntaOid)
      .getOrElse(throw new IllegalArgumentException(s"ValpasEiOppivelvollisuuttaSuorittavatQuery: getKuntaKoodiByKuntaOid palautti None kuntaOid:lla $kuntaOid"))

    if (vainAktiivisetKuntailmoitukset) {
      val ovSuorittamattomatOppijatAktiivisuustiedoillaJaKeskeytyksillä = for {
        kuntailmoitukset <- kuntailmoitusService.getKuntailmoituksetKunnalleIlmanKäyttöoikeustarkistusta(kuntaOid)
        oppijaOids = kuntailmoitukset.flatMap(_.oppijaOid)
        oppijaTiedot <- application.valpasOppijalistatService.getOppijalistaIlmanOikeustarkastusta(oppijaOids)
        eiOvSuorittavatOppijatJoillaKuntailmoitus = oppijaTiedot.filterNot(_.oppija.suorittaaOppivelvollisuutta)
      } yield {
        val oppijatKuntaIlmoituksilla = eiOvSuorittavatOppijatJoillaKuntailmoitus
          .map(oppijaTieto => kuntailmoitusService.oppijaHakutilanteillaJaAktiivisuustiedoilla(oppijaTieto, kuntailmoitukset))
          .map(ValpasRouhintaOppivelvollinen.apply)

        ovKeskeytysService.fetchOppivelvollisuudenKeskeytykset(
          oppijatKuntaIlmoituksilla
        )
      }

      ovSuorittamattomatOppijatAktiivisuustiedoillaJaKeskeytyksillä
        .left.map(_.errorString.getOrElse("Tuntematon virhe"))
        .map { tulos =>
          val oppijat = tulos.filter(_.aktiivinenKuntailmoitus.nonEmpty).map(ValpasMassaluovutusOppija.apply)
          // Rikastetaan oppijat oppivelvollisuustiedoilla
          val oppijatOppivelvollisuustiedoilla = withOppivelvollisuustiedot(oppijat, application)
          val oppijaOids = oppijatOppivelvollisuustiedoilla.map(_.oppijanumero)
          ValpasAuditLog.auditLogMassaluovutusKunnalla(kunta, oppijaOids)
          val result = ValpasMassaluovutusResult(oppijatOppivelvollisuustiedoilla)
          writer.putJson("result", result)
        }
    } else {
      kuntarouhintaService
        .haeKunnanPerusteellaIlmanOikeustarkastusta(kunta)
        .left.map(_.errorString.getOrElse("Tuntematon virhe"))
        .map { tulos =>
          val oppijat = tulos.eiOppivelvollisuuttaSuorittavat.map(ValpasMassaluovutusOppija.apply)
          // Rikastetaan oppijat oppivelvollisuustiedoilla
          val oppijatOppivelvollisuustiedoilla = withOppivelvollisuustiedot(oppijat, application)
          val oppijaOids = oppijatOppivelvollisuustiedoilla.map(_.oppijanumero)
          ValpasAuditLog.auditLogMassaluovutusKunnalla(kunta, oppijaOids)
          val result = ValpasMassaluovutusResult(oppijatOppivelvollisuustiedoilla)
          writer.putJson("result", result)
        }
    }
  }
}
