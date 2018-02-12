package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.perustiedot.NimitiedotJaOid
import fi.oph.koski.schema.annotation.{KoodistoUri, OksaUri}
import fi.oph.scalaschema.annotation._

object Henkilö {
  type Oid = String
  type Hetu = String
  def withOid(oid: String) = OidHenkilö(oid)
  def isHenkilöOid(s: String) = s.matches("""1\.2\.246\.562\.24\.\d{11}""")
}

@Description("Henkilötiedot. Syötettäessä vaaditaan joko oppijanumero `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö")
sealed trait Henkilö

object TäydellisetHenkilötiedot {
  def apply(oid: String, etunimet: String, kutsumanimi: String, sukunimi: String): TäydellisetHenkilötiedot =
    TäydellisetHenkilötiedot(oid, None, None, etunimet, kutsumanimi, sukunimi, None, None)
}

@Description("Täydet henkilötiedot. Tietoja haettaessa Koskesta saadaan aina täydet henkilötiedot")
case class TäydellisetHenkilötiedot(
  oid: Henkilö.Oid,
  hetu: Option[Henkilö.Hetu],
  @Description("Henkilön syntymäaika. Muoto YYYY-MM-DD")
  syntymäaika: Option[LocalDate],
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String,
  @Description("Opiskelijan äidinkieli (vrkn mukainen äidinkieli)")
  @KoodistoUri("kieli")
  äidinkieli: Option[Koodistokoodiviite],
  @Description("Opiskelijan kansalaisuudet")
  @KoodistoUri("maatjavaltiot2")
  kansalaisuus: Option[List[Koodistokoodiviite]]
) extends HenkilöWithOid with Henkilötiedot {
  def toHenkilötiedotJaOid = HenkilötiedotJaOid(oid, hetu, etunimet, kutsumanimi, sukunimi)
}

case class TäydellisetHenkilötiedotWithMasterInfo(henkilö: TäydellisetHenkilötiedot, master: Option[TäydellisetHenkilötiedot]) extends HenkilöWithOid with Henkilötiedot {
  def oid = henkilö.oid
  def hetu = henkilö.hetu
  def etunimet: String = henkilö.etunimet
  def kutsumanimi: String = henkilö.kutsumanimi
  def sukunimi: String = henkilö.sukunimi
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

@Description("Henkilö, jonka oppijanumero 'oid' ei ole tiedossa. Tietoja syötettäessä luodaan mahdollisesti uusi henkilö Henkilöpalveluun, jolloin henkilölle muodostuu oppijanumero")
case class UusiHenkilö(
  hetu: String,
  etunimet:String,
  kutsumanimi: String,
  sukunimi: String
) extends Henkilö

@Title("Henkilö-OID")
@Description("Henkilö, jonka oppijanumero 'oid' on tiedossa. Tietoja syötettäessä henkilö haetaan henkilöpalvelusta")
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
  @Description("Oppijanumero 'oid' on oppijan yksilöivä tunniste Opintopolku-palvelussa ja Koskessa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  @RegularExpression("""^1\.2\.246\.562\.24\.\d{11}$""")
  def oid: String
}
