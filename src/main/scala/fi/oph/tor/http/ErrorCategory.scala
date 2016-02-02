package fi.oph.tor.http

private object ErrorCategory {
  def makeKey(key: String, subkey: String) = key + "." + subkey
}

import ErrorCategory._

case class ErrorCategory(val key: String, val statusCode: Int) {
  def this(parent: ErrorCategory, key: String) = {
    this(makeKey(parent.key, key), parent.statusCode)
    parent.addSubcategory(key, this)
  }
  def subcategory(subkey: String) = new ErrorCategory(this, subkey)
  def subcategory(subkey: String, message: String) = new CategoryWithDefaultText(this, subkey, message)
  def apply(message: AnyRef): HttpStatus = HttpStatus(statusCode, List(ErrorDetail(this, message)))

  private var children_ : List[(String, ErrorCategory)] = Nil
  def children: List[(String, ErrorCategory)] = children_
  protected[http] def addSubcategory[T <: ErrorCategory](key: String, subcategory: T) = {
    children_ = children_ ++ List((key, subcategory))
    subcategory
  }
}

class CategoryWithDefaultText(key: String, status: Int, val message: String) extends ErrorCategory(key, status) {
  def this(parent: ErrorCategory, key: String, message: String) = {
    this(makeKey(parent.key, key), parent.statusCode, message)
    parent.addSubcategory(key, this)
  }
  def apply(): HttpStatus = apply(message)
}