package fi.oph.tor.perftest

object ConsoleProgressBar {
  def showProgress[T](things: Seq[T])(block: T => Unit) = {
    val steps = 20
    things.zipWithIndex.map { case (thing, index) =>
      val stepsCompleted = steps * index / things.length
      print("\rProgress: [")
      for (a <- 0 to steps - 1) {
        print(if (a <= stepsCompleted) { "." } else { " " })
      }
      print("] " + index + "/" + things.length)
      block(thing)
    }
    println
  }
}
