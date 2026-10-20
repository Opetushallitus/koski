package fi.oph.koski.perftest

import scala.io.Source

// Mitätöi Valppaan vanhan testidatan opiskeluoikeudet Kosken rajapinnan kautta, jotta ne poistuvat myös
// hakuindeksistä ja raportointikannasta. Ks. documentation/suorituskykytestit.md.
// CSV-tiedosto saadaan ajamalla QA-kannan operatiiviseen tietokantaan (koski):
//   SELECT oid AS opiskeluoikeus_oid
//   FROM opiskeluoikeus
//   WHERE koulutusmuoto = 'perusopetus'
//     AND alkamispaiva = '2025-08-15'
//     AND paattymispaiva = '2026-05-31'
//     AND luokka = '9A'
//     AND NOT mitatoity;
// Tulos tallennetaan CSV-muodossa (header: opiskeluoikeus_oid).
object ValpasPeruskouluMitatoiOpiskeluoikeudet extends App {
  PerfTestRunner.executeTest(ValpasPeruskouluMitatoiOpiskeluoikeudetScenario)
}

object ValpasPeruskouluMitatoiOpiskeluoikeudetScenario extends PerfTestScenario {
  private val csvFile = "src/test/resources/" + env("VALPAS_MITATOINTI_CSV", "valpas_mitatointi.csv")

  private val opiskeluoikeusOidit: IndexedSeq[String] =
    Source.fromFile(csvFile).getLines()
      .drop(1)
      .map(_.trim)
      .filter(_.nonEmpty)
      .toIndexedSeq

  override def roundCount: Int = opiskeluoikeusOidit.length
  // Lämmittelykierrokset ajavat samat kierrokset uudelleen, ja toinen mitätöinti epäonnistuisi
  override def warmupRoundCount: Int = 0

  override def operation(x: Int) = List(Operation(
    "DELETE",
    s"api/opiskeluoikeus/${opiskeluoikeusOidit(x)}",
    uriPattern = Some("api/opiskeluoikeus/_")
  ))
}
