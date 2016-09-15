package fi.oph.koski.log

import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{AppenderSkeleton, Logger}

import scala.collection.mutable.ListBuffer

trait LogTester {
  private var messages: ListBuffer[LoggingEvent] = ListBuffer.empty[LoggingEvent]

  lazy val setup: Unit = {
    getLogger.addAppender(new AppenderSkeleton() {
      override def append(event: LoggingEvent) = this.synchronized {
        messages += event
      }

      override def requiresLayout = false
      override def close() {}
    })
  }

  def getLogMessages: List[LoggingEvent] = this.synchronized(messages.toList)
  def getLogger: Logger = Logger.getRootLogger
}
