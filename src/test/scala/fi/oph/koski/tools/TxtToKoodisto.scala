package fi.oph.koski.tools

import java.nio.charset.StandardCharsets
import java.time.LocalDate

import fi.oph.koski.json.Json
import fi.oph.koski.koodisto._
import fi.oph.koski.util.Files

// text format: xx yy
// where xx == koodiarvo, yy = nimi

object TxtToKoodisto extends App {
  val filename = "aikuisten_perusopetus_alkuvaihe_2017.txt"
  val koodistoUri = "aikuistenperusopetuksenalkuvaiheenkurssit2017"
  val koodistoNimi = "Aikuisten perusopetuksen alkuvaiheen kurssit 2017"

  private val fileContent = Files.asString(filename, StandardCharsets.ISO_8859_1).get
  private val lines = fileContent.split("\n").toList
  val koodit = lines
    .map(line => line.split(" ").toList)
    .filter(words => words.length >= 2)
    .map(_.map(_.trim))
    .map {
      case arvo :: nimenosat =>
        val koodiarvo = arvo.toUpperCase
        KoodistoKoodi(KoodistoKoodi.koodiUri(koodistoUri, koodiarvo), koodiarvo, List(KoodistoKoodiMetadata(kieli = Some("FI"), nimi = Some(nimenosat.mkString(" ")))), 1, None, None)
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
