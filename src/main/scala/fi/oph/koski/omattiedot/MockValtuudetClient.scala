package fi.oph.koski.omattiedot

import java.util.UUID.randomUUID

import fi.oph.koski.henkilo.MockOppijat
import fi.vm.sade.suomifi.valtuudet.{OrganisationDto, PersonDto, SessionDto, ValtuudetType, ValtuudetClient => SuomifiValtuudetClient}

object MockValtuudetClient extends SuomifiValtuudetClient {
  override def createSession(`type`: ValtuudetType, nationalIdentificationNumber: String): SessionDto = new SessionDto {
    if (MockOppijat.markkanen.hetu.contains(nationalIdentificationNumber)) {
      throw new RuntimeException("Unexpected response status: 403 Expected: 200 Url: https://asiointivaltuustarkastus.suomi.fi/xyz")
    }
    sessionId = nationalIdentificationNumber
    userId = nationalIdentificationNumber
  }

  override def getSelectedPerson(sessionId: String, accessToken: String): PersonDto = {
    val oppija = findOppija(sessionId).getOrElse(???)
    if (oppija == MockOppijat.tero) {
      throw new Exception("Unexpected response status: 400 Expected: 200 Url: https://asiointivaltuustarkastus.suomi.fi/xyz")
    }
    new PersonDto { personId = oppija.hetu.get; name = s"${oppija.etunimet} ${oppija.sukunimi}" }
  }

  override def getRedirectUrl(userId: String, callbackUrl: String, language: String): String = if (findOppija(userId).isDefined) {
    s"/huoltaja?code=${randomUUID.toString}"
  } else {
    "/huoltaja?error=xyz"
  }

  private var codes = Set[String]()
  override def getAccessToken(code: String, callbackUrl: String): String = synchronized {
    if (codes.contains(code)) {
      throw new Exception("Unexpected response status: 400 Expected: 200 Url: https://asiointivaltuustarkastus.suomi.fi/xyz")
    }
    codes = codes + code
    "mock-token"
  }

  override def destroySession(`type`: ValtuudetType, sessionId: String): Unit = ???
  override def isAuthorizedToPerson(sessionId: String, accessToken: String, nationalIdentificationNumber: String): Boolean = ???
  override def getSelectedOrganisation(sessionId: String, accessToken: String): OrganisationDto = ???

  private def findOppija(hetu: String) = if (MockOppijat.aikuisOpiskelija.hetu.contains(hetu)) {
    Some(MockOppijat.ylioppilasLukiolainen)
  } else if (MockOppijat.dippainssi.hetu.contains(hetu)) {
    Some(MockOppijat.eiKoskessa)
  } else if (MockOppijat.eiKoskessa.hetu.contains(hetu)) {
    Some(MockOppijat.dippainssi)
  } else if (MockOppijat.tero.hetu.contains(hetu)) {
    Some(MockOppijat.tero)
  } else if (MockOppijat.teija.hetu.contains(hetu)) {
    Some(MockOppijat.virtaEiVastaa)
  } else {
    None
  }
}

