package fi.oph.koski.config

object RunMode extends Enumeration {
  type RunMode = Value
  val NORMAL, GENERATE_RAPORTOINTIKANTA, REMOVE_LAAJUUDET = Value

  def get: RunMode = sys.env.get("GENERATE_RAPORTOINTIKANTA") match {
    case Some(_) => GENERATE_RAPORTOINTIKANTA
    case None => sys.env.get("REMOVE_LAAJUUDET") match {
      case Some(_) => REMOVE_LAAJUUDET
      case None => NORMAL
    }
  }
}
