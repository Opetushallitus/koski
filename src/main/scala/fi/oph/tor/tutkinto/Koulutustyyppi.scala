package fi.oph.tor.tutkinto

object Koulutustyyppi {
  type Koulutustyyppi = Int

  def fromEPerusteetKoulutustyyppiAndSuoritustapa(ePerusteetKoulutustyyppi: String, suoritustapa: Suoritustapa): Koulutustyyppi = {
    if (ePerusteetKoulutustyyppi == "koulutustyyppi_1" && suoritustapa == Suoritustapa.naytto) {
      13
    } else {
      ePerusteetKoulutustyyppi.substring(15).toInt
    }
  }
}
