package fi.oph.koski.raportointikanta

import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, OpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.db.KoskiTables.Henkilöt
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.KoskiHenkilöCache
import fi.oph.koski.opiskeluoikeus.PäivitetytOpiskeluoikeudetJonoService
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.raportointikanta.OpiskeluoikeusLoader.isRaportointikantaanSiirrettäväOpiskeluoikeus
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.suostumus.SuostumuksenPeruutusService
import fi.oph.koski.util.TimeConversions.toTimestamp
import rx.lang.scala.Observable

import java.time.{LocalDateTime, ZonedDateTime}
import scala.concurrent.duration.FiniteDuration
import scala.collection.parallel.CollectionConverters._

class IncrementalUpdateOpiskeluoikeusLoader(
  suostumuksenPeruutusService: SuostumuksenPeruutusService,
  organisaatioRepository: OrganisaatioRepository,
  henkilöCache: KoskiHenkilöCache,
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
    val (mitätöidytOot, kaikkiOlemassaolevatOot) = batch.partition(_.mitätöity)
    val olemassaolevatOot = kaikkiOlemassaolevatOot.filter(isRaportointikantaanSiirrettäväOpiskeluoikeus)
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

    val oppijaOids = oot.map(_.oppijaOid).distinct
    val masterOidsByOppijaOid = getMasterOidsForOppijaOids(oppijaOids)

    val (errors, outputRows) = oot.par
      .map(row => OpiskeluoikeusLoaderRowBuilder.buildKoskiRow(row, masterOidsByOppijaOid.get(row.oppijaOid).flatten))
      .seq
      .partition(_.isLeft)

    val successfulRows = outputRows.collect { case Right(value) => value }

    db.updateOpiskeluoikeudet(successfulRows.map(_.rOpiskeluoikeusRow), mitätöidytOot)
    db.updateOrganisaatioHistoria(successfulRows.flatMap(_.organisaatioHistoriaRows))

    val opiskeluoikeusAikajaksoRows = successfulRows.flatMap(_.rOpiskeluoikeusAikajaksoRows)
    db.updateOpiskeluoikeusAikajaksot(opiskeluoikeusAikajaksoRows)

    val aikajaksoRows = successfulRows.flatMap(_.rAikajaksoRows)
    db.updateAikajaksot(aikajaksoRows)

    val ammatillisenKoulutuksenJarjestamismuotoAikajaksoRows = successfulRows.flatMap(_.rAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows)
    db.updateAmmatillisenKoulutuksenJarjestamismuotoAikajaksot(ammatillisenKoulutuksenJarjestamismuotoAikajaksoRows)

    val updateOsaamisenHankkimistapaAikajaksoRows = successfulRows.flatMap(_.rOsaamisenHankkimistapaAikajaksoRows)
    db.updateOsaamisenHankkimistapaAikajaksoRows(updateOsaamisenHankkimistapaAikajaksoRows)

    val esiopetusOpiskeluoikeusAikajaksoRows = successfulRows.flatMap(_.esiopetusOpiskeluoikeusAikajaksoRows)
    db.updateEsiopetusOpiskeluoikeusAikajaksot(esiopetusOpiskeluoikeusAikajaksoRows)

    val päätasonSuoritusRows = successfulRows.flatMap(_.rPäätasonSuoritusRows)
    db.updatePäätasonSuoritukset(päätasonSuoritusRows)

    val osasuoritusRows = successfulRows.flatMap(_.rOsasuoritusRows)
    db.updateOsasuoritukset(osasuoritusRows)

    val muuAmmatillinenRaportointiRows = successfulRows.flatMap(_.muuAmmatillinenOsasuoritusRaportointiRows)
    db.updateMuuAmmatillinenRaportointi(muuAmmatillinenRaportointiRows)

    val topksAmmatillinenRaportointiRows = successfulRows.flatMap(_.topksAmmatillinenRaportointiRows)
    db.updateTOPKSAmmatillinenRaportointi(topksAmmatillinenRaportointiRows)

    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, successfulRows.size)
    val result = errors.collect { case Left(err) => err } :+ LoadProgressResult(successfulRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    val toOpiskeluoikeusUnsafeDuration: Long = successfulRows.map(_.toOpiskeluoikeusUnsafeDuration).sum / 1000000
    logger.info(s"Batchin käsittely kesti ${loadBatchDuration} ms, jossa toOpiskeluOikeusUnsafe ${toOpiskeluoikeusUnsafeDuration} ms.")
    result
  }

  private def updateBatchMitätöidytOpiskeluoikeudet(
    oot: Seq[KoskiOpiskeluoikeusRow],
    olemassaolevatOot: Seq[Opiskeluoikeus.Oid],
  ) = {
    val (errors, outputRows) = oot.par.filterNot(_.poistettu).map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity).seq.partition(_.isLeft)
    val successfulRows = outputRows.collect { case Right(value) => value }
    db.updateMitätöidytOpiskeluoikeudet(successfulRows, olemassaolevatOot)
    db.updateStatusCount(mitätöidytStatusName, successfulRows.size)
    errors.collect { case Left(err) => err }
  }

  private def updateBatchPoistetutOpiskeluoikeudet(
    oot: Seq[KoskiOpiskeluoikeusRow]
  ): Seq[LoadErrorResult] = {
    if (oot.nonEmpty) {
      val (errors, outputRows) = suostumuksenPeruutusService
        .etsiPoistetut(oot.map(_.oid))
        .map(OpiskeluoikeusLoaderRowBuilder.buildRowMitätöity(organisaatioRepository))
        .partition(_.isLeft)
      val successfulRows = outputRows.collect { case Right(value) => value }
      db.updateMitätöidytOpiskeluoikeudet(successfulRows, Seq.empty)
      db.updateStatusCount(mitätöidytStatusName, successfulRows.size)
      errors.collect { case Left(err) => err }
    } else {
      Seq.empty
    }
  }

  private def getMasterOidsForOppijaOids(oppijaOids: Seq[String]): Map[String, Option[String]] = {
    if (oppijaOids.isEmpty) {
      Map.empty
    } else {
      QueryMethods.runDbSync(
        henkilöCache.db,
        Henkilöt
          .filter(_.oid inSetBind oppijaOids)
          .map(h => (h.oid, h.masterOid))
          .result
      ).toMap
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
