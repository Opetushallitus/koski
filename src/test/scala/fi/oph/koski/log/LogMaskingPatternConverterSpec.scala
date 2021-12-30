package fi.oph.koski.log

import fi.oph.koski.TestEnvironment
import fi.oph.koski.log.LogUtils.HETU_MASK
import org.json4s.{JObject, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.json4s.jackson.JsonMethods.parse

class LogMaskingPatternConverterSpec extends AnyFreeSpec with TestEnvironment with Logging with Matchers {
  "LogMaskingPatternConverterSpec" - {
    "Ilman hetujen maskausta" - {
      "PatternLayout: %m" in {
        logger.info("Hetu: 010101-0101")
        latestOriginalMessage should equal("Hetu: 010101-0101")
      }
      "JsonTemplateLayout: LogstashJsonEventLayoutV1.json" in {
        logger.info("Hetu: 030303-0303")
        latestJsonMessage should equal("Hetu: 030303-0303")
      }
    }
    "Hetujen maskaus" - {
      "PatternLayout: %cm" in {
        logger.info("Hetu: 020202-0202")
        latestMaskedMessage should equal(s"Hetu: ${HETU_MASK}")
      }
      "JsonTemplateLayout: MaskedLogstashJsonEventLayoutV1.json" in {
        logger.info("Hetu: 040404-0404")
        latestMaskedJsonMessage should equal(s"Hetu: ${HETU_MASK}")
      }
    }
  }

  private def latestOriginalMessage = StubLogs.getLogs("PluginTest").last
  private def latestMaskedMessage = StubLogs.getLogs("PluginTestMasked").last
  private def latestJsonMessage = latestJSONMessage("PluginTestJSON")
  private def latestMaskedJsonMessage = latestJSONMessage("PluginTestMaskedJSON")
  private def latestJSONMessage(appenderName: String) = {
    parse(StubLogs.getLogs(appenderName).last) match {
      case o: JObject => o.values("message").toString
      case o: Any => o.toString
    }
  }
}
