package fi.oph.koski.servlet

import fi.oph.common.koskiuser.Unauthenticated

class RedirectServlet(val path: String, forwardFullPath: Boolean = true) extends ApiServlet with Unauthenticated with NoCache {
  get("/*") {
    if (forwardFullPath) {
      redirect(path + "/" + multiParams("splat").mkString("/"))
    }
    else {
      redirect(path)
    }
  }
}
