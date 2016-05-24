package fi.oph.tor.opiskeluoikeus

import fi.oph.tor.log.Logging
import rx.lang.scala.Observable

object StreamCounter extends Logging {
  def countElems[T](obs: Observable[T]): Observable[T] = {
    var count = 0

    def onNext(item: T) = item match {
      case _ =>
        StreamCounter.this.synchronized {
          count = count + 1
          if (count % 1000 == 0) logger.info("" + count)
        }
    }

    def onError(error: Throwable) = {
      logger.error(error)("Error while streaming output")
    }


    obs.subscribe(onNext _, onError _, () => {
      logger.info("Done, " + count + " items")
    })
    Observable.empty
  }
}
