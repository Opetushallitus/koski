package fi.oph.koski.perftest

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.{OidHenkilö, Oppija}

import scala.io.Source

// Päivittää olemassaolevien opiskeluoikeuksien tietoja tarvittaessa.
// Tee halutut muutokset opiskeluoikeuksiin ennen inserterin ajoa.
// CSV-tiedosto saadaan ajamalla QA-kannan operatiiviseen tietokantaan (koski):
//   SELECT oppija_oid, oppilaitos_oid, oid AS opiskeluoikeus_oid
//   FROM opiskeluoikeus
//   WHERE koulutusmuoto = 'perusopetus'
//     AND alkamispaiva = '2025-08-15'
//     AND paattymispaiva = '2026-05-31'
//     AND luokka = '9A';
// Tulos tallennetaan CSV-muodossa (header: oppija_oid,oppilaitos_oid,opiskeluoikeus_oid).
object ValpasPeruskouluUpdateOpiskeluoikeusInserter extends App {
  PerfTestRunner.executeTest(ValpasPeruskouluUpdateOpiskeluoikeusInserterScenario)
}

object ValpasPeruskouluUpdateOpiskeluoikeusInserterScenario extends ValpasOpiskeluoikeusInserterScenario with PerfTestScenario {
  override val readBody = true

  private val csvFile = "src/test/resources/" + env("VALPAS_UPDATE_CSV", "valpas_update.csv")

  private case class UpdateRow(oppijaOid: String, oppilaitosOid: String, opiskeluoikeusOid: String)

  private val rows: IndexedSeq[UpdateRow] = {
    Source.fromFile(csvFile).getLines()
      .drop(1)
      .map(_.trim)
      .filter(_.nonEmpty)
      .map { line =>
        val parts = line.split(",", 3)
        UpdateRow(parts(0), parts(1), parts(2))
      }
      .toIndexedSeq
  }

  override def operation(x: Int) = {
    val row = rows(x)
    val opiskeluoikeus = opiskeluoikeudetForOppilaitos(row.oppilaitosOid).head
      .copy(oid = Some(row.opiskeluoikeusOid))

    val oppija = Oppija(OidHenkilö(row.oppijaOid), List(opiskeluoikeus))
    val body = JsonSerializer.writeWithRoot(oppija).getBytes("utf-8")
    List(Operation("PUT", "api/oppija", body = body, gzip = true, responseCodes = List(200)))
  }
}
