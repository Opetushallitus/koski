package fi.oph.koski.henkilo

import fi.oph.koski.henkilo.AuthenticationServiceClient.{OppijaHenkilö, QueryHenkilö}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._

case class OpintopolkuHenkilöRepository(henkilöPalveluClient: AuthenticationServiceClient, koodisto: KoodistoViitePalvelu) extends FindByHetu with FindByOid with Logging {
  def findByHetu(hetu: String)(implicit user: KoskiSession): Option[HenkilötiedotJaOid] = {
    val opp = henkilöPalveluClient.findOppijaByHetu(hetu).map(h => HenkilötiedotJaOid(h.oidHenkilo, Some(hetu), h.etunimet, h.kutsumanimi, h.sukunimi))
    opp
  }

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, TäydellisetHenkilötiedot] =  {
    henkilöPalveluClient.findOrCreate(AuthenticationServiceClient.UusiHenkilö.oppija(henkilö.hetu, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi)).right.flatMap { h =>
      toTäydellisetHenkilötiedot(h) match {
        case Some(t) => Right(t)
        case None => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Henkilöltä puuttuu hetu"))
      }
    }
  }

  def findByOid(oid: String): Option[TäydellisetHenkilötiedot] = {
    val retval = henkilöPalveluClient.findOppijaByOid(oid).flatMap(toTäydellisetHenkilötiedot)
    retval
  }

  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot] = oids match {
    case Nil => Nil // <- authentication-service fails miserably with empty input list
    case _ => henkilöPalveluClient.findOppijatByOids(oids).flatMap(toTäydellisetHenkilötiedot)
  }

  // TODO: muuta signature OppijaHenkilö -> TäydellisetHenkilötiedot
  private def toTäydellisetHenkilötiedot(user: OppijaHenkilö): Option[TäydellisetHenkilötiedot] = {
    Some(TäydellisetHenkilötiedot(user.oidHenkilo, user.hetu, user.syntymaika, user.etunimet, user.kutsumanimi, user.sukunimi, convertÄidinkieli(user.aidinkieli), convertKansalaisuus(user.kansalaisuus)))
  }

  private def toHenkilötiedot(user: QueryHenkilö) =  HenkilötiedotJaOid(user.oidHenkilo, user.hetu, user.etunimet, user.kutsumanimi, user.sukunimi)

  private def convertÄidinkieli(äidinkieli: Option[String]) = äidinkieli.flatMap(äidinkieli => koodisto.getKoodistoKoodiViite("kieli", äidinkieli.toUpperCase))

  private def convertKansalaisuus(kansalaisuus: Option[List[String]]) = {
    kansalaisuus.flatMap(_.flatMap(kansalaisuus => koodisto.getKoodistoKoodiViite("maatjavaltiot2", kansalaisuus)) match {
      case Nil => None
      case xs: List[Koodistokoodiviite] => Some(xs)
    })
  }
}

object MockOpintopolkuHenkilöRepository extends OpintopolkuHenkilöRepository(new MockAuthenticationServiceClient(), MockKoodistoViitePalvelu)