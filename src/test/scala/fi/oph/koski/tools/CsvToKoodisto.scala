package fi.oph.koski.tools

import java.time.LocalDate

import fi.oph.koski.json.Json
import fi.oph.koski.koodisto._
import fi.oph.koski.util.Files

// Csv format: nimi, koodiarvo

object CsvToKoodisto extends App {
  val filename = "koodistot.csv"
  val koodistoUri = "koskikoulutustendiaarinumerot"
  val koodistoNimi = "Koski - koulutusten diaarinumerot"

  private val fileContent = Files.asString(filename).get
  private val lines = fileContent.split("\n").toList
  val koodit = lines
    .map(line => line.split(",").toList)
    .map(_.map(_.trim))
    .map {
      case List(nimi: String, koodi: String) => KoodistoKoodi(KoodistoKoodi.koodiUri(koodistoUri, koodi), koodi, List(KoodistoKoodiMetadata(kieli = Some("FI"), nimi = Some(nimi))), 1, None, None)
    }
  Json.writeFile(
    MockKoodistoPalvelu.koodistoKooditFileName(koodistoUri),
    koodit
  )
  val koodisto = Koodisto(koodistoUri, 1, List(KoodistoMetadata("FI", Some(koodistoNimi), None)), "http://koski", LocalDate.now, "1.2.246.562.10.00000000001")
  Json.writeFile(
    MockKoodistoPalvelu.koodistoFileName(koodistoUri),
    koodisto
  )
}
