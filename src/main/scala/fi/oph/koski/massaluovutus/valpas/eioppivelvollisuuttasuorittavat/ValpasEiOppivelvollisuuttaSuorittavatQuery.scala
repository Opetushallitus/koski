package fi.oph.koski.massaluovutus.valpas.eioppivelvollisuuttasuorittavat

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.log.Logging
import fi.oph.koski.massaluovutus.valpas.ValpasMassaluovutusQueryParameters
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.massaluovutus.{ValpasMassaluovutusOppija, ValpasMassaluovutusResult}
import fi.oph.koski.valpas.oppija.ValpasAccessResolver
import fi.oph.koski.valpas.rouhinta.ValpasKuntarouhintaService
import fi.oph.koski.valpas.valpasuser.ValpasSession
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
) extends ValpasMassaluovutusQueryParameters with Logging {

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = user match {
    case valpasUser: ValpasSession =>
      implicit val session: ValpasSession = valpasUser
      val kuntarouhinta = new ValpasKuntarouhintaService(application)
      val kunta = getKuntaKoodiByKuntaOid(application, kuntaOid)
        .getOrElse(throw new IllegalArgumentException(s"ValpasEiOppivelvollisuuttaSuorittavatQuery: getKuntaKoodiByKuntaOid palautti None kuntaOid:lla $kuntaOid"))

      if(vainAktiivisetKuntailmoitukset) {
        kuntarouhinta.rouhiOppivelvollisuuttaSuorittamattomatKoskesta(kunta)
          .left.map(_.errorString.getOrElse("Tuntematon virhe"))
          .map { tulos =>
            val oppijat = tulos.filter(_.aktiivinenKuntailmoitus.nonEmpty).map(ValpasMassaluovutusOppija.apply)
            // Rikastetaan oppijat maksuttomuustiedoilla
            val enrichedOppijat = withOikeusMaksuttomuuteen(oppijat, application)
            val oppijaOids = enrichedOppijat.map(_.oppijanumero)
            ValpasAuditLog.auditLogMassaluovutusKunnalla(kunta, oppijaOids)
            val result = ValpasMassaluovutusResult(enrichedOppijat)
            writer.putJson("result", result)
          }
      } else {
        kuntarouhinta
          .haeKunnanPerusteellaIlmanOikeustarkastusta(kunta)
          .left.map(_.errorString.getOrElse("Tuntematon virhe"))
          .map { tulos =>
            val oppijat = tulos.eiOppivelvollisuuttaSuorittavat.map(ValpasMassaluovutusOppija.apply)
            // Rikastetaan oppijat maksuttomuustiedoilla
            val enrichedOppijat = withOikeusMaksuttomuuteen(oppijat, application)
            val oppijaOids = enrichedOppijat.map(_.oppijanumero)
            ValpasAuditLog.auditLogMassaluovutusKunnalla(kunta, oppijaOids)
            val result = ValpasMassaluovutusResult(enrichedOppijat)
            writer.putJson("result", result)
          }
      }
    case _ =>
      throw new IllegalArgumentException("ValpasSession required")
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = user match {
    case session: ValpasSession =>
      val kuntaOpt = getKuntaKoodiByKuntaOid(application, kuntaOid)
      kuntaOpt.exists(kunta =>{
        val accessResolver = new ValpasAccessResolver
        accessResolver.accessToKuntaOrg(kunta)(session)
      })
    case _ => false
  }

  private def getKuntaKoodiByKuntaOid(application: KoskiApplication, kuntaOid: String): Option[String] = {
    Kunta.validateAndGetKuntaKoodi(application.organisaatioService, application.koodistoPalvelu, kuntaOid) match {
      case Right(kuntaKoodi) => Some(kuntaKoodi)
      case Left(error) =>
        logger.warn(s"ValpasEiOppivelvollisuuttaSuorittavatQuery getKuntaKoodiByKuntaOid: ${error.errors.map(_.toString).mkString(", ")}")
        None
    }
  }
}
