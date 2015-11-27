package fi.oph.tor.tutkinto

object Koulutustyyppi {
  type Koulutustyyppi = Int

  def fromEPerusteetKoulutustyyppiAndSuoritustapa(ePerusteetKoulutustyyppi: String, suoritustapa: String): Koulutustyyppi = {
    if (ePerusteetKoulutustyyppi == "koulutustyyppi_1" && suoritustapa == "naytto") {
      13
    } else {
      ePerusteetKoulutustyyppi.substring(15).toInt
    }
  }
}
