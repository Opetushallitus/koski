package fi.oph.tor.log

trait Loggable {
}

object Loggable {
  def describe(arg: Any): String = {
    try {
      arg match {
        case null => "null"
        case s: String => "\""+ s + "\""
        case s: java.lang.Boolean => s.toString
        case n: Number => n.toString
        case x: Option[AnyRef] => x.map(y => describe(y)).toString
        case x: Loggable => x.toString
        case x => "_"
      }
    } catch {
      case e:Exception => "error" // <- catch all exceptions to make absolutely sure this won't break the software
    }
  }
}
