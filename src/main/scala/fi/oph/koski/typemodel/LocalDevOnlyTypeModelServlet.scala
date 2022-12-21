package fi.oph.koski.typemodel

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.sso.KoskiSpecificSSOSupport

class LocalDevOnlyTypeModelServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
  with KoskiSpecificAuthenticationSupport
  with KoskiSpecificSSOSupport
  with NoCache
{
  get("/update") {
    TsFileUpdater.updateTypeFiles()
    "Files written"
  }
}
