package fi.oph.koski.henkilo

import fi.oph.koski.henkilo.oppijanumerorekisteriservice.OppijaHenkilö
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._

case class OpintopolkuHenkilöRepository(henkilöt: OpintopolkuHenkilöFacade, koodisto: KoodistoViitePalvelu) extends FindByHetu with FindByOid with Logging {
  def withMasterInfo(henkilötiedot: TäydellisetHenkilötiedot) = TäydellisetHenkilötiedotWithMasterInfo(henkilötiedot, findMasterHenkilö(henkilötiedot.oid))

  def findByHetu(hetu: String)(implicit user: KoskiSession): Option[HenkilötiedotJaOid] = {
    val opp = henkilöt.findOppijaByHetu(hetu).map(h => HenkilötiedotJaOid(h.oidHenkilo, Some(hetu), h.etunimet, h.kutsumanimi, h.sukunimi))
    opp
  }

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, TäydellisetHenkilötiedot] =  {
    val validKutsumanimet = henkilö.etunimet.trim
      .replaceAll("\\s+", " ")
      .replaceAll("\\s*-\\s*", "-")
      .split(" ")
      .flatMap(n => n :: n.split("-").toList).toList.distinct

    val kutsumanimi = henkilö.kutsumanimi.flatMap(n => validKutsumanimet.find(_ == n)).getOrElse(validKutsumanimet.head)

    henkilöt
      .findOrCreate(oppijanumerorekisteriservice.UusiHenkilö.oppija(Some(henkilö.hetu), henkilö.sukunimi, henkilö.etunimet, kutsumanimi))
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
    TäydellisetHenkilötiedot(user.oidHenkilo, user.hetu, user.syntymaika, user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus))
  }

  private def toHenkilötiedot(user: QueryHenkilö) =  HenkilötiedotJaOid(user.oidHenkilo, user.hetu, user.etunimet, user.kutsumanimi, user.sukunimi)

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.getKoodistoKoodiViite("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.getKoodistoKoodiViite("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }

  override def existsWithHetu(hetu: String)(implicit user: KoskiSession): Boolean = findByHetu(hetu).isDefined
}

case class QueryHenkilö(oidHenkilo: String, sukunimi: String, etunimet: String, kutsumanimi: String, hetu: Option[String])
case class HenkilöQueryResult(totalCount: Int, results: List[QueryHenkilö])

object MockOpintopolkuHenkilöRepository extends OpintopolkuHenkilöRepository(new MockOpintopolkuHenkilöFacade(), MockKoodistoViitePalvelu)
