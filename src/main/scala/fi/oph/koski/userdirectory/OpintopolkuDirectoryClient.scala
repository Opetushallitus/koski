package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.oph.koski.henkilo.{KäyttäjäHenkilö, OppijanumeroRekisteriClient}
import fi.oph.koski.http.Http
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.Logging
import fi.oph.koski.sso.CasService

/**
  * Replacement for the LDAP-based directory client
  */
class OpintopolkuDirectoryClient(config: Config, casService: CasService) extends DirectoryClient with Logging {
  private val käyttöoikeusServiceClient = KäyttöoikeusServiceClient(config)
  private val oppijanumeroRekisteriClient = OppijanumeroRekisteriClient(config)

  override def findUser(userid: String): Option[DirectoryUser] = {
    logger.info(s"START: findUser ${userid}")
    val result = Http.runIO(käyttöoikeusServiceClient.findKäyttöoikeudetByUsername(userid).map {
      case List(käyttäjä) => Some(DirectoryClient.resolveKäyttöoikeudet(käyttäjä))
      case Nil => None
      case _ => throw new RuntimeException(s"More than 1 user found with username $userid")
    }).flatMap { case (oid: String, käyttöoikeudet: List[Käyttöoikeus]) => findKäyttäjä(oid, käyttöoikeudet) }
    logger.info(s"END: findUser ${userid}")
    result
  }

  override def authenticate(userid: String, wrappedPassword: Password): Boolean =
    casService.authenticateVirkailija(userid, wrappedPassword)

  private def findKäyttäjä(oid: String, käyttöoikeudet: List[Käyttöoikeus]) = {
    Http.runIO(oppijanumeroRekisteriClient.findKäyttäjäByOid(oid)).map { (käyttäjä: KäyttäjäHenkilö) =>
      DirectoryUser(käyttäjä.oidHenkilo, käyttöoikeudet, käyttäjä.etunimet, käyttäjä.sukunimi, käyttäjä.asiointiKieli.map(_.kieliKoodi))
    }
  }
}
