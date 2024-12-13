package fi.oph.koski.koskiuser

case class Palvelurooli(palveluName: String, rooli: String) {
  def toOmaDataOAuth2Scope(): Option[String] = {
    val isOmaDataOAuth2Rooli = rooli.startsWith(Rooli.omadataOAuth2Prefix)
    if (isOmaDataOAuth2Rooli) {
      Some(rooli.substring(Rooli.omadataOAuth2Prefix.length))
    } else {
      None
    }
  }
}

object Palvelurooli {
  def apply(rooli: String): Palvelurooli = Palvelurooli("KOSKI", rooli)
}
