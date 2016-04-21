package fi.oph.tor.schema

import fi.oph.tor.localization.LocalizedString
import fi.oph.scalaschema.annotation.{MinValue, ReadOnly, RegularExpression, Description}

@Description("Organisaatio. Voi olla Opintopolun organisaatiosta löytyvä oid:illinen organisaatio, y-tunnuksellinen yritys tai tutkintotoimikunta.")
sealed trait Organisaatio

@Description("Opintopolun organisaatiopalvelusta löytyvä organisaatio. Esimerkiksi koulutustoimijat, oppilaitokset ja toimipisteet ovat tällaisia organisaatioita.")
case class OidOrganisaatio(
  @Description("Organisaation tunniste Opintopolku-palvelussa")
  @RegularExpression("""1\.2\.246\.562\.10\.\d{11}""")
  oid: String,
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  nimi: Option[LocalizedString] = None
) extends OrganisaatioWithOid

@Description("Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio.")
case class Oppilaitos(
   @Description("Organisaation tunniste Opintopolku-palvelussa")
   @RegularExpression("""1\.2\.246\.562\.10\.\d{11}""")
   oid: String,
   @Description("5-numeroinen oppilaitosnumero, esimerkiksi 00001")
   @ReadOnly("Tiedon syötössä oppilaitosnumeroa ei tarvita; numero haetaan Organisaatiopalvelusta")
   @KoodistoUri("oppilaitosnumero")
   oppilaitosnumero: Option[Koodistokoodiviite] = None,
   @Description("Organisaation (kielistetty) nimi")
   @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
   nimi: Option[LocalizedString] = None
) extends OrganisaatioWithOid

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
  def oid: String
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def nimi: Option[LocalizedString]
}
