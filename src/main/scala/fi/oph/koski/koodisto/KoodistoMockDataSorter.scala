package fi.oph.koski.koodisto

import fi.oph.common.koodisto.{Koodisto, KoodistoKoodi}
import fi.oph.common.log.Logging
import fi.oph.koski.json.{JsonFiles, JsonSerializer}

/**
 * Sorts the contents of mockdata/koodisto/ (to get deterministic diffs etc.)
 * mvn scala:compile exec:java -Dexec.mainClass=fi.oph.koski.koodisto.KoodistoMockDataSorter
 */
object KoodistoMockDataSorter extends App with Logging {
  sortMockData()

  def sortMockData(): Unit = {
    Koodistot.koodistot.foreach(sortMockDataForKoodisto)
    logger.info("Done")
  }

  private def sortMockDataForKoodisto(koodistoUri: String): Unit = {
    val koodistoFileName = MockKoodistoPalvelu.koodistoFileName(koodistoUri)
    logger.info(s"Sorting ${koodistoFileName}")
    val koodisto = JsonSerializer.extract[Koodisto](JsonFiles.readFile(koodistoFileName), ignoreExtras = true)
    val koodistoSorted = MockKoodistoPalvelu.sortKoodistoMetadata(koodisto)
    JsonFiles.writeFile(koodistoFileName, koodistoSorted)

    val kooditFileName = MockKoodistoPalvelu.koodistoKooditFileName(koodistoUri)
    logger.info(s"Sorting ${kooditFileName}")
    val koodit = JsonSerializer.extract[List[KoodistoKoodi]](JsonFiles.readFile(kooditFileName), ignoreExtras = true)
    val kooditSorted = koodit.map(MockKoodistoPalvelu.sortKoodistoKoodiMetadata).sortBy(_.koodiArvo)
    JsonFiles.writeFile(kooditFileName, kooditSorted)
  }
}
