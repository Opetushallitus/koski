package fi.oph.koski.servlet

import java.io.{EOFException, PrintWriter}

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import rx.lang.scala.Observable

import scala.reflect.runtime.universe.TypeTag

trait ObservableSupport extends ApiServlet {
  def streamResponse[T : TypeTag](in: Observable[T])(implicit user: KoskiSession): Unit = try {
    writeJsonStreamSynchronously(in)
  } catch {
    case e: Exception if isEOF(e) =>
      // Client abort, ok
    case e: Exception =>
      renderInternalError(logger.error(e)("Error occurred while streaming"))
  }

  private def isEOF(e: Exception) = e.isInstanceOf[EOFException] || e.getCause.isInstanceOf[EOFException]

  private def renderInternalError(log: Unit) = renderStatus(KoskiErrorCategory.internalError())

  def writeJsonStreamSynchronously[T : TypeTag](in: Observable[T])(implicit user: KoskiSession): Unit = {
    contentType = "application/json;charset=utf-8"
    val writer = response.getWriter
    var empty = true
    in.zipWithIndex.toBlocking.foreach { case (item, index) =>
      if (index == 0) {
        writer.print("[")
        empty = false
      }
      if (index > 0) {
        writer.print(",")
      }
      val output: String = JsonSerializer.write(item)
      writer.print(output)
    }

    if (empty) {
      writer.print("[")
    }
    writer.print("]")
    writer.flush
    writer.close
  }
}