package fi.oph.koski.servlet

import java.io.EOFException

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import org.eclipse.jetty.server.HttpConnection
import rx.lang.scala.Observable

import scala.reflect.runtime.universe.TypeTag

trait ObservableSupport extends KoskiSpecificApiServlet {
  private val FlushInterval = 5000 // ms

  def streamResponse[T : TypeTag](in: Observable[T], user: KoskiSpecificSession): Unit = try {
    writeJsonStreamSynchronously(in, user)
  } catch {
    case e: Exception if isEOF(e) =>
      // Client abort, ok
    case e: Exception =>
      logger.error(s"Error occurred while streaming: ${e.toString}")
      HttpConnection.getCurrentConnection.abort(e)
  }

  def streamResponse[T : TypeTag](x: Either[HttpStatus, Observable[T]], user: KoskiSpecificSession): Unit = x match {
    case Right(in) =>
      streamResponse(in, user)
    case Left(status) =>
      haltWithStatus(status)
  }

  private def isEOF(e: Exception) = e.isInstanceOf[EOFException] || e.getCause.isInstanceOf[EOFException]

  private def writeJsonStreamSynchronously[T : TypeTag](in: Observable[T], user: KoskiSpecificSession): Unit = {
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
