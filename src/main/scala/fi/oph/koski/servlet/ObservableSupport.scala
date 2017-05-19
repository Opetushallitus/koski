package fi.oph.koski.servlet

import java.io.{EOFException, PrintWriter}

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.LocalDateSerializer
import org.json4s.DefaultFormats
import rx.lang.scala.Observable

trait ObservableSupport extends ApiServlet {
  override protected def renderResponse(actionResult: Any): Unit = {
    actionResult match {
      case in: Observable[AnyRef] => streamResponse(in)
      case a => super.renderResponse(a)
    }
  }

  def streamResponse(in: Observable[_ <: AnyRef]): Unit = try {
    writeJsonStreamSynchronously(in)
  } catch {
    case HttpClientEofException(eof) => renderInternalError(logger.warn(eof)("Client encountered a problem while reading streamed response"))
    case e: Exception => renderInternalError(logger.error(e)("Error occurred while streaming"))
  }

  private def renderInternalError(log: Unit) = renderStatus(KoskiErrorCategory.internalError())

  def writeJsonStreamSynchronously(in: Observable[_ <: AnyRef]): Unit = {
    contentType = "application/json;charset=utf-8"
    val writer = HttpWriter(response.getWriter)
    var empty = true
    in.zipWithIndex.toBlocking.foreach { case (item, index) =>
      if (index == 0) {
        writer.print("[")
        empty = false
      }
      if (index > 0) {
        writer.print(",")
      }
      val output: String = org.json4s.jackson.Serialization.write(item)(DefaultFormats + LocalDateSerializer)
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

case class HttpClientEofException(e: EOFException) extends RuntimeException
case class HttpWriter(writer: PrintWriter) {
  def print(s: String): Unit = try {
    writer.print(s)
  } catch {
    case eof: EOFException => throw HttpClientEofException(eof)
    case e: Exception => e.getCause match {
      case eof: EOFException => throw HttpClientEofException(eof)
      case _ => throw e
    }
  }

  def flush(): Unit = writer.flush()
  def close(): Unit = writer.close()
}
