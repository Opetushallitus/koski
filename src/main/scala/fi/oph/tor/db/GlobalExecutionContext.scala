package fi.oph.tor.db
import scala.concurrent.ExecutionContext.Implicits.global

trait GlobalExecutionContext {
  implicit val executor = global
}
