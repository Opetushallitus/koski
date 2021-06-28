package fi.oph.koski.http

import fi.oph.koski.json.JsonSerializer
import org.json4s.JValue
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.universe.TypeTag

case class HttpStatus(statusCode: Int, errors: List[ErrorDetail]) {
  if (statusCode == 200 && errors.nonEmpty) throw new RuntimeException("HttpStatus 200 with error message " + errors.mkString(","))

  def isOk: Boolean = statusCode < 300

  def isError: Boolean = !isOk

  /** Pick given status if this one is ok. Otherwise stick with this one */
  def onSuccess(status: => HttpStatus): HttpStatus = if (isOk) { status } else { this }

  def errorString: Option[String] = errors.headOption.flatMap(_.message match {
    case JString(s) => Some(s)
    case otherJValue => None
  })

  def toEither: Either[HttpStatus, Unit] = if (isError) {
    Left(this)
  } else {
    Right(Unit)
  }
}

// Constructor is private to force force explicit usage of ErrorMessage. This allows us
// to quickly analyze what kind of things we put into our errors
case class ErrorDetail private(key: String, message: JValue) {
  override def toString = key + " (" + JsonMethods.compact(message) + ")"
}

object ErrorDetail {
  def apply(key: String, message: String): ErrorDetail = apply(key, StringErrorMessage(message))
  def apply(key: String, message: ErrorMessage): ErrorDetail = new ErrorDetail(key, message.asJValue)
}

object HttpStatus {
  val ok = HttpStatus(200, Nil)

  // Combinators

  /** If predicate is true, yield 200/ok, else run given block */
  def validate(predicate: => Boolean)(status: => HttpStatus): HttpStatus = if (predicate) { ok } else { status }

  /** Combine two statuses: concatenate errors list, pick highest status code */
  def append(a: HttpStatus, b: HttpStatus): HttpStatus = {
    HttpStatus(Math.max(a.statusCode, b.statusCode), a.errors ++ b.errors)
  }

  /** Append all given statuses into one, concatenating error list, picking highest status code */
  def fold(statuses: Iterable[HttpStatus]): HttpStatus = statuses.fold(ok)(append)
  def fold(statuses: HttpStatus*): HttpStatus = fold(statuses)

  def foldEithers[T](xs: Seq[Either[HttpStatus, T]]): Either[HttpStatus, Seq[T]] = {
    val lefts = xs.collect { case Left(e) => e }
    lefts match {
      case Nil => Right(xs.collect { case Right(value) => value })
      case errors: Iterable[HttpStatus] => Left(HttpStatus.fold(errors))
    }
  }

  /** Returns the first Right in the given Seq of Eithers; if none are Right, returns the first Left */
  def any[S, T]: Seq[Either[S, T]] => Either[S, T] = {
    case Seq() => throw new RuntimeException("Cannot take 'any' from an empty Seq")
    case nonEmpty: Any => nonEmpty.find(_.isRight) match {
      case Some(either) => either
      case None => nonEmpty.head
    }
  }

  def justStatus[A](either: Either[HttpStatus, A]): HttpStatus = either match {
    case Right(_) => HttpStatus.ok
    case Left(status) => status
  }
}

trait ErrorMessage {
  def asJValue: JValue
}

case class StringErrorMessage(str: String) extends ErrorMessage {
  override def asJValue: JValue = JString(str)
}

case class JsonErrorMessage[T : TypeTag](value: T) extends ErrorMessage {
  override def asJValue: JValue = JsonSerializer.serializeWithRoot(value)
}
