package fi.oph.koski.koodisto

import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.log.Logging

/**
 * Sorts the contents of mockdata/koodisto/ (to get deterministic diffs etc.)
 * mvn scala:compile exec:java -Dexec.mainClass=fi.oph.koski.koodisto.KoodistoMockDataSorter
 */
object KoodistoMockDataSorter extends App with Logging {
  sortMockData()

  def sortMockData(): Unit = {
    Koodistot.koodistoAsetukset.foreach(sortMockDataForKoodisto)
    logger.info("Done")
  }

  private def sortMockDataForKoodisto(koodistoAsetus: KoodistoAsetus): Unit = {
    val koodistoFileName = MockKoodistoPalvelu.koodistoFileName(koodistoAsetus.koodisto, koodistoAsetus.koodistoVersio)
    logger.info(s"Sorting ${koodistoFileName}")
    val koodisto = JsonSerializer.extract[Koodisto](JsonFiles.readFile(koodistoFileName), ignoreExtras = true)
    val koodistoSorted = MockKoodistoPalvelu.sortKoodistoMetadata(koodisto)
    JsonFiles.writeFile(koodistoFileName, koodistoSorted)

    val kooditFileName = MockKoodistoPalvelu.koodistoKooditFileName(koodistoAsetus.koodisto, koodistoAsetus.koodistoVersio)
    logger.info(s"Sorting ${kooditFileName}")
    val koodit = JsonSerializer.extract[List[KoodistoKoodi]](JsonFiles.readFile(kooditFileName), ignoreExtras = true)
    val kooditSorted = koodit.map(MockKoodistoPalvelu.sortKoodistoKoodiMetadata).sortBy(_.koodiArvo)
    JsonFiles.writeFile(kooditFileName, kooditSorted)
  }
}
