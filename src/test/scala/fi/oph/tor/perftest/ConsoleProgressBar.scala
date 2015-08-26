package fi.oph.tor.perftest

object ConsoleProgressBar {
  def showProgress[T](things: Seq[T])(block: T => Unit) = {
    val started = System.currentTimeMillis
    val steps = 20
    things.zipWithIndex.map { case (thing, index) =>
      val stepsCompleted = steps * index / things.length
      print("\rProgress: [")
      for (a <- 0 to steps - 1) {
        print(if (a <= stepsCompleted) { "." } else { " " })
      }
      print("] " + index + "/" + things.length)
      val seconds = ((System.currentTimeMillis - started).toDouble / 1000)
      val speed = index.toDouble / seconds
      print(", elapsed " + seconds + " seconds, " + speed + " / second")
      block(thing)
    }
    println
  }
}
