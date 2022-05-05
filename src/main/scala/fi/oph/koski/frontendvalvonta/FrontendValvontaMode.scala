package fi.oph.koski.frontendvalvonta

object FrontendValvontaMode extends Enumeration {
  type FrontendValvontaMode = Value
  val DISABLED, REPORT_ONLY, ENABLED = Value

  def apply(s: String): FrontendValvontaMode =
    s match {
      case "disabled" => DISABLED
      case "report-only" => REPORT_ONLY
      case _ => ENABLED
    }
}
