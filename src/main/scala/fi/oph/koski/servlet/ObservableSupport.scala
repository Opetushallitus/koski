package fi.oph.koski.servlet

import java.io.EOFException

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.common.json.JsonSerializer
import fi.oph.common.koskiuser.KoskiSession
import rx.lang.scala.Observable

import scala.reflect.runtime.universe.TypeTag

trait ObservableSupport extends ApiServlet {
  private val FlushInterval = 5000 // ms

  def streamResponse[T : TypeTag](in: Observable[T], user: KoskiSession): Unit = try {
    writeJsonStreamSynchronously(in, user)
  } catch {
    case e: Exception if isEOF(e) =>
      // Client abort, ok
    case e: Exception =>
      renderInternalError(logger.error(e)("Error occurred while streaming"))
  }

  def streamResponse[T : TypeTag](x: Either[HttpStatus, Observable[T]], user: KoskiSession): Unit = x match {
    case Right(in) =>
      streamResponse(in, user)
    case Left(status) =>
      haltWithStatus(status)
  }

  private def isEOF(e: Exception) = e.isInstanceOf[EOFException] || e.getCause.isInstanceOf[EOFException]

  private def renderInternalError(log: Unit) = renderStatus(KoskiErrorCategory.internalError())

  private def writeJsonStreamSynchronously[T : TypeTag](in: Observable[T], user: KoskiSession): Unit = {
    contentType = "application/json;charset=utf-8"
    val writer = response.getWriter
    var empty = true
    var lastFlushed = System.currentTimeMillis
    in.zipWithIndex.toBlocking.foreach { case (item, index) =>
      if (index == 0) {
        writer.print("[")
        empty = false
      }
      if (index > 0) {
        writer.print(",")
      }
      implicit val u = user
      val output: String = JsonSerializer.write(item)
      writer.print(output)

      val now = System.currentTimeMillis
      if ((now - lastFlushed) > FlushInterval) {
        lastFlushed = now
        writer.flush
      }
    }

    if (empty) {
      writer.print("[")
    }
    writer.print("]")
    writer.flush
    writer.close
  }
}
