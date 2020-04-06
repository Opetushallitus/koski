package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.HtmlServlet

class YtrKoesuoritusServlet(implicit val application: KoskiApplication) extends HtmlServlet with RequiresKansalainen {
  private val koesuoritukset: KoesuoritusService = KoesuoritusService(application.config)

  get("/:copyOfExamPaper") {
    val examPaper = getStringParam("copyOfExamPaper")
    val hasAccess = hasAccessTo(examPaper)
    if (koesuoritukset.koesuoritusExists(examPaper) && hasAccess) {
      contentType = "application/pdf"
      koesuoritukset.writeKoesuoritus(examPaper, response.getOutputStream)
    } else {
      logger.warn(s"Exam paper $examPaper not found, hasAccess: $hasAccess")
      haltWithStatus(KoskiErrorCategory.notFound.suoritustaEiLöydy())
    }
  }

  private def hasAccessTo(examPaper: String): Boolean = {
    logger.debug(s"Tarkistetaan ${if (isHuollettava) "huollettavan" else "oma"} koesuoritus access")
    getOppija.flatMap(application.ytrRepository.findByTunnisteet)
      .exists(_.examPapers.contains(examPaper))
  }

  private def getOppija: Option[HenkilönTunnisteet] =  {
    if (isHuollettava) {
      val huollettavaOid = getStringParam("huollettava")
      assert(koskiSession.isUsersHuollettava(huollettavaOid), "Käyttäjän oid: " + koskiSession.oid + " ei löydy etsittävän oppijan oideista: " + koskiSession)

      val huollettavaOppija = application.henkilöRepository.findByOid(huollettavaOid)
      if (huollettavaOppija.isEmpty) {
        logger.warn("Huollettavaa oppijaa ei löytynyt")
      } else {
        logger.debug(s"Tarkastetaan huollettavan oppijan koesuoritus access ${huollettavaOppija.get.oid}")
      }
      huollettavaOppija
    } else {
      application.henkilöRepository.findByOid(koskiSession.oid)
    }
  }

  private def isHuollettava = try {
    !params("huollettava").isEmpty
  }  catch {
    case _: Throwable => false
  }
}
