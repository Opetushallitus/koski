package fi.oph.koski.oppija

import fi.oph.koski.henkilo.{AuthenticationServiceClient, CreateUser, User, UserQueryUser}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

class OpintopolkuOppijaRepository(henkilöPalveluClient: AuthenticationServiceClient, koodisto: KoodistoViitePalvelu) extends OppijaRepository {
  override def findOppijat(query: String)(implicit user: KoskiUser): List[HenkilötiedotJaOid] = {
    if (Henkilö.isHenkilöOid(query)) {
      findByOid(query).map(_.toHenkilötiedotJaOid).toList
    } else {
      henkilöPalveluClient.search(query).results.flatMap(toHenkilötiedot)
    }
  }

  override def findOrCreate(henkilö: UusiHenkilö)(implicit user: KoskiUser): Either[HttpStatus, Henkilö.Oid] =  {
    henkilöPalveluClient.findOrCreate(CreateUser.oppija(henkilö.hetu, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi)).right.map(_.oidHenkilo)
  }

  override def findByOid(oid: String)(implicit user: KoskiUser): Option[TäydellisetHenkilötiedot] = henkilöPalveluClient.findByOid(oid).flatMap(toTäydellisetHenkilötiedot)

  override def findByOids(oids: List[String])(implicit user: KoskiUser): List[TäydellisetHenkilötiedot] = henkilöPalveluClient.findByOids(oids).flatMap(toTäydellisetHenkilötiedot)

  private def toTäydellisetHenkilötiedot(user: User) = user.hetu.map(hetu => TäydellisetHenkilötiedot(user.oidHenkilo, hetu, user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus)))

  private def toHenkilötiedot(user: UserQueryUser) =  user.hetu.map(hetu => HenkilötiedotJaOid(user.oidHenkilo, hetu, user.etunimet, user.kutsumanimi, user.sukunimi))

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.getKoodistoKoodiViite("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.getKoodistoKoodiViite("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }
}
