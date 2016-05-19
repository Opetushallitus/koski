package fi.oph.tor.json

import java.io.PrintWriter
import fi.oph.tor.log.Logging
import org.json4s.Formats
import org.scalatra.ScalatraContext
import rx.lang.scala.{Subscription, Observable}
import scala.concurrent.{Future, Promise}

object JsonStreamWriter extends Logging {
  def writeJsonStream(objects: Observable[AnyRef], writer: PrintWriter)(implicit formats: Formats): Future[String] = {
    val p = Promise[String]()
    var subscription: Option[Subscription] = None

    writer.print("[")

    def onNext(item: (AnyRef, Int)) = item match {
      case (item, index) =>
        if (index > 0) {
          writer.print(",")
        }
        writer.print(org.json4s.jackson.Serialization.write(item))
    }

    def onError(error: Throwable) = {
      logger.error("Error while streaming output", error)
      p.failure(error)
      subscription.foreach { _.unsubscribe}
    }

    def onEnd {
      writer.print("]")
      p.success("")
    }

    subscription = Some(objects.zipWithIndex.subscribe(onNext _, onError _, onEnd _))

    p.future
  }

  def writeJsonStream(objects: Observable[AnyRef])(implicit ctx: ScalatraContext, formats: Formats): Future[String] = {
    ctx.contentType = "application/json;charset=utf-8"
    writeJsonStream(objects, ctx.response.getWriter)
  }
}