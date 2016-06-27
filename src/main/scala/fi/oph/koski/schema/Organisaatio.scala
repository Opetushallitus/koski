package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, MinValue, RegularExpression}

@Description("Organisaatio. Voi olla Opintopolun organisaatiosta löytyvä oid:illinen organisaatio, y-tunnuksellinen yritys tai tutkintotoimikunta.")
sealed trait Organisaatio

object Organisaatio {
  type Oid = String
}

@Description("Opintopolun organisaatiopalvelusta löytyvä organisaatio. Esimerkiksi koulutustoimijat, oppilaitokset ja toimipisteet ovat tällaisia organisaatioita.")
case class OidOrganisaatio(
  oid: Organisaatio.Oid,
  nimi: Option[LocalizedString] = None
) extends OrganisaatioWithOid {
  def toOppilaitos = None
}

@Description("Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio.")
case class Oppilaitos(
   oid: String,
   @Description("5-numeroinen oppilaitosnumero, esimerkiksi 00001")
   @ReadOnly("Tiedon syötössä oppilaitosnumeroa ei tarvita; numero haetaan Organisaatiopalvelusta")
   @KoodistoUri("oppilaitosnumero")
   oppilaitosnumero: Option[Koodistokoodiviite] = None,
   nimi: Option[LocalizedString] = None
) extends OrganisaatioWithOid {
  def toOppilaitos = Some(this)
}

@Description("Yritys, jolla on y-tunnus")
case class Yritys(
  nimi: LocalizedString,
  @RegularExpression("\\d{7}-\\d")
  yTunnus: String
) extends Organisaatio

@Description("Tutkintotoimikunta")
case class Tutkintotoimikunta(
  nimi: LocalizedString,
  @MinValue(1)
  tutkintotoimikunnanNumero: Int
) extends Organisaatio

trait OrganisaatioWithOid extends Organisaatio {
  @Description("Organisaation tunniste Opintopolku-palvelussa")
  @RegularExpression("""1\.2\.246\.562\.10\.\d{11}""")
  def oid: String
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def nimi: Option[LocalizedString]
  def toOppilaitos: Option[Oppilaitos]
}
