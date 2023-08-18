package fi.oph.koski.log

import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.{Plugin, PluginAttribute, PluginElement, PluginFactory}
import org.apache.logging.log4j.core._

import scala.collection.mutable.ListBuffer

object StubAppender {
  @PluginFactory
  def createAppender[T <: Serializable](
    @PluginAttribute("name") name: String,
    @PluginElement("Layout") layout: Layout[T],
    @PluginElement("Filter") filter: Filter,
  ): StubAppender[T] = {
    new StubAppender(name, layout, filter)
  }
}

@Plugin(name = "Stub", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
class StubAppender[T <: Serializable] protected (
  val name: String,
  val layout: Layout[T],
  val filter: Filter,
) extends AbstractAppender(name, filter, layout, true, Array.empty) {
  override def append(event: LogEvent): Unit = {
    StubLogs.append(name, toSerializable(event).toString)
  }
}

object StubLogs {
  private val logs: ListBuffer[Message] = ListBuffer.empty
  val maxSize = 200

  def clear(): Unit = logs.synchronized { logs.clear() }
  def getLogs(appenderName: String): Seq[String] = logs.synchronized { logs.filter(_.appenderName == appenderName).map(_.message) }
  def append(appenderName: String, message: String): Unit = {
    logs.synchronized {
      logs += Message(appenderName, message)
      if (logs.size > maxSize) {
        logs.trimStart(logs.size - maxSize)
      }
    }
  }
}

case class Message(
  appenderName: String,
  message: String,
)
