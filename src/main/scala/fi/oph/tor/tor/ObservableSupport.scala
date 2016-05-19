package fi.oph.tor.tor

import fi.oph.tor.json.Json
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
      writer.print(org.json4s.jackson.Serialization.write(item)(Json.jsonFormats))
    }
    writer.print("]")
    writer.flush
    writer.close
  }
}
