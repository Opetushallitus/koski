package fi.oph.koski.log

object LogUtils {
  val HETU_MASK = "******-****"

  def maskSensitiveInformation(s: String): String = {
    s.replaceAll("[0-9]{6}[-AaBbCcDdEeFfXxYyWwVvUu+][0-9]{3}[0-9A-Za-z]\\b", HETU_MASK)
  }
}
