package fi.oph.koski.log

object LogUtils {
  val HETU_MASK = "******-****"

  def maskSensitiveInformation(s: String): String = {
    s.replaceAll("\\b[0-9]{6}[-Aa+][0-9]{3}[0-9A-Za-z]\\b", HETU_MASK)
  }
}
