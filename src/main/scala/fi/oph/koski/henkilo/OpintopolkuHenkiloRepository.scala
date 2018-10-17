package fi.oph.koski.henkilo

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._

case class TäydellisetHenkilötiedotWithMasterInfo(henkilö: TäydellisetHenkilötiedot, master: Option[TäydellisetHenkilötiedot]) {
  def oid = henkilö.oid
  def hetu = henkilö.hetu
  def etunimet: String = henkilö.etunimet
  def kutsumanimi: String = henkilö.kutsumanimi
  def sukunimi: String = henkilö.sukunimi
}

case class OpintopolkuHenkilöRepository(henkilöt: OpintopolkuHenkilöFacade, koodisto: KoodistoViitePalvelu) extends FindByOid with Logging {
  def withMasterInfo(henkilötiedot: TäydellisetHenkilötiedot) = TäydellisetHenkilötiedotWithMasterInfo(henkilötiedot, findMasterHenkilö(henkilötiedot.oid))

  // Tarkistaa vain Oppijanumerorekisterin, ei koskaan luo uutta oppijanumeroa Virta/YTR-datan perusteella
  def findByHetu(hetu: String): Option[TäydellisetHenkilötiedot] = {
    Hetu.validFormat(hetu)
      .toOption
      .flatMap(henkilöt.findOppijaByHetu)
      .map(toTäydellisetHenkilötiedot)
  }

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, TäydellisetHenkilötiedot] =  {
    val validKutsumanimet = henkilö.etunimet.trim
      .replaceAll("\\s+", " ")
      .replaceAll("\\s*-\\s*", "-")
      .split(" ")
      .flatMap(n => n :: n.split("-").toList).toList.distinct

    val kutsumanimi = henkilö.kutsumanimi.flatMap(n => validKutsumanimet.find(_ == n)).getOrElse(validKutsumanimet.head)

    henkilöt
      .findOrCreate(UusiOppijaHenkilö(Some(henkilö.hetu), henkilö.sukunimi, henkilö.etunimet, kutsumanimi))
      .right.map(toTäydellisetHenkilötiedot)
  }

  def findByOid(oid: String): Option[TäydellisetHenkilötiedot] = {
    henkilöt.findOppijaByOid(oid).map(toTäydellisetHenkilötiedot)
  }

  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot] = oids match {
    case Nil => Nil // <- authentication-service fails miserably with empty input list
    case _ => henkilöt.findOppijatByOids(oids).map(toTäydellisetHenkilötiedot)
  }

  // Hakee master-henkilön, jos eri kuin tämä henkilö
  private def findMasterHenkilö(oid: Henkilö.Oid): Option[TäydellisetHenkilötiedot] = henkilöt.findMasterOppija(oid) match {
    case Some(master) if master.oidHenkilo != oid => Some(master.toTäydellisetHenkilötiedot)
    case _ => None
  }

  private def toTäydellisetHenkilötiedot(user: OppijaHenkilö): TäydellisetHenkilötiedot = {
    val syntymäpäivä = user.hetu.flatMap { hetu => Hetu.toBirthday(hetu) }
    TäydellisetHenkilötiedot(user.oidHenkilo, user.hetu, user.syntymaika.orElse(syntymäpäivä), user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus), Some(user.turvakielto))
  }

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.validate("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.validate("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }
}

object MockOpintopolkuHenkilöRepository extends OpintopolkuHenkilöRepository(new MockOpintopolkuHenkilöFacade(), MockKoodistoViitePalvelu)
