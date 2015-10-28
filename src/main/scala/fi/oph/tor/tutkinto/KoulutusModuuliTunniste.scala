package fi.oph.tor.tutkinto

/**
 * Tunniste, jolla suoritukset linkitetään koulutusmoduuleihin. Tässä vaiheessa linkittyy aina tutkinnon osaan ePerusteissa.
 * Luodaan KoulutusModuuliTunniste-objektin staattisilla metodeilla.
 */
case class KoulutusModuuliTunniste(tyyppi: String, koodi: String) {
  override def toString = tyyppi + " " + koodi
}

object KoulutusModuuliTunniste {
  def tutkinnonOsa(koodi: String) = KoulutusModuuliTunniste("tutkinnonosa", koodi)
}

