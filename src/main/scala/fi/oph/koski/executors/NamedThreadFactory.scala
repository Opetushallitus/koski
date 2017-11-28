package fi.oph.koski.executors

import java.util.concurrent.{Executors, ThreadFactory}

case class NamedThreadFactory(namePrefix: String) extends ThreadFactory {
  private val defaultThreadFactory = Executors.defaultThreadFactory()
  private var idCounter = 0
  private def nextId = {
    idCounter = idCounter + 1
    idCounter
  }

  def newThread(r: Runnable) = this.synchronized {
    val t = defaultThreadFactory.newThread(r)
    t.setName(namePrefix + "-thread-" + nextId)
    t.setDaemon(true)
    t
  }
}
