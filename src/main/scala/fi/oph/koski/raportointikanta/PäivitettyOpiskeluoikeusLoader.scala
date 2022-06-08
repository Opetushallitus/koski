package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables.OpiskeluOikeudet
import fi.oph.koski.db.{DB, OpiskeluoikeusRow, PäivitettyOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.opiskeluoikeus.PäivitetytOpiskeluoikeudetJonoService
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.Observable
import rx.observables.SyncOnSubscribe.createStateful
import rx.Observable.{create => createObservable}

import java.time.ZonedDateTime

class PäivitettyOpiskeluoikeusLoader(
  val db: DB,
  val opiskeluoikeusJonoService: PäivitetytOpiskeluoikeudetJonoService,
) extends QueryMethods {

  def load[A]
    (pageSize: Int, update: RaportointiDatabaseUpdate)
    (processRows: Seq[OpiskeluoikeusRow] => Seq[A])
  : Observable[A] = {
    import rx.lang.scala.JavaConverters._

    case class State(
      queue: Seq[PäivitettyOpiskeluoikeusRow],
      updateIds: Seq[Int] = Seq.empty,
      batchStartTime: Option[ZonedDateTime] = None,
      resultBatch: Seq[OpiskeluoikeusRow] = Seq.empty,
      nextPage: Int = 0,
    ) {
      def loadNextPage(): State = {
        val batchStartTime = ZonedDateTime.now()
        val index = nextPage * pageSize
        val updates = queue.slice(index, index + pageSize)
        val oids = updates.map(_.opiskeluoikeusOid)
        val result = runDbSync(OpiskeluOikeudet.filter(_.oid inSet oids).result)
        this.copy(
          updateIds = updates.flatMap(_.id),
          resultBatch = result,
          batchStartTime = Some(batchStartTime),
          nextPage = nextPage + 1,
        )
      }

      def completed: State = this.copy(resultBatch = Nil)
      def nothingToProcess: Boolean = resultBatch.isEmpty
    }

    object State {
      def init(): State = {
        val state = State(queue = opiskeluoikeusJonoService.päivitetytOpiskeluoikeudet(update.dueTime))
        state.loadNextPage()
      }
    }

    createObservable(createStateful[State, Seq[A]](
      (() => State.init()): Func0[_ <: State],
      ((state, observer) => {
        def process(): Unit = {
          observer.onNext(processRows(state.resultBatch))
          opiskeluoikeusJonoService.merkitseKäsitellyiksi(state.updateIds)
        }
        if (state.nothingToProcess) {
          if (state.batchStartTime.exists(_.isAfter(update.dueTime))) {
            process()
            observer.onCompleted()
            state.completed
          } else {
            Thread.sleep(update.sleepDuration.toMillis)
            State.init() // Restart loading
          }
        } else {
          process()
          state.loadNextPage()
        }
      }): Func2[_ >: State, _ >: Observer[_ >: Seq[A]], _ <: State]
    )).asScala.flatMap(Observable.from(_))
  }
}
