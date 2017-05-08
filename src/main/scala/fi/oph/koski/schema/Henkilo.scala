package fi.oph.koski.schema

import fi.oph.scalaschema.annotation._

object Henkilö {
  type Oid = String
  type Hetu = String
  def withOid(oid: String) = OidHenkilö(oid)
  def apply(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String) = UusiHenkilö(Some(hetu), etunimet, kutsumanimi, sukunimi)
  def isHenkilöOid(s: String) = s.matches("""1\.2\.246\.562\.24\.\d{11}""")
}

@Description("Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö")
sealed trait Henkilö

object TäydellisetHenkilötiedot {
  def apply(oid: String, etunimet: String, kutsumanimi: String, sukunimi: String): TäydellisetHenkilötiedot =
    TäydellisetHenkilötiedot(oid, None, etunimet, kutsumanimi, sukunimi, None, None)
}

@Description("Täydet henkilötiedot. Tietoja haettaessa Koskesta saadaan aina täydet henkilötiedot.")
case class TäydellisetHenkilötiedot(
  oid: Henkilö.Oid,
  hetu: Option[Henkilö.Hetu],
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String,
  @Description("Opiskelijan äidinkieli")
  @KoodistoUri("kieli")
  äidinkieli: Option[Koodistokoodiviite],
  @Description("Opiskelijan kansalaisuudet")
  @KoodistoUri("maatjavaltiot2")
  kansalaisuus: Option[List[Koodistokoodiviite]]
) extends HenkilöWithOid with Henkilötiedot {
  def vainHenkilötiedot = UusiHenkilö(hetu, etunimet, kutsumanimi, sukunimi)
  def toHenkilötiedotJaOid = HenkilötiedotJaOid(oid, hetu, etunimet, kutsumanimi, sukunimi)
}

@Title("Henkilötiedot ja henkilö-OID")
@IgnoreInAnyOfDeserialization
case class HenkilötiedotJaOid(
  oid: Henkilö.Oid,
  hetu: Option[Henkilö.Hetu],
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String
) extends HenkilöWithOid with Henkilötiedot

@Description("Henkilö, jonka oppijanumero ei ole tiedossa. Tietoja syötettäessä luodaan mahdollisesti uusi henkilö Henkilöpalveluun, jolloin henkilölle muodostuu oppijanumero")
case class UusiHenkilö(
  hetu: Option[String],
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String
) extends Henkilö with Henkilötiedot

@Title("Henkilö-OID")
@Description("Henkilö, jonka oid on tiedossa. Tietoja syötettäessä henkilö haetaan henkilöpalvelusta.")
case class OidHenkilö(
  oid: String
) extends HenkilöWithOid

trait Henkilötiedot extends NimellinenHenkilö {
  @Description("Suomalainen henkilötunnus")
  def hetu: Option[String]
  def hetuStr: String = hetu.getOrElse("")
}

trait NimellinenHenkilö {
  @Description("Henkilön kaikki etunimet. Esimerkiksi Sanna Katariina")
  def etunimet:String
  @Description("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\"")
  def kutsumanimi: String
  @Description("Henkilön sukunimi. Henkilön sukunimen etuliite tulee osana sukunimeä")
  def sukunimi: String
  def kokonimi = etunimet + " " + sukunimi
  def nimitiedot = Nimitiedot(etunimet, kutsumanimi, sukunimi)
}

case class Nimitiedot(etunimet: String, kutsumanimi: String, sukunimi: String) extends NimellinenHenkilö

trait HenkilöWithOid extends Henkilö {
  @Description("Yksilöivä tunniste (oppijanumero) Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  @RegularExpression("""1\.2\.246\.562\.24\.\d{11}""")
  def oid: String
}