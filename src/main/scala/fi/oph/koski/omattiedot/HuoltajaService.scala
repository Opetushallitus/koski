package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.ValtuudetSessionRow
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö.{Hetu, Oid}
import fi.oph.koski.schema.{Oppija, UusiHenkilö}
import fi.oph.koski.util.WithWarnings
import fi.vm.sade.suomifi.valtuudet._

import scala.util.Try

class HuoltajaService(application: KoskiApplication) extends Logging {
  private val valtuudetSessionRepository = ValtuudetSessionRepository(application.masterDatabase.db)
  private val client = ValtuudetClient(application.config)

  def findHuollettavaOppija(valtuutusKoodi: String, koskiRoot: String)(implicit koskiSession: KoskiSession): Either[HttpStatus, Option[WithWarnings[Oppija]]] = if (valtuutusKoodi == "errorCreatingSession") {
    Left(KoskiErrorCategory.unavailable.suomifivaltuudet())
  } else {
    val huollettava = valtuudetSessionRepository.get(koskiSession.oid)
      .map(session => getHuollettava(koskiSession.oid, session.sessionId, valtuutusKoodi, koskiRoot))
      .map(_.map(findOrCreate))
      .getOrElse(Right(None))

    huollettava.map(_.flatMap(findOppijaAllowEmpty))
  }

  def findUserOppijaAllowEmpty(implicit koskiSession: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    application.oppijaFacade.findUserOppija.left.flatMap(status => opinnotonOppija(koskiSession.oid).toRight(status))
  }

  def findUserOppijaNoAuditLog(implicit koskiSession: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    application.henkilöRepository.findByOid(koskiSession.oid)
      .map { henkilöTiedot =>
        application.opiskeluoikeusRepository.findByCurrentUser(henkilöTiedot).map(Oppija(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilöTiedot), _))
      }.orElse(opinnotonOppija(koskiSession.oid))
      .toRight(KoskiErrorCategory.notFound.oppijaaEiLöydy())
  }

  def getValtuudetUrl(koskiRoot: String)(implicit koskiSession: KoskiSession): Either[HttpStatus, String] =
    createAndStoreValtuudetSession(koskiSession).map(valtuudetSession => redirectUrl(valtuudetSession, koskiRoot, koskiSession.lang))

  def getHuollettava(oppijaOid: String): Option[OppijaHenkilö] = for {
    session <- valtuudetSessionRepository.get(oppijaOid)
    selectedPerson <- session.accessToken.flatMap(getSelectedPerson(session.sessionId, _).toOption)
    huollettava <- application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(selectedPerson.personId)
  } yield huollettava

  private def findOppijaAllowEmpty(huollettava: OppijaHenkilö)(implicit koskiSession: KoskiSession) =
    application.oppijaFacade.findUserOppija(KoskiSession.huollettavaSession(koskiSession, huollettava))
      .toOption
      .orElse(opinnotonOppija(huollettava.oid))

  private def opinnotonOppija(oid: String) =
    application.henkilöRepository.findByOid(oid)
      .map(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot)
      .map(Oppija(_, Nil))
      .map(WithWarnings(_, Nil))

  private def createAndStoreValtuudetSession(koskiSession: KoskiSession): Either[HttpStatus, ValtuudetSessionRow] = {
    createValtuudetSession(koskiSession.oid, findHetu(koskiSession)).map(valtuudetSessionRepository.store(koskiSession.oid, _))
  }

  private def findHetu(koskiSession: KoskiSession) =
    application.henkilöRepository.findByOid(koskiSession.oid).flatMap(_.hetu).getOrElse(throw new Exception("Kansalaisen istunto ilman hetua"))

  private def createValtuudetSession(oid: Oid, hetu: Hetu) = {
    Try(client.createSession(ValtuudetType.PERSON, hetu)).toEither.left.map { e =>
      logger.error(s"Virhe luotaessa sessiota henkilölle $oid: ${e.getMessage}")
      KoskiErrorCategory.unavailable.suomifivaltuudet()
    }
  }

  private def getHuollettava(oppijaOid: String, sessionId: String, valtuutusCode: String, koskiRoot: String) = {
    valtuudetSessionRepository.getAccessToken(oppijaOid, valtuutusCode, () => client.getAccessToken(valtuutusCode, redirectBackUrl(koskiRoot)))
      .flatMap(getSelectedPerson(sessionId, _))
  }

  private def getSelectedPerson(sessionId: String, accessToken: String) =
    Try {
      client.getSelectedPerson(sessionId, accessToken)
    }.toEither.left.map { e =>
      logger.error(s"Virhe haettaessa huollettavaa sessionId:llä $sessionId: ${e.getMessage}")
      KoskiErrorCategory.unavailable.suomifivaltuudet()
    }

  private def redirectUrl(session: ValtuudetSessionRow, koskiRoot: String, lang: String): String =
    client.getRedirectUrl(session.userId,  redirectBackUrl(koskiRoot), lang)

  private def redirectBackUrl(rootUrl: String) = s"$rootUrl/huoltaja"

  private def findOrCreate(huollettava: PersonDto) = {
    val nimet = Option(huollettava.name).map(_.trim).getOrElse("").split(" ")
    application.henkilöRepository.findOrCreate(UusiHenkilö(
      hetu = huollettava.personId,
      etunimet = nimet.drop(1).mkString(" "),
      kutsumanimi = None,
      sukunimi = nimet.head
    )).toOption
  }
}
