package fi.oph.koski.servlet

import fi.oph.koski.util.Timing
import org.scalatra.ScalatraBase

trait TimedServlet extends ScalatraBase with Timing {
  private def timedAction(verb: String, path: String, action: => Any, threshold: Int = 100) = {
    val blockname: String = verb + " " + request.getServletPath + path
    timed(blockname, thresholdMs = threshold)(action)
  }
  def get(s: String)(action: => Any) = super.get(s)(timedAction("GET", s, action))

  def post(s: String)(action: => Any) = super.post(s)(timedAction("POST", s, action))

  def put(s: String)(action: => Any) = super.put(s)(timedAction("PUT", s, action))

  def delete(s: String)(action: => Any) = super.delete(s)(timedAction("DELETE", s, action))
}
