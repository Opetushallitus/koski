package fi.oph.koski.servlet

import fi.oph.koski.koskiuser.Unauthenticated

class RedirectServlet(val path: String, forwardFullPath: Boolean = true) extends KoskiSpecificApiServlet with Unauthenticated with NoCache {
  get("/*") {
    if (forwardFullPath) {
      redirect(path + "/" + multiParams("splat").mkString("/"))
    }
    else {
      redirect(path)
    }
  }
}
