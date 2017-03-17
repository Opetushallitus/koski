package fi.oph.koski.servlet

import fi.oph.koski.http.KoskiErrorCategory
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
    case e: Exception =>
      logger.error(e)("Error occurred while streaming")
      renderStatus(KoskiErrorCategory.internalError())
  }

  def writeJsonStreamSynchronously(in: Observable[_ <: AnyRef]): Unit = {
    contentType = "application/json;charset=utf-8"
    val writer = response.getWriter
    in.zipWithIndex.toBlocking.foreach { case (item, index) =>
      if (index == 0) {
        writer.print("[")
      }
      if (index > 0) {
        writer.print(",")
      }
      val output: String = org.json4s.jackson.Serialization.write(item)(DefaultFormats)
      writer.print(output)
    }
    writer.print("]")
    writer.flush
    writer.close
  }
}
