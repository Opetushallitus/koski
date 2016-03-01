package fi.oph.tor.oppija

import fi.oph.tor.henkilo._
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.schema.{FullHenkilö, Henkilö, NewHenkilö}

class RemoteOppijaRepository(henkilöPalveluClient: AuthenticationServiceClient, koodisto: KoodistoViitePalvelu) extends OppijaRepository {
  override def findOppijat(query: String): List[FullHenkilö] = {
    henkilöPalveluClient.search(query).results.map(toOppija)
  }

  override def findOrCreate(henkilö: NewHenkilö): Either[HttpStatus, Henkilö.Oid] =  {
    henkilöPalveluClient.findOrCreate(CreateUser.oppija(henkilö.hetu, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi)).right.map(_.oidHenkilo)
  }

  override def findByOid(oid: String): Option[FullHenkilö] = henkilöPalveluClient.findByOid(oid).map(toOppija)

  override def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String) = henkilöPalveluClient.create(CreateUser.oppija(hetu, sukunimi, etunimet, kutsumanimi))

  override def findByOids(oids: List[String]): List[FullHenkilö] = henkilöPalveluClient.findByOids(oids).map(toOppija)

  private def toOppija(user: User) = FullHenkilö(user.oidHenkilo, user.hetu, user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus))

  private def convertÄidinkieli(äidinkieli: Option[Äidinkieli]) = äidinkieli.flatMap(äidinkieli => koodisto.getKoodistoKoodiViite("kieli", äidinkieli.kieliKoodi.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[Kansalaisuus]]) = {
    kansalaisuus.flatMap(_.map(k => koodisto.getKoodistoKoodiViite("maatjavaltiot2", k.kansalaisuusKoodi)).flatten match {
      case Nil => None
      case xs => Some(xs)
    })
  }
}