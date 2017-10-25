package fi.oph.koski.http
import fi.oph.koski.http.ErrorCategory._
import fi.oph.koski.json.JsonSerializer
import org.json4s.JValue

import scala.reflect.runtime.{universe => ru}

private object ErrorCategory {
  def makeKey(key: String, subkey: String) = key + "." + subkey
  def defaultErrorContent(key: String, message: ErrorMessage): List[ErrorDetail] = {
    List(ErrorDetail(key, message))
  }
}

case class ErrorCategory(key: String, statusCode: Int, message: String, exampleResponse: JValue) {
  def this(key: String, statusCode: Int, message: String) = {
    this(key, statusCode, message, JsonSerializer.serializeWithRoot(defaultErrorContent(key, StringErrorMessage(message))))
  }
  def this(parent: ErrorCategory, key: String, message: String, exampleResponse: JValue) = {
    this(makeKey(parent.key, key), parent.statusCode, message, exampleResponse)
    parent.addSubcategory(key, this)
  }
  def this(parent: ErrorCategory, key: String, message: String) = {
    this(parent, key, message, JsonSerializer.serializeWithRoot(defaultErrorContent(makeKey(parent.key, key), StringErrorMessage(message))))
  }

  def subcategory(subkey: String, message: String, exampleResponse: JValue) = new ErrorCategory(this, subkey, message, exampleResponse)
  def subcategory(subkey: String, message: String) = new ErrorCategory(this, subkey, message)

  def apply(message: ErrorMessage): HttpStatus = HttpStatus(statusCode, defaultErrorContent(key, message))
  def apply(message: String): HttpStatus = apply(StringErrorMessage(message))

  def apply(): HttpStatus = statusCode match {
    case 200 => HttpStatus.ok
    case _ => apply(message)
  }

  def children: List[(String, ErrorCategory)] = children_
  def flatten: List[ErrorCategory] = children match {
    case Nil => List(this)
    case _ => children.flatMap(_._2.flatten)
  }

  private var children_ : List[(String, ErrorCategory)] = Nil
  protected[http] def addSubcategory[T <: ErrorCategory](key: String, subcategory: T) = {
    children_ = children_ ++ List((key, subcategory))
    subcategory
  }
}