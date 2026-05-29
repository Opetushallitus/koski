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

import fi.oph.koski.executors.Pools

import rx.lang.scala.Observable

import java.time.{LocalDateTime, ZonedDateTime}
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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

  private val SlowOperationThresholdMs = 5000

  private def timedMs[T](name: String)(block: => T): (T, Long) = {
    val t0 = System.nanoTime()
    val result = block
    val ms = (System.nanoTime() - t0) / 1000000
    if (ms >= SlowOperationThresholdMs) {
      logger.warn(s"Hidas operaatio: $name kesti ${ms} ms")
    }
    (result, ms)
  }

  def loadOpiskeluoikeudet(): Observable[LoadResult] = {
    setStatusStarted(Some(toTimestamp(update.dueTime)))

    var loopCount = 0

    update.service.alustaKaikkiKäsiteltäviksi()
    db.cloneUpdateableTables(update.previousRaportointiDatabase, enableYtr)
    createIndexesForIncrementalUpdate()

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
    val (masterOidsByOppijaOid, masterOidsMs) = timedMs("masterOids")(getMasterOidsForOppijaOids(oppijaOids))

    val (buildResult, rowBuildMs) = timedMs("rowBuild") {
      oot.par
        .map(row => OpiskeluoikeusLoaderRowBuilder.buildKoskiRow(row, masterOidsByOppijaOid.get(row.oppijaOid).flatten))
        .seq
        .partition(_.isLeft)
    }
    val (errors, outputRows) = buildResult
    val successfulRows = outputRows.collect { case Right(value) => value }
    val toOpiskeluoikeusUnsafeMs = successfulRows.map(_.toOpiskeluoikeusUnsafeDuration).sum / 1000000

    val opiskeluoikeusAikajaksoRows                          = successfulRows.flatMap(_.rOpiskeluoikeusAikajaksoRows)
    val aikajaksoRows                                        = successfulRows.flatMap(_.rAikajaksoRows)
    val ammatillisenKoulutuksenJarjestamismuotoAikajaksoRows = successfulRows.flatMap(_.rAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows)
    val updateOsaamisenHankkimistapaAikajaksoRows            = successfulRows.flatMap(_.rOsaamisenHankkimistapaAikajaksoRows)
    val esiopetusOpiskeluoikeusAikajaksoRows                 = successfulRows.flatMap(_.esiopetusOpiskeluoikeusAikajaksoRows)
    val opiskeluoikeusRows                                   = successfulRows.map(_.rOpiskeluoikeusRow)
    val päätasonSuoritusRows                                 = successfulRows.flatMap(_.rPäätasonSuoritusRows)
    val osasuoritusRows                                      = successfulRows.flatMap(_.rOsasuoritusRows)
    val muuAmmatillinenRaportointiRows                       = successfulRows.flatMap(_.muuAmmatillinenOsasuoritusRaportointiRows)
    val topksAmmatillinenRaportointiRows                     = successfulRows.flatMap(_.topksAmmatillinenRaportointiRows)

    implicit val ec = Pools.globalExecutor

    def parallelUpdate[T](name: String)(block: => T): Future[T] =
      Future(block).recover { case e => logger.error(e)(s"Raportointikannan inkrementaalisen latauksen operaatio $name epäonnistui"); throw e }

    val fOo          = parallelUpdate("updateOpiskeluoikeudet")(timedMs("updateOpiskeluoikeudet")(db.updateOpiskeluoikeudet(opiskeluoikeusRows, mitätöidytOot)))
    val fOrgHistoria = parallelUpdate("updateOrganisaatioHistoria")(timedMs("updateOrganisaatioHistoria")(db.updateOrganisaatioHistoria(successfulRows.flatMap(_.organisaatioHistoriaRows))))
    val fAikajakso   = parallelUpdate("updateOpiskeluoikeusAikajaksot")(timedMs("updateOpiskeluoikeusAikajaksot")(db.updateOpiskeluoikeusAikajaksot(opiskeluoikeusAikajaksoRows)))
    val fPäätason    = parallelUpdate("updatePäätasonSuoritukset")(timedMs("updatePäätasonSuoritukset")(db.updatePäätasonSuoritukset(päätasonSuoritusRows)))
    val fOsasuoritus = parallelUpdate("updateOsasuoritukset")(timedMs("updateOsasuoritukset")(db.updateOsasuoritukset(osasuoritusRows, opiskeluoikeusRows.map(_.opiskeluoikeusOid).toSet)))

    val (updateOoMs, updateOrgHistoriaMs, updateOoAikajaksoMs, updatePäätasonMs, updateOsasuoritusMs) =
      Await.result(for {
        (_, ooMs)          <- fOo
        (_, orgHistoriaMs) <- fOrgHistoria
        (_, aikajaksoMs)   <- fAikajakso
        (_, päätasonMs)    <- fPäätason
        (_, osasuoritusMs) <- fOsasuoritus
      } yield (ooMs, orgHistoriaMs, aikajaksoMs, päätasonMs, osasuoritusMs), 15.minutes)

    val (_, updateAikajaksoMs)        = timedMs("updateAikajaksot")(db.updateAikajaksot(aikajaksoRows))
    val (_, updateJarjestamismuotoMs) = timedMs("updateJarjestamismuotoAikajaksot")(db.updateAmmatillisenKoulutuksenJarjestamismuotoAikajaksot(ammatillisenKoulutuksenJarjestamismuotoAikajaksoRows))
    val (_, updateHankkimistapaMs)    = timedMs("updateHankkimistapaAikajaksot")(db.updateOsaamisenHankkimistapaAikajaksoRows(updateOsaamisenHankkimistapaAikajaksoRows))
    val (_, updateEsiopetusMs)        = timedMs("updateEsiopetusAikajaksot")(db.updateEsiopetusOpiskeluoikeusAikajaksot(esiopetusOpiskeluoikeusAikajaksoRows))
    val (_, updateMuuAmmatillinenMs)  = timedMs("updateMuuAmmatillinenRaportointi")(db.updateMuuAmmatillinenRaportointi(muuAmmatillinenRaportointiRows))
    val (_, updateTopksMs)            = timedMs("updateTOPKSAmmatillinenRaportointi")(db.updateTOPKSAmmatillinenRaportointi(topksAmmatillinenRaportointiRows))

    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, successfulRows.size)
    val result = errors.collect { case Left(err) => err } :+ LoadProgressResult(successfulRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    logger.info(
      s"Batchin käsittely [${oot.size} oo, ${päätasonSuoritusRows.size} ps, ${osasuoritusRows.size} os] kesti " +
      s"yhteensä=${loadBatchDuration}ms, jossa " +
      s"masterOids=${masterOidsMs}ms, " +
      s"rowBuild=${rowBuildMs}ms (toOoUnsafe=${toOpiskeluoikeusUnsafeMs}ms), " +
      s"updateOo=${updateOoMs}ms, " +
      s"updateOrgHistoria=${updateOrgHistoriaMs}ms, " +
      s"updateOoAikajakso=${updateOoAikajaksoMs}ms, " +
      s"updateAikajakso=${updateAikajaksoMs}ms, " +
      s"updateJarjestamismuoto=${updateJarjestamismuotoMs}ms, " +
      s"updateHankkimistapa=${updateHankkimistapaMs}ms, " +
      s"updateEsiopetus=${updateEsiopetusMs}ms, " +
      s"updatePäätason=${updatePäätasonMs}ms, " +
      s"updateOsasuoritus=${updateOsasuoritusMs}ms, " +
      s"updateMuuAmmatillinen=${updateMuuAmmatillinenMs}ms, " +
      s"updateTopks=${updateTopksMs}ms"
    )
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
