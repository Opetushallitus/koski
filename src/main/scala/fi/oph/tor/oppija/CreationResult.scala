package fi.oph.tor.oppija

sealed trait CreationResult {
  def httpStatus: Int
  def text: String
  def ok = httpStatus == 200
}

case class Created(oid: Any) extends CreationResult {
  def httpStatus = 200
  def text = oid.toString
}

case class Exists(oid: Any) extends CreationResult {
  def httpStatus = 200
  def text = oid.toString
}

case class Failed(httpStatus: Int, text: String) extends CreationResult
