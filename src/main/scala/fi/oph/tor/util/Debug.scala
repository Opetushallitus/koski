package fi.oph.tor.util

object Debug {
  def debug[A](x: A): A = {
    println(x)
    x
  }
}
