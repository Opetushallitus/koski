package fi.oph.koski.config

object RunMode extends Enumeration {
  type RunMode = Value
  val NORMAL, GENERATE_RAPORTOINTIKANTA, DOWNLOAD_YTR = Value

  def get: RunMode = (sys.env.get("GENERATE_RAPORTOINTIKANTA"), sys.env.get("DOWNLOAD_YTR")) match {
    case (Some("full"), _) => GENERATE_RAPORTOINTIKANTA
    case (Some("update"), _) => GENERATE_RAPORTOINTIKANTA
    case (Some(s), _) => throw new RuntimeException(s"Odottaman arvo muuttujalla GENERATE_RAPORTOINTIKANTA: ${s} (sallitut arvot: full, update)")
    case (_, Some("full")) => DOWNLOAD_YTR
    case (_, Some(s)) => throw new RuntimeException(s"Odottaman arvo muuttujalla DOWNLOAD_YTR: ${s} (sallitut arvot: full)")
    case (None, None) => NORMAL
  }

  def isFullReload: Boolean = sys.env.get("GENERATE_RAPORTOINTIKANTA").contains("full")
}
