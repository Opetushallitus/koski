package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{RequiresSession, UserLanguage}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.userdirectory.MockDirectoryClient

/**
 * Virkailijan kieli tulee asiointikielestä eikä sitä voi vaihtaa käyttöliittymästä. Paikallisessa
 * kehitysympäristössä kielen saa silti vaihdettua asettamalla mock-käyttäjän asiointikielen: kieli
 * ratkeaa tämän jälkeen täsmälleen samaa reittiä kuin tuotannossa, eikä kielenratkaisuun tarvita
 * kehitysaikaista haaraa. Ks. MockDirectoryClient.
 *
 * Riittää että käyttäjä on kirjautunut: myös pelkillä Valpas-oikeuksilla oleva käyttäjä saa vaihtaa
 * oman kielensä, eikä Koski-oikeuksia vaadita.
 */
class MockAsiointikieliServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresSession with NoCache {
  post("/:lang") {
    UserLanguage.sanitizeLanguage(Some(params("lang"))) match {
      case Some(kieli) =>
        MockDirectoryClient.setAsiointikieli(session.user.username, kieli)
        // Sivunlataus ohittaa välimuistin, mutta session kieli tulee sieltä: tyhjennetään, jotta
        // molemmat näkevät uuden kielen heti.
        application.directoryClient.invalidateCache()
        "ok"
      case None => haltWithStatus(KoskiErrorCategory.badRequest.queryParam("Tuntematon kieli: " + params("lang")))
    }
  }
}
