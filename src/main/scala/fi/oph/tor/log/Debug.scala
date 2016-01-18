package fi.oph.tor.log

object Debug {
  def debug[A](x: A): A = {
    println(x)
    x
  }
}
