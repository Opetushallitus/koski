package fi.oph.koski.log

import java.io.IOException
import org.eclipse.jetty.server.Slf4jRequestLogWriter

object MaskedSlf4jRequestLogWriter {
  def maskSensitiveInformation(s: String): String = {
    maskSensitiveInformationAPIUris(
      maskSensitiveInformationFrontendUris(s)
    )
  }

  def maskSensitiveInformationFrontendUris(s: String): String = {
    s
      .replaceAll("(/koski/opinnot/[0-9a-f]{8})([0-9a-f]+)", "$1************************")
      .replaceAll("(/koski/kela/)(\\d\\S+)", "$1******-****")
  }

  def maskSensitiveInformationAPIUris(s: String): String = {
    s
      .replaceAll("(/koski/api/henkilo/hetu/)(\\S+)", "$1*")
      .replaceAll("(/koski/api/henkilo/search\\?query=)(\\S+)", "$1*")
      .replaceAll("(/koski/api/henkilo/search\\?query=)(\\S+)", "$1*")
      .replaceAll("(/koski/api/luovutuspalvelu/valvira/)(\\S+)", "$1******-****")
      .replaceAll("(/koski/valpas/api/henkilohaku/suorittaminen/)(\\d\\S+)", "$1******-****")
      .replaceAll("(/koski/valpas/api/henkilohaku/maksuttomuus/)(\\d\\S+)", "$1******-****")
      .replaceAll("(/koski/valpas/api/henkilohaku/kunta/)(\\d\\S+)", "$1******-****")
  }
}

class MaskedSlf4jRequestLogWriter extends Slf4jRequestLogWriter {
  @throws[IOException]
  override def write(requestEntry: String): Unit = {
    super.write(MaskedSlf4jRequestLogWriter.maskSensitiveInformation(requestEntry))
  }
}
