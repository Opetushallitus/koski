package fi.oph.koski.koodisto

case class Alakoodi(koodiUri: String, metadata: List[KoodistoKoodiMetadata], versio: Int, koodisto: KoodistoVersioilla)

case class KoodistoVersioilla(koodistoUri: String, koodistoVersios: List[Int]) {
  def latestVersion = KoodistoViite(koodistoUri, koodistoVersios.max)
}