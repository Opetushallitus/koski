package fi.oph.koski.oppija

import fi.oph.koski.henkilo._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._

class RemoteOppijaRepository(henkilöPalveluClient: AuthenticationServiceClient, koodisto: KoodistoViitePalvelu) extends OppijaRepository {
  override def findOppijat(query: String): List[TäydellisetHenkilötiedot] = {
    if (Henkilö.isHenkilöOid(query)) {
      findByOid(query).toList
    } else {
      henkilöPalveluClient.search(query).results.map(toOppija)
    }
  }

  override def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, Henkilö.Oid] =  {
    henkilöPalveluClient.findOrCreate(CreateUser.oppija(henkilö.hetu, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi)).right.map(_.oidHenkilo)
  }

  override def findByOid(oid: String): Option[TäydellisetHenkilötiedot] = henkilöPalveluClient.findByOid(oid).map(toOppija)

  override def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot] = henkilöPalveluClient.findByOids(oids).map(toOppija)

  private def toOppija(user: User) = TäydellisetHenkilötiedot(user.oidHenkilo, user.hetu, user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus))

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.getKoodistoKoodiViite("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.getKoodistoKoodiViite("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }
}