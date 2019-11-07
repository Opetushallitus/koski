package fi.oph.koski.omattiedot

import java.util.UUID.randomUUID

import fi.oph.koski.henkilo.MockOppijat
import org.http4s.Request

object MockValtuudetClient extends ValtuudetClient {
  override def createSession(hetu: String): Either[ValtuudetFailure, SessionResponse] = if (MockOppijat.markkanen.hetu.contains(hetu)) {
    Left(SuomifiValtuudetFailure(403, "", Request()))
  } else {
    Right(SessionResponse(hetu, hetu))
  }


  override def getSelectedPerson(sessionId: String, accessToken: String): Either[ValtuudetFailure, SelectedPersonResponse] = {
    val (hetu, nimet) = findOppija(sessionId).getOrElse(???)
    if (MockOppijat.tero.hetu.contains(hetu)) {
      Left(SuomifiValtuudetFailure(400, "", Request()))
    } else {
      Right(SelectedPersonResponse(hetu, nimet))
    }
  }

  override def getRedirectUrl(userId: String, callbackUrl: String, language: String): String = if (findOppija(userId).isDefined) {
    s"/huoltaja?code=${randomUUID.toString}"
  } else {
    "/huoltaja?error=xyz"
  }

  private var codes = Set[String]()
  def getAccessToken(code: String, callbackUrl: String): Either[ValtuudetFailure, String] = synchronized {
    if (codes.contains(code)) {
      Left(SuomifiValtuudetFailure(400, "", Request()))
    } else {
      codes = codes + code
      Right("mock-token")
    }
  }

  private def findOppija(hetu: String) = {
    findOppijaFromONR(hetu).map(o => (o.hetu.get, s"${o.sukunimi} ${o.etunimet}")).orElse {
      if (MockOppijat.eerola.hetu.contains(hetu)) {
        Some(("080340-273P", "Eioppijanumerorekisterissä Erkki Einari"))
      } else {
        None
      }
    }
  }

  private def findOppijaFromONR(hetu: String) = {
    if (MockOppijat.aikuisOpiskelija.hetu.contains(hetu)) {
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
}

