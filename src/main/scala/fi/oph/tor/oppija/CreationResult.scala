package fi.oph.tor.oppija

sealed trait CreationResult[A] {
  def flatMap[B](f : A => CreationResult[B]): CreationResult[B]
  def map[B](f: A => B): CreationResult[B]

  def httpStatus: Int
  def text: String
  def ok = httpStatus == 200
}

case class Created[A](oid: A) extends CreationResult[A] {
  def httpStatus = 200
  def text = oid.toString

  def flatMap[B](f : A => CreationResult[B]) = f(oid)

  def map[B](f: (A) => B) = Created(f(oid))
}

case class Exists[A](oid: A) extends CreationResult[A] {
  def httpStatus = 200
  def text = oid.toString

  def flatMap[B](f : A => CreationResult[B]) = f(oid)
  def map[B](f: (A) => B) = Exists(f(oid))
}

case class Failed[A](httpStatus: Int, text: String) extends CreationResult[A] {
  override def flatMap[B](f: (A) => CreationResult[B]) = Failed[B](httpStatus, text)
  def map[B](f: (A) => B) = Failed[B](httpStatus, text)
}
