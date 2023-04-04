package fi.oph.koski.raportointikanta

import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, OpiskeluoikeusRow}
import fi.oph.koski.opiskeluoikeus.PäivitetytOpiskeluoikeudetJonoService
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.suostumus.SuostumuksenPeruutusService
import fi.oph.koski.util.TimeConversions.toTimestamp
import rx.lang.scala.Observable

import java.time.{LocalDateTime, ZonedDateTime}
import scala.concurrent.duration.FiniteDuration

class IncrementalUpdateOpiskeluoikeusLoader(
  suostumuksenPeruutusService: SuostumuksenPeruutusService,
  val db: RaportointiDatabase,
  enableYtr: Boolean = true,
  update: RaportointiDatabaseUpdate,
  batchSize: Int = OpiskeluoikeusLoader.DefaultBatchSize,
  onAfterPage: (Int, Seq[OpiskeluoikeusRow]) => Unit = (_, _) => (),
) extends OpiskeluoikeusLoader {

  def loadOpiskeluoikeudet(): Observable[LoadResult] = {
    setStatusStarted(Some(toTimestamp(update.dueTime)))

    var loopCount = 0

    update.service.alustaKaikkiKäsiteltäviksi()
    db.cloneUpdateableTables(update.previousRaportointiDatabase, enableYtr)
    createIndexesForIncrementalUpdate()
    OpiskeluoikeusLoaderRowBuilder.suoritusIds.set(db.getLatestSuoritusId)

    val dataResult =
      update.loader.load(batchSize, update) { koskiBatch =>
        val loadResults = if (koskiBatch.nonEmpty) {
          val result = updateBatch(koskiBatch)

          onAfterPage(loopCount, koskiBatch)
          loopCount = loopCount + 1
          result
        } else {
          Seq.empty
        }

        loadResults
      }

    val result = dataResult.doOnCompleted {
      // Last batch processed; finalize
      createIndexes()
      setStatusCompleted()
    } ++ Observable.from(Seq(LoadCompleted()))

    result.doOnEach(progressLogger)
  }

  private def updateBatch(
    batch: Seq[KoskiOpiskeluoikeusRow]
  ): Seq[LoadResult] = {
    val (mitätöidytOot, olemassaolevatOot) = batch.partition(_.mitätöity)
    val (poistetutOot, mitätöidytEiPoistetutOot) = mitätöidytOot.partition(_.poistettu)

    val resultOlemassaolevatOot = updateBatchOlemassaolevatOpiskeluoikeudet(olemassaolevatOot, mitätöidytOot.map(_.oid))
    val resultMitätöidyt = updateBatchMitätöidytOpiskeluoikeudet(mitätöidytEiPoistetutOot, olemassaolevatOot.map(_.oid))
    val resultPoistetut = updateBatchPoistetutOpiskeluoikeudet(poistetutOot)

    resultOlemassaolevatOot ++ resultMitätöidyt ++ resultPoistetut
  }

  private def updateBatchOlemassaolevatOpiskeluoikeudet(
    oot: Seq[KoskiOpiskeluoikeusRow],
    mitätöidytOot: Seq[Opiskeluoikeus.Oid],
  ) = {
    val loadBatchStartTime = System.nanoTime()

    val (errors, outputRows) = oot.par
      .map(row => OpiskeluoikeusLoaderRowBuilder.buildKoskiRow(row))
      .seq
      .partition(_.isLeft)

    db.updateOpiskeluoikeudet(outputRows.map(_.right.get.rOpiskeluoikeusRow), mitätöidytOot)
    db.updateOrganisaatioHistoria(outputRows.flatMap(_.right.get.organisaatioHistoriaRows))

    val aikajaksoRows = outputRows.flatMap(_.right.get.rOpiskeluoikeusAikajaksoRows)
    db.updateOpiskeluoikeusAikajaksot(aikajaksoRows)

    val esiopetusOpiskeluoikeusAikajaksoRows = outputRows.flatMap(_.right.get.esiopetusOpiskeluoikeusAikajaksoRows)
    db.updateEsiopetusOpiskeluoikeusAikajaksot(esiopetusOpiskeluoikeusAikajaksoRows)

    val päätasonSuoritusRows = outputRows.flatMap(_.right.get.rPäätasonSuoritusRows)
    db.updatePäätasonSuoritukset(päätasonSuoritusRows)

    val osasuoritusRows = outputRows.flatMap(_.right.get.rOsasuoritusRows)
    db.updateOsasuoritukset(osasuoritusRows)

    val muuAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.muuAmmatillinenOsasuoritusRaportointiRows)
    db.updateMuuAmmatillinenRaportointi(muuAmmatillinenRaportointiRows)

    val topksAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.topksAmmatillinenRaportointiRows)
    db.updateTOPKSAmmatillinenRaportointi(topksAmmatillinenRaportointiRows)

    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, outputRows.size)
    val result = errors.map(_.left.get) :+ LoadProgressResult(outputRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    val toOpiskeluoikeusUnsafeDuration: Long = outputRows.map(_.right.get.toOpiskeluoikeusUnsafeDuration).sum / 1000000
    logger.info(s"Batchin käsittely kesti ${loadBatchDuration} ms, jossa toOpiskeluOikeusUnsafe ${toOpiskeluoikeusUnsafeDuration} ms.")
    result
  }

  private def updateBatchMitätöidytOpiskeluoikeudet(
    oot: Seq[KoskiOpiskeluoikeusRow],
    olemassaolevatOot: Seq[Opiskeluoikeus.Oid],
  ) = {
    val (errors, outputRows) = oot.par.filterNot(_.poistettu).map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity).seq.partition(_.isLeft)
    db.updateMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get), olemassaolevatOot)
    db.updateStatusCount(mitätöidytStatusName, outputRows.size)
    errors.map(_.left.get)
  }

  private def updateBatchPoistetutOpiskeluoikeudet(
    oot: Seq[KoskiOpiskeluoikeusRow]
  ): Seq[LoadErrorResult] = {
    if (oot.nonEmpty) {
      val (errors, outputRows) = suostumuksenPeruutusService
        .etsiPoistetut(oot.map(_.oid))
        .map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity)
        .partition(_.isLeft)
      db.updateMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get), Seq.empty)
      db.updateStatusCount(mitätöidytStatusName, outputRows.size)
      errors.map(_.left.get)
    } else {
      Seq.empty
    }
  }
}

case class RaportointiDatabaseUpdate(
  previousRaportointiDatabase: RaportointiDatabase,
  readReplicaDb: DB,
  dueTime: ZonedDateTime,
  sleepDuration: FiniteDuration,
  service: PäivitetytOpiskeluoikeudetJonoService,
) {
  def dueTimeExpired: Boolean = {
    dueTime.toLocalDateTime.isBefore(LocalDateTime.now())
  }

  val loader: PäivitettyOpiskeluoikeusLoader =
    new PäivitettyOpiskeluoikeusLoader(readReplicaDb, service)
}
