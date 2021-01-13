package fi.oph.common.log

object LogUtils {
  def maskSensitiveInformation(s: String): String = {
    s.replaceAll("\\b[0-9]{6}[-A+][0-9]{3}[0-9A-Z]\\b", "******-****")
  }
}
