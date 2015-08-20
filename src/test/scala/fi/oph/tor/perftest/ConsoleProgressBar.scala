package fi.oph.tor.perftest

object ConsoleProgressBar {
  def showProgress[T](things: Seq[T])(block: T => Unit) = {
    things.zipWithIndex.map { case (thing, index) =>
      print("\rProgress: " + index + "/" + things.length)
      block(thing)
    }
    println
  }
}
