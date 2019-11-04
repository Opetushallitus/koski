package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.ValtuudetSessionRow
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Oppija, UusiHenkilö}
import fi.oph.koski.util.WithWarnings

class HuoltajaService(application: KoskiApplication) extends Logging {
  private val valtuudetSessionRepository = ValtuudetSessionRepository(application.masterDatabase.db)
  private val client = ValtuudetClient(application.config)

  def findHuollettavaOppija(valtuutusKoodi: String, koskiRoot: String)(implicit koskiSession: KoskiSession): Either[List[HttpStatus], Option[WithWarnings[Oppija]]] = if (valtuutusKoodi == "errorCreatingSession") {
    logger.warn("Received code: errorCreatingSession")
    Left(List(KoskiErrorCategory.unavailable.suomifivaltuudet()))
  } else {
    findOrCreateHuollettavaOppijaHenkilö(valtuutusKoodi, koskiRoot, koskiSession)
      .map(_.flatMap(findOppijaAllowEmpty))
      .left.map(_.toHttpStatus(logger(koskiSession)))
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

  def getValtuudetUrl(koskiRoot: String)(implicit koskiSession: KoskiSession): Either[ValtuudetFailure, String] =
    createAndStoreValtuudetSession(koskiSession).map(valtuudetSession => redirectUrl(valtuudetSession, koskiRoot, koskiSession.lang))

  def getSelectedHuollettava(oppijaOid: String): Option[OppijaHenkilö] = for {
    session <- valtuudetSessionRepository.get(oppijaOid)
    selectedPerson <- session.accessToken.flatMap(client.getSelectedPerson(session.sessionId, _).toOption)
    huollettava <- application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(selectedPerson.personId)
  } yield huollettava

  private def findOrCreateHuollettavaOppijaHenkilö(valtuutusKoodi: String, koskiRoot: String, koskiSession: KoskiSession) = for {
    valtuudetSession <- valtuudetSessionRepository.get(koskiSession.oid).filter(_.code.forall(_ == valtuutusKoodi)).toRight(SessionExpired)
    accessToken <- valtuudetSessionRepository.getAccessToken(valtuudetSession, valtuutusKoodi, fetchAccessToken(koskiRoot))
    selectedHuollettava <- client.getSelectedPerson(valtuudetSession.sessionId, accessToken)
  } yield findOrCreate(selectedHuollettava)

  private def findOppijaAllowEmpty(huollettava: OppijaHenkilö)(implicit koskiSession: KoskiSession) =
    application.oppijaFacade.findUserOppija(KoskiSession.huollettavaSession(koskiSession, huollettava))
      .toOption
      .orElse(opinnotonOppija(huollettava.oid))

  private def opinnotonOppija(oid: String) =
    application.henkilöRepository.findByOid(oid)
      .map(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot)
      .map(Oppija(_, Nil))
      .map(WithWarnings(_, Nil))

  private def createAndStoreValtuudetSession(koskiSession: KoskiSession) = for {
    valtuudetSession <- client.createSession(findHetu(koskiSession))
  } yield valtuudetSessionRepository.store(koskiSession.oid, valtuudetSession)

  private def findHetu(koskiSession: KoskiSession) =
    application.henkilöRepository.findByOid(koskiSession.oid).flatMap(_.hetu).getOrElse(throw new Exception(s"Kansalaisen istunto (oid: ${koskiSession.oid}) ilman hetua"))

  private def fetchAccessToken(koskiRoot: String)(valtuutusCode: String) =
    client.getAccessToken(valtuutusCode, redirectBackUrl(koskiRoot))

  private def redirectUrl(session: ValtuudetSessionRow, koskiRoot: String, lang: String): String =
    client.getRedirectUrl(session.userId,  redirectBackUrl(koskiRoot), lang)

  private def redirectBackUrl(rootUrl: String) = s"$rootUrl/huoltaja"

  private def findOrCreate(huollettava: SelectedPersonResponse) = {
    val nimet = Option(huollettava.name).map(_.trim).getOrElse("").split(" ")
    application.henkilöRepository.findOrCreate(UusiHenkilö(
      hetu = huollettava.personId,
      etunimet = nimet.drop(1).mkString(" "),
      kutsumanimi = None,
      sukunimi = nimet.head
    )).toOption
  }
}
