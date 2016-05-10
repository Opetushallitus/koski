package fi.oph.tor.schema

import fi.oph.scalaschema.annotation._


object Henkilö {
  type Oid = String
  def withOid(oid: String) = OidHenkilö(oid)
  def apply(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String) = UusiHenkilö(hetu, etunimet, kutsumanimi, sukunimi)
}

@Description("Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö")
sealed trait Henkilö

@Description("Täydet henkilötiedot. Tietoja haettaessa TOR:sta saadaan aina täydet henkilötiedot.")
case class TaydellisetHenkilötiedot(
  @Description("Yksilöivä tunniste (oppijanumero) Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  @RegularExpression("""1\.2\.246\.562\.24\.\d{11}""")
  oid: String,
  hetu: String,
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
}

@Description("Henkilö, jonka oppijanumero ei ole tiedossa. Tietoja syötettäessä luodaan mahdollisesti uusi henkilö Henkilöpalveluun, jolloin henkilölle muodostuu oppijanumero")
case class UusiHenkilö(
  hetu: String,
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String
) extends Henkilö with Henkilötiedot

@Description("Henkilö, jonka oid on tiedossa. Tietoja syötettäessä henkilö haetaan henkilöpalvelusta.")
case class OidHenkilö(
  @Description("Yksilöivä tunniste (oppijanumero) Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  @RegularExpression("""1\.2\.246\.562\.24\.\d{11}""")
  oid: String
) extends HenkilöWithOid

trait Henkilötiedot {
  @Description("Suomalainen henkilötunnus")
  def hetu: String
  @Description("Henkilön kaikki etunimet. Esimerkiksi Sanna Katariina")
  def etunimet:String
  @Description("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\"")
  def kutsumanimi: String
  @Description("Henkilön sukunimi. Henkilön sukunimen etuliite tulee osana sukunimeä")
  def sukunimi: String

  def kokonimi = etunimet + " " + sukunimi
}

trait HenkilöWithOid extends Henkilö {
  def oid: String
}