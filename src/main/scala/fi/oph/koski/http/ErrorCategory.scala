package fi.oph.koski.http

private object ErrorCategory {
  def makeKey(key: String, subkey: String) = key + "." + subkey
}

import ErrorCategory._

case class ErrorCategory(val key: String, val statusCode: Int, val message: String) {
  def this(parent: ErrorCategory, key: String, message: String) = {
    this(makeKey(parent.key, key), parent.statusCode, message)
    parent.addSubcategory(key, this)
  }
  def subcategory(subkey: String, message: String) = new ErrorCategory(this, subkey, message)

  def apply(message: AnyRef): HttpStatus = HttpStatus(statusCode, List(ErrorDetail(key, message)))
  def apply(): HttpStatus = apply(message)

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