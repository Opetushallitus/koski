package fi.oph.koski.henkilo

import fi.oph.koski.henkilo.AuthenticationServiceClient.{OppijaHenkilö, QueryHenkilö}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._

class OpintopolkuHenkilöRepository(henkilöPalveluClient: AuthenticationServiceClient, koodisto: KoodistoViitePalvelu) extends FindByHetu with FindByOid {
  def findByHetu(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid] = {
    henkilöPalveluClient.search(query).results.flatMap(toHenkilötiedot) // TODO: käytä spesifisempää RESTiä
  }

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, TäydellisetHenkilötiedot] =  {
    henkilöPalveluClient.findOrCreate(AuthenticationServiceClient.UusiHenkilö.oppija(henkilö.hetu, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi)).right.flatMap { h =>
      toTäydellisetHenkilötiedot(h) match {
        case Some(t) => Right(t)
        case None => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Henkilöltä puuttuu hetu"))
      }
    }
  }

  def findByOid(oid: String): Option[TäydellisetHenkilötiedot] = henkilöPalveluClient.findOppijaByOid(oid).flatMap(toTäydellisetHenkilötiedot)

  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot] = oids match {
    case Nil => Nil // <- authentication-service fails miserably with empty input list
    case _ => henkilöPalveluClient.findOppijatByOids(oids).flatMap(toTäydellisetHenkilötiedot)
  }

  private def toTäydellisetHenkilötiedot(user: OppijaHenkilö) = user.hetu.map(hetu => TäydellisetHenkilötiedot(user.oidHenkilo, hetu, user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus)))

  private def toHenkilötiedot(user: QueryHenkilö) =  user.hetu.map(hetu => HenkilötiedotJaOid(user.oidHenkilo, hetu, user.etunimet, user.kutsumanimi, user.sukunimi))

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.getKoodistoKoodiViite("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.getKoodistoKoodiViite("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }
}

object MockOpintopolkuHenkilöRepository extends OpintopolkuHenkilöRepository(new MockAuthenticationServiceClient(), MockKoodistoViitePalvelu)