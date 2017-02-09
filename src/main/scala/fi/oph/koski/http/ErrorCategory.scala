package fi.oph.koski.http

private object ErrorCategory {
  def makeKey(key: String, subkey: String) = key + "." + subkey
  def defaultErrorContent(key: String, message: AnyRef): List[ErrorDetail] = {
    List(ErrorDetail(key, message))
  }
}

import fi.oph.koski.http.ErrorCategory._

case class ErrorCategory(val key: String, val statusCode: Int, val message: String, val exampleResponse: AnyRef) {
  def this(key: String, statusCode: Int, message: String) = {
    this(key, statusCode, message, Some(defaultErrorContent(key, message)))
  }
  def this(parent: ErrorCategory, key: String, message: String, exampleResponse: AnyRef) = {
    this(makeKey(parent.key, key), parent.statusCode, message, exampleResponse)
    parent.addSubcategory(key, this)
  }
  def this(parent: ErrorCategory, key: String, message: String) = {
    this(parent, key, message, defaultErrorContent(makeKey(parent.key, key), message))
  }

  def subcategory(subkey: String, message: String, exampleResponse: AnyRef) = new ErrorCategory(this, subkey, message, exampleResponse)
  def subcategory(subkey: String, message: String) = new ErrorCategory(this, subkey, message)

  def apply(message: AnyRef): HttpStatus = HttpStatus(statusCode, defaultErrorContent(key, message))

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