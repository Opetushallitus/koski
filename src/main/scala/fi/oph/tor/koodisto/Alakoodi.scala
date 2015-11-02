package fi.oph.tor.koodisto

case class Alakoodi(koodiUri: String, metadata: List[KoodistoKoodiMetadata], versio: Int, koodisto: KoodistoVersioilla)

case class KoodistoVersioilla(koodistoUri: String, koodistoVersios: List[Int]) {
  def latestVersion = KoodistoViittaus(koodistoUri, koodistoVersios.max)
}