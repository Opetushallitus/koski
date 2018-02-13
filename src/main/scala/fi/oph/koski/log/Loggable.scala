package fi.oph.koski.log

/**
 *  Marker trait for Loggable objects: the expectation is that the toString method returns a loggable string
 */
trait Loggable {
  /**
   * Returns a log-safe string
   */
  def logString: String
  override def toString = logString
}

trait NotLoggable extends Loggable {
  def logString: String = "*"
}

object Loggable {
  def describe(arg: Any): String = {
    try {
      arg match {
        case null => "null"
        case s: String => "\"" + s + "\""
        case s: java.lang.Boolean => s.toString
        case n: Number => n.toString
        case x: Option[AnyRef @unchecked] => x.map(y => describe(y)).toString
        case x: Loggable => x.logString
        case _ => "_"
      }
    } catch {
      case e:Exception => "error" // <- catch all exceptions to make absolutely sure this won't break the software
    }
  }
}
