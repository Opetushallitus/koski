package fi.oph.koski.valpas.massaluovutus.eioppivelvollisuuttasuorittavat

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter, ValpasMassaluovutusQueryParameters}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.valpas.massaluovutus.{ValpasMassaluovutusOppija, ValpasMassaluovutusResult}
import fi.oph.koski.valpas.oppija.ValpasAccessResolver
import fi.oph.koski.valpas.rouhinta.ValpasKuntarouhintaService
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.scalaschema.annotation.{Description, Title}

@Title("Kunnan ei-oppivelvollisuutta suorittavat oppijat")
@Description("Palauttaa kunnan oppijat, jotka eivät suorita oppivelvollisuutta.")
case class ValpasEiOppivelvollisuuttaSuorittavatQuery(
  @EnumValues(Set("eiSuoritaOppivelvollisuutta"))
  `type`: String = "eiSuoritaOppivelvollisuutta",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Kunnan organisaatio-oid")
  kuntaOid: String,
) extends ValpasMassaluovutusQueryParameters {

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = {
    val kuntarouhinta = new ValpasKuntarouhintaService(application)

    kuntarouhinta
      .haeKunnanPerusteellaIlmanOikeustarkastusta(kuntaOid)
      .left.map(_.errorString.getOrElse("Tuntematon virhe"))
      .map { tulos =>
        val oppijat = tulos.eiOppivelvollisuuttaSuorittavat.map(ValpasMassaluovutusOppija.apply)
        val result = ValpasMassaluovutusResult(oppijat)
        writer.putJson("result", result)
      }
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = user match {
    case session: ValpasSession =>
      val accessResolver = new ValpasAccessResolver
      accessResolver.accessToKuntaOrg(kuntaOid)(session)
    case _ => false
  }
}
