package fi.oph.koski.log

import fi.oph.koski.log.LogMaskingPatternConverter._
import fi.oph.koski.log.LogUtils.maskSensitiveInformation
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.{ConverterKeys, LogEventPatternConverter}

import java.lang

// LogMaskingPatternConverter siivoaa hetut käytettäessä PatternLayoutia

object LogMaskingPatternConverter {
  val NAME: String = "cm"
  def newInstance(options: Array[String]) = new LogMaskingPatternConverter
}

@Plugin(name = "maskString", category = "Converter")
@ConverterKeys(Array("cm"))
class LogMaskingPatternConverter extends LogEventPatternConverter(NAME, NAME) {
  override def format(event: LogEvent, outputMessage: lang.StringBuilder): Unit = {
    val message = cutToMaxLength(maskSensitiveInformation(event.getMessage.getFormattedMessage))
    outputMessage.append(message)
  }

  private def cutToMaxLength(msg: => String) = {
    if (msg.length > LogConfiguration.logMessageMaxLength) {
      msg.take(LogConfiguration.logMessageMaxLength - 3) + "..."
    } else {
      msg
    }
  }
}
