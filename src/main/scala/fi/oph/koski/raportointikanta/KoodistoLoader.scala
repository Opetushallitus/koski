package fi.oph.koski.raportointikanta

import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu}
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.LoaderUtils.convertLocalizedString

object KoodistoLoader extends Logging {
  private val BatchSize = 1000
  private val LadattavatKoodistot = List("opiskeluoikeudentyyppi", "oppilaitostyyppi", "koulutustyyppi", "kunta", "suorituksentyyppi")

  def loadKoodistot(koodistoPalvelu: KoodistoPalvelu, raportointiDatabase: RaportointiDatabase): Int = {
    logger.info("Ladataan koodistoja...")
    var count = 0
    LadattavatKoodistot.foreach(koodistoUri => {
      val versio = koodistoPalvelu.getLatestVersion(koodistoUri).get
      val koodit = koodistoPalvelu.getKoodistoKoodit(versio).get
      val rows = koodit.map(buildRKoodistoKoodiRow)
      raportointiDatabase.deleteKoodistoKoodit(koodistoUri)
      raportointiDatabase.loadKoodistoKoodit(rows)
      count += rows.length
    })
    logger.info(s"Ladattiin $count koodiarvoa")
    count
  }

  private def buildRKoodistoKoodiRow(koodi: KoodistoKoodi) =
    RKoodistoKoodiRow(
      koodistoUri = koodi.koodistoUri,
      koodiarvo = koodi.koodiArvo,
      nimi = convertLocalizedString(koodi.nimi)
    )
}
