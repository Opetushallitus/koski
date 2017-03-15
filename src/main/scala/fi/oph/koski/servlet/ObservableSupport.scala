package fi.oph.koski.servlet

import fi.oph.koski.json.Json
import org.json4s.DefaultFormats
import org.scalatra.servlet.ServletBase
import rx.lang.scala.Observable

trait ObservableSupport extends ServletBase {
  override protected def renderResponse(actionResult: Any): Unit = {
    actionResult match {
      case in: Observable[AnyRef] => writeJsonStreamSynchronously(in)
      case a => super.renderResponse(a)
    }
  }

  def writeJsonStreamSynchronously(in: Observable[_ <: AnyRef]) {
    contentType = "application/json;charset=utf-8"
    val writer = response.getWriter
    writer.print("[")
    in.zipWithIndex.toBlocking.foreach { case (item, index) =>
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
