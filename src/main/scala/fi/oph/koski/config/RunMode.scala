package fi.oph.koski.config

object RunMode extends Enumeration {
  type RunMode = Value
  val NORMAL, GENERATE_RAPORTOINTIKANTA = Value

  def get: RunMode = sys.env.get("GENERATE_RAPORTOINTIKANTA") match {
    case Some(_) => GENERATE_RAPORTOINTIKANTA
    case None => NORMAL
  }

  def fullReload: Boolean = sys.env.get("GENERATE_RAPORTOINTIKANTA").contains("full")
}
