package fi.oph.koski.executors

import java.util.concurrent.{Executors, ThreadFactory, ThreadPoolExecutor}

object NamedThreadPool {
  def apply(name: String, size: Int) = ManagedThreadPoolExecutor.register(name, Executors.newFixedThreadPool(size, threadFactory(name)).asInstanceOf[ThreadPoolExecutor])

  private def threadFactory(namePrefix: String) = new ThreadFactory {
    val defaultThreadFactory = Executors.defaultThreadFactory()
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
}
