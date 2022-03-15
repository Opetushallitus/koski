package fi.oph.koski.raportointikanta

import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu}
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.LoaderUtils.convertLocalizedString

object KoodistoLoader extends Logging {
  private val LadattavatKoodistot = List(
    "opiskeluoikeudentyyppi",
    "oppilaitostyyppi",
    "koulutustyyppi",
    "kunta",
    "suorituksentyyppi",
    "oppilaitoksenopetuskieli",
    "opintojenlaajuusyksikko",
    "kieli"
  )
  private val name = "koodistot"

  def loadKoodistot(koodistoPalvelu: KoodistoPalvelu, db: RaportointiDatabase): Int = {
    logger.info("Ladataan koodistoja...")
    db.setStatusLoadStarted(name)
    var count = 0
    LadattavatKoodistot.foreach(koodistoUri => {
      val versio = koodistoPalvelu.getLatestVersionRequired(koodistoUri)
      val koodit = koodistoPalvelu.getKoodistoKoodit(versio)
      val rows = koodit.map(buildRKoodistoKoodiRow)
      db.loadKoodistoKoodit(rows)
      db.setLastUpdate(name)
      count += rows.length
    })
    db.setStatusLoadCompletedAndCount(name, count)
    logger.info(s"Ladattiin $count koodiarvoa")
    count
  }

  private def buildRKoodistoKoodiRow(koodi: KoodistoKoodi) =
    RKoodistoKoodiRow(
      koodistoUri = koodi.koodistoUri,
      koodiarvo = koodi.koodiArvo,
      nimi = convertLocalizedString(koodi.nimi, "fi"),
      nimiSv = convertLocalizedString(koodi.nimi, "sv")
    )
}
