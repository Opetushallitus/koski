package fi.oph.koski.config

object RunMode extends Enumeration {
  type RunMode = Value
  val NORMAL, GENERATE_RAPORTOINTIKANTA = Value

  def get: RunMode = sys.env.get("GENERATE_RAPORTOINTIKANTA") match {
    case Some("full") => GENERATE_RAPORTOINTIKANTA
    case Some("update") => GENERATE_RAPORTOINTIKANTA
    case Some(s) => throw new RuntimeException(s"Odottaman arvo muuttujalla GENERATE_RAPORTOINTIKANTA: ${s} (sallitut arvot: full, update)")
    case None => NORMAL
  }

  def isFullReload: Boolean = sys.env.get("GENERATE_RAPORTOINTIKANTA").contains("full")
}
