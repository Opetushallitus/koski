package fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.massaluovutus.{ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult, ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija}
import fi.oph.koski.valpas.rouhinta.{ValpasKuntarouhintaService, ValpasRouhintaOppivelvollinen}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Title}

@Title("Kunnan oppivelvolliset oppijat, jotka eivät suorita oppivelvollisuutta")
@Description("Palauttaa kaikki kunnan oppivelvolliset oppijat, joilla ei ole tällä hetkellä aktiivista oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta KOSKI-tietovarannossa.")
@Description("HUOM! Oppijan asuinkunta voi olla eri kuin oppijan virallinen kotikunta. Tuloksissa eivät näy henkilöt, joilla on turvakielto, tai henkilöt, joista ei ole mitään tietoja tallennettuna Opintopolun palveluihin.")
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
    timed("ValpasEiOppivelvollisuuttaSuorittavatQuery:run") {
      val kuntarouhintaService = new ValpasKuntarouhintaService(application)
      val kuntailmoitusService = application.valpasKuntailmoitusService
      val ovKeskeytysService = application.valpasRouhintaOppivelvollisuudenKeskeytysService
      val kunta = getKuntaKoodiByKuntaOid(application, kuntaOid)
        .getOrElse(throw new IllegalArgumentException(s"ValpasEiOppivelvollisuuttaSuorittavatQuery: getKuntaKoodiByKuntaOid palautti None kuntaOid:lla $kuntaOid"))

      val oppijatOppivelvollisuustiedoilla = if (vainAktiivisetKuntailmoitukset) {
        timed("ValpasEiOppivelvollisuuttaSuorittavatQuery:run:vainAktiivisetKuntailmoitukset=true") {
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
              val oppijat = tulos.filter(_.aktiivinenKuntailmoitus.nonEmpty).map(ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija.apply)
              // Rikastetaan oppijat oppivelvollisuustiedoilla
              withEiOppivelvollisuuttaSuorittavatOppivelvollisuustiedot(oppijat, application)
            }
        }
      } else {
        timed("ValpasEiOppivelvollisuuttaSuorittavatQuery:run:vainAktiivisetKuntailmoitukset=false") {
          kuntarouhintaService
            .haeKunnanPerusteellaIlmanOikeustarkastusta(kunta)
            .left.map(_.errorString.getOrElse("Tuntematon virhe"))
            .map { tulos =>
              val oppijat = tulos.eiOppivelvollisuuttaSuorittavat.map(ValpasMassaluovutusEiOppivelvollisuuttaSuorittavaOppija.apply)
              // Rikastetaan oppijat oppivelvollisuustiedoilla
              withEiOppivelvollisuuttaSuorittavatOppivelvollisuustiedot(oppijat, application)
            }
        }
      }

      oppijatOppivelvollisuustiedoilla.map { oppijat =>
        writer.predictFileCount(oppijat.size / sivukoko)

        oppijat.grouped(sivukoko).zipWithIndex.foreach { case (oppijatSivu, index) =>
          val oppijaOids = oppijatSivu.map(_.oppijanumero)
          ValpasAuditLog.auditLogMassaluovutusKunnalla(kunta, oppijaOids)
          val result = ValpasEiOppivelvollisuuttaSuorittavatMassaluovutusResult(oppijatSivu)
          writer.putJson(s"$index", result)
        }
      }
    }
  }
}

object ValpasEiOppivelvollisuuttaSuorittavatQueryDocumentation {
  def example: ValpasEiOppivelvollisuuttaSuorittavatQuery = ValpasEiOppivelvollisuuttaSuorittavatQuery(
    kuntaOid = MockOrganisaatiot.helsinginKaupunki,
    vainAktiivisetKuntailmoitukset = false
  )
}
