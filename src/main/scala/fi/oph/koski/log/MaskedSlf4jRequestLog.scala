package fi.oph.koski.log

import java.io.IOException
import org.eclipse.jetty.server.Slf4jRequestLog

class MaskedSlf4jRequestLog extends Slf4jRequestLog {
  @throws[IOException]
  override def write(requestEntry: String): Unit = {
    super.write(maskSensitiveInformation(requestEntry))
  }

  private def maskSensitiveInformation(s: String): String = {
    s
      .replaceAll("(/koski/opinnot/[0-9a-f]{8})([0-9a-f]+)", "$1************************")
      .replaceAll("(/koski/api/henkilo/hetu/)(\\S+)", "$1*")
      .replaceAll("(/koski/api/henkilo/search\\?query=)(\\S+)", "$1*")
  }
}
