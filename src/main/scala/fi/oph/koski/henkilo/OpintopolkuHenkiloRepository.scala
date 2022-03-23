package fi.oph.koski.henkilo

import java.time.LocalDate
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot.oppivelvollisuudenUlkopuolisetKunnat
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.SyntheticProperty

// Tätä rakennetta käytetään kuvaamaan oppijanumerorekisteristä löytyvää henkilöä sovelluksen sisällä.
// Rajapinnoissa käytetään scheman osana olevia rakenteita (Henkilö, TäydellisetHenkilötiedot).
// Tässä rakenteessa voi siis olla tietoja joita ei haluta välittää ulospäin, ja sitä voi muuttaa.

case class LaajatOppijaHenkilöTiedot(
  oid: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  sukupuoli: Option[String] = None,
  äidinkieli: Option[String] = None,
  kansalaisuus: Option[List[String]] = None,
  modified: Long = 0,
  turvakielto: Boolean = false,
  linkitetytOidit: List[String] = Nil,
  vanhatHetut: List[String] = Nil,
  kotikunta: Option[String] = Some("179"),
  yksilöity: Boolean = true,
  yhteystiedot: Seq[Yhteystiedot] = List.empty
) extends OppijaHenkilö with HenkilönTunnisteet {
  @SyntheticProperty
  def preventSerialization: Nothing = ??? // ensure this class never gets serialized to JSON
  def kaikkiOidit: List[String] = oid :: linkitetytOidit

  override def ilmanHetua: HenkilönTunnisteet = this.copy(hetu = None)

  def laajennetunOppivelvollisuudenUlkopuolinenKunnanPerusteella: Boolean = {
    kotikunta match {
      case Some(k) => oppivelvollisuudenUlkopuolisetKunnat.contains(k)
      case _ => true
    }
  }
}

case class SuppeatOppijaHenkilöTiedot(
  oid: String,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  sukupuoli: Option[String] = None,
  äidinkieli: Option[String] = None,
  kansalaisuus: Option[List[String]] = None,
  modified: Long = 0,
  turvakielto: Boolean = false,
  linkitetytOidit: List[String]
) extends OppijaHenkilö {
  @SyntheticProperty
  def preventSerialization: Nothing = ??? // ensure this class never gets serialized to JSON
  def vanhatHetut: List[String] = Nil

  override def ilmanHetua: HenkilönTunnisteet = this.copy(hetu = None)
}

trait OppijaHenkilö extends HenkilönTunnisteet {
  def oid: String
  def sukunimi: String
  def etunimet: String
  def kutsumanimi: String
  def hetu: Option[String]
  def syntymäaika: Option[LocalDate]
  def sukupuoli: Option[String]
  def äidinkieli: Option[String]
  def kansalaisuus: Option[List[String]]
  def modified: Long
  def turvakielto: Boolean

  def toHenkilötiedotJaOid = HenkilötiedotJaOid(oid, hetu, etunimet, kutsumanimi, sukunimi)
}

case class OppijaHenkilöWithMasterInfo(henkilö: OppijaHenkilö, master: Option[OppijaHenkilö])

trait HenkilönTunnisteet {
  def oid: String
  def hetu: Option[String]
  def linkitetytOidit: List[String]
  def vanhatHetut: List[String]
  def ilmanHetua: HenkilönTunnisteet
}

case class Yhteystiedot(
  @KoodistoUri("yhteystietojenalkupera")
  alkuperä: Koodistokoodiviite,
  @KoodistoUri("yhteystietotyypit")
  tyyppi: Koodistokoodiviite,
  sähköposti: Option[String],
  puhelinnumero: Option[String],
  matkapuhelinnumero: Option[String],
  katuosoite: Option[String],
  kunta: Option[String],
  postinumero: Option[String],
  kaupunki: Option[String],
  maa: Option[String],
)

case class OpintopolkuHenkilöRepository(henkilöt: OpintopolkuHenkilöFacade, koodisto: KoodistoViitePalvelu) extends Logging {
  def withMasterInfo(henkilötiedot: OppijaHenkilö) = OppijaHenkilöWithMasterInfo(henkilötiedot, findMasterHenkilö(henkilötiedot.oid))

  // Tarkistaa vain Oppijanumerorekisterin, ei koskaan luo uutta oppijanumeroa Virta/YTR-datan perusteella
  def findByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot] = {
    Hetu.validFormat(hetu)
      .toOption
      .flatMap(henkilöt.findOppijaByHetu)
  }

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, OppijaHenkilö] =  {
    val validKutsumanimet = henkilö.etunimet.trim
      .replaceAll("\\s+", " ")
      .replaceAll("\\s*-\\s*", "-")
      .split(" ")
      .flatMap(n => n :: n.split("-").toList).toList.distinct

    val kutsumanimi = henkilö.kutsumanimi.flatMap(n => validKutsumanimet.find(_ == n)).getOrElse(validKutsumanimet.head)

    henkilöt
      .findOrCreate(UusiOppijaHenkilö(Some(henkilö.hetu), henkilö.sukunimi, henkilö.etunimet, kutsumanimi))
  }

  def findMasterByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] = {
    henkilöt.findMasterOppija(oid)
  }

  def findMastersByOids(oids: List[String]): List[LaajatOppijaHenkilöTiedot] = {
    henkilöt.findMasterOppijat(oids).values.toList
  }

  def findByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] = {
    henkilöt.findOppijaByOid(oid)
  }

  // Hakee master-henkilön, jos eri kuin tämä henkilö
  private def findMasterHenkilö(oid: Henkilö.Oid): Option[LaajatOppijaHenkilöTiedot] = henkilöt.findMasterOppija(oid) match {
    case Some(master) if master.oid != oid => Some(master)
    case _ => None
  }

  def oppijaHenkilöToTäydellisetHenkilötiedot(user: OppijaHenkilö): TäydellisetHenkilötiedot = {
    val syntymäpäivä = user.hetu.flatMap { hetu => Hetu.toBirthday(hetu) }
    TäydellisetHenkilötiedot(user.oid, user.hetu, user.syntymäaika.orElse(syntymäpäivä), user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.äidinkieli), convertKansalaisuus(user.kansalaisuus), Some(user.turvakielto))
  }

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.validate("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.validate("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }
}
