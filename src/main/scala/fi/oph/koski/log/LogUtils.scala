package fi.oph.koski.log

object LogUtils {
  val HETU_MASK = "******-****"
  val OPPIJA_OID_MASK = "***"

  private val maskOppijaOids: Boolean = sys.env.contains("CI")

  def maskSensitiveInformation(s: String): String = {
    val hetuMasked = s.replaceAll("[0-9]{6}[-AaBbCcDdEeFfXxYyWwVvUu+][0-9]{3}[0-9A-Za-z]\\b", HETU_MASK)
    if (maskOppijaOids) {
      hetuMasked.replaceAll("""1\.2\.246\.562\.24\.\d+""", OPPIJA_OID_MASK)
    } else {
      hetuMasked
    }
  }
}
