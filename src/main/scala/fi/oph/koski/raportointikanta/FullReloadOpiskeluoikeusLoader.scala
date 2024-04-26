package fi.oph.koski.raportointikanta

import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, OpiskeluoikeusRow, YtrOpiskeluoikeusRow}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.suostumus.SuostumuksenPeruutusService
import rx.lang.scala.Observable

class FullReloadOpiskeluoikeusLoader(
  opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService,
  suostumuksenPeruutusService: SuostumuksenPeruutusService,
  organisaatioRepository: OrganisaatioRepository,
  val db: RaportointiDatabase,
  enableYtr: Boolean = true,
  batchSize: Int = OpiskeluoikeusLoader.DefaultBatchSize,
  onAfterPage: (Int, Seq[OpiskeluoikeusRow]) => Unit = (_, _) => (),
) extends OpiskeluoikeusLoader {

  def loadOpiskeluoikeudet(): Observable[LoadResult] = {
    setStatusStarted()

    var loopCount = 0
    var loadBatchStartTime = System.nanoTime()

    val dataResult =
      opiskeluoikeudetSivuittainWithoutAccessCheck(batchSize, enableYtr, opiskeluoikeusQueryRepository)
        .filter(!_.isEmpty)
        .flatMap(batch => {
          val newLoadBatchStartTime = System.nanoTime()
          logger.info(s"Opiskeluoikeuserän lataaminen kesti ${(newLoadBatchStartTime - loadBatchStartTime) / 1000000} ms")

          val koskiBatch = batch.collect { case r: KoskiOpiskeluoikeusRow => r }
          val ytrBatch = batch.collect { case r: YtrOpiskeluoikeusRow => r }

          val results = (koskiBatch, ytrBatch) match {
            case (_, Seq()) =>
              loadKoskiBatch(koskiBatch)
            case (Seq(), _) =>
              loadYtrBatch(ytrBatch)
            case _ =>
              throw new InternalError("Tuntematon tilanne, samassa batchissä YTR- ja Koski-opiskeluoikeuksia")
          }

          onAfterPage(loopCount, batch)
          loopCount = loopCount + 1

          loadBatchStartTime = System.nanoTime()
          Observable.from(results)
        })

    val result = dataResult.doOnCompleted {
      // Last batch processed; finalize
      createIndexesForIncrementalUpdate()
      createIndexes()
      setStatusCompleted()
    } ++ Observable.from(Seq(LoadCompleted()))

    result.doOnEach(progressLogger)
  }

  private def loadKoskiBatch(
    batch: Seq[KoskiOpiskeluoikeusRow]
  ): Seq[LoadResult] = {
    val (mitätöidytOot, olemassaolevatOot) = batch.partition(_.mitätöity)
    val (poistetutOot, mitätöidytEiPoistetutOot) = mitätöidytOot.partition(_.poistettu)

    val resultOlemassaolevatOot = loadKoskiBatchOlemassaolevatOpiskeluoikeudet(olemassaolevatOot)
    val resultMitätöidyt = loadKoskiBatchMitätöidytOpiskeluoikeudet(mitätöidytEiPoistetutOot)
    val resultPoistetut = loadKoskiBatchPoistetutOpiskeluoikeudet(poistetutOot)

    resultOlemassaolevatOot ++ resultMitätöidyt ++ resultPoistetut
  }

  private def loadYtrBatch(
    batch: Seq[YtrOpiskeluoikeusRow]
  ): Seq[LoadResult] = {
    val (mitätöidytOot, olemassaolevatOot) = batch.partition(_.mitätöity)

    val resultOlemassaolevatOot = loadYtrBatchOlemassaolevatOpiskeluoikeudet(olemassaolevatOot)

    // Mitätöityjä ei (toistaiseksi) käsitellä, koska sellaisia ei YTR-datassa voi olla.

    resultOlemassaolevatOot
  }

  private def loadKoskiBatchOlemassaolevatOpiskeluoikeudet(oot: Seq[KoskiOpiskeluoikeusRow]): Seq[LoadResult] = {
    val loadBatchStartTime = System.nanoTime()

    val (errors, outputRows) = oot.par
      .map(row => OpiskeluoikeusLoaderRowBuilder.buildKoskiRow(row))
      .seq
      .partition(_.isLeft)

    db.loadOpiskeluoikeudet(outputRows.map(_.right.get.rOpiskeluoikeusRow))
    db.loadOrganisaatioHistoria(outputRows.flatMap(_.right.get.organisaatioHistoriaRows))
    val aikajaksoRows = outputRows.flatMap(_.right.get.rOpiskeluoikeusAikajaksoRows)
    val esiopetusOpiskeluoikeusAikajaksoRows = outputRows.flatMap(_.right.get.esiopetusOpiskeluoikeusAikajaksoRows)
    val päätasonSuoritusRows = outputRows.flatMap(_.right.get.rPäätasonSuoritusRows)
    val osasuoritusRows = outputRows.flatMap(_.right.get.rOsasuoritusRows)
    val muuAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.muuAmmatillinenOsasuoritusRaportointiRows)
    val topksAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.topksAmmatillinenRaportointiRows)
    db.loadOpiskeluoikeusAikajaksot(aikajaksoRows)
    db.loadEsiopetusOpiskeluoikeusAikajaksot(esiopetusOpiskeluoikeusAikajaksoRows)
    db.loadPäätasonSuoritukset(päätasonSuoritusRows)
    db.loadOsasuoritukset(osasuoritusRows)
    db.loadMuuAmmatillinenRaportointi(muuAmmatillinenRaportointiRows)
    db.loadTOPKSAmmatillinenRaportointi(topksAmmatillinenRaportointiRows)
    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, outputRows.size)
    val result = errors.map(_.left.get) :+ LoadProgressResult(outputRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    val toOpiskeluoikeusUnsafeDuration: Long = outputRows.map(_.right.get.toOpiskeluoikeusUnsafeDuration).sum / 1000000
    logger.info(s"Koski batchin käsittely kesti ${loadBatchDuration} ms, jossa toOpiskeluOikeusUnsafe ${toOpiskeluoikeusUnsafeDuration} ms.")
    result
  }

  private def loadYtrBatchOlemassaolevatOpiskeluoikeudet(oot: Seq[YtrOpiskeluoikeusRow]): Seq[LoadResult] = {
    val loadBatchStartTime = System.nanoTime()

    val (errors, outputRows) = oot.par
      .map(row => OpiskeluoikeusLoaderRowBuilder.buildYtrRow(row))
      .seq
      .partition(_.isLeft)

    db.loadOpiskeluoikeudet(outputRows.map(_.right.get.rOpiskeluoikeusRow))
    val päätasonSuoritusRows = outputRows.flatMap(_.right.get.rPäätasonSuoritusRows)
    val tutkintokokonaisuudenSuoritusRows = outputRows.flatMap(_.right.get.rTutkintokokonaisuudenSuoritusRows)
    val tutkintokerranSuoritusRows = outputRows.flatMap(_.right.get.rTutkintokerranSuoritusRows)
    val kokeenSuoritusRows = outputRows.flatMap(_.right.get.rKokeenSuoritusRows)
    val tutkintokokonaisuudenKokeenSuoritusRows = outputRows.flatMap(_.right.get.rTutkintokokonaisuudenKokeenSuoritusRows)
    db.loadPäätasonSuoritukset(päätasonSuoritusRows)
    db.loadYtrOsasuoritukset(
      tutkintokokonaisuudenSuoritusRows,
      tutkintokerranSuoritusRows,
      kokeenSuoritusRows,
      tutkintokokonaisuudenKokeenSuoritusRows
    )

    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, outputRows.size)

    val result = errors.map(_.left.get) :+
      LoadProgressResult(outputRows.size,
        päätasonSuoritusRows.size +
          tutkintokokonaisuudenSuoritusRows.size +
          tutkintokerranSuoritusRows.size +
          kokeenSuoritusRows.size
      )

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    val toOpiskeluoikeusUnsafeDuration: Long = outputRows.map(_.right.get.toOpiskeluoikeusUnsafeDuration).sum / 1000000
    logger.info(s"YTR batchin käsittely kesti ${loadBatchDuration} ms, jossa toOpiskeluOikeusUnsafe ${toOpiskeluoikeusUnsafeDuration} ms.")
    result
  }

  private def loadKoskiBatchMitätöidytOpiskeluoikeudet(oot: Seq[KoskiOpiskeluoikeusRow]) = {
    val loadBatchStartTime = System.nanoTime()
    val (errors, outputRows) = oot.par.filterNot(_.poistettu).map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity).seq.partition(_.isLeft)
    db.loadMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get))
    db.updateStatusCount(mitätöidytStatusName, outputRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    logger.info(s"Koski batchin käsittely mitätöidyille opiskeluoikeuksille kesti ${loadBatchDuration} ms")

    errors.map(_.left.get)
  }

  private def loadKoskiBatchPoistetutOpiskeluoikeudet(
    oot: Seq[KoskiOpiskeluoikeusRow]
  ): Seq[LoadErrorResult] = {
    if (oot.nonEmpty) {
      val loadBatchStartTime = System.nanoTime()
      val (errors, outputRows) = suostumuksenPeruutusService
        .etsiPoistetut(oot.map(_.oid))
        .map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity(organisaatioRepository))
        .partition(_.isLeft)
      db.loadMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get))
      db.updateStatusCount(mitätöidytStatusName, outputRows.size)

      val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
      logger.info(s"Koski batchin käsittely poistetuille opiskeluoikeuksille kesti ${loadBatchDuration} ms")

      errors.map(_.left.get)
    } else {
      Seq.empty
    }
  }

  def opiskeluoikeudetSivuittainWithoutAccessCheck
    (
      pageSize: Int,
      enableYtr: Boolean,
      opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService,
    )
  : Observable[Seq[OpiskeluoikeusRow]] =
    if (enableYtr) {
      opiskeluoikeusQueryRepository.koskiJaYtrOpiskeluoikeudetSivuittainWithoutAccessCheck(pageSize)
    } else {
      opiskeluoikeusQueryRepository.koskiOpiskeluoikeudetSivuittainWithoutAccessCheck(pageSize)
    }

}
