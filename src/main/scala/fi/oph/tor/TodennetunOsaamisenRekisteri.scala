package fi.oph.tor

import java.sql.Timestamp
import java.util.Date

import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Futures, Tables}
import fi.oph.tor.model._
import fi.vm.sade.utils.slf4j.Logging
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._
import slick.jdbc.{PositionedParameters, SQLActionBuilder, SetParameter}
import slick.profile.SqlStreamingAction

import scala.concurrent.{ExecutionContext, Future}

class TodennetunOsaamisenRekisteri(db: DB)(implicit val executor: ExecutionContext) extends Futures with Logging {
  private type RowTuple = (Tables.SuoritusRow, Option[Tables.ArviointiRow])

  def getSuoritukset(filters: Iterable[SuoritusFilter] = List()): Future[Seq[Suoritus]] = {
    runQuery(buildSuoritusQueryAction(filters)) { rows =>
      rowsToSuoritukset(rows.map {
        case (suoritusRow, arviointiRow) =>
          // actually arviointiRow should be Option[ArviointiRow] but that's not supported by slick, so we have to do this iffing here
          if (arviointiRow.id > 0)
            (suoritusRow, Some(arviointiRow))
          else
            (suoritusRow, None)
      })
    }
  }

  private def buildSuoritusQueryAction(filters: Iterable[SuoritusFilter]) = {
    // SQL query is used because Slick query API doesn't support recursive query
    val sql = buildSuoritusQuerySql(filters)
    val setParams = buildSuoritusQueryParameterSetter(filters)
    SQLActionBuilder(List(sql), setParams)
      .as[(Tables.SuoritusRow, Tables.ArviointiRow)]
  }

  private def buildSuoritusQueryParameterSetter(filters: Iterable[SuoritusFilter]): SetParameter[Unit] = {
    new SetParameter[Unit] {
      override def apply(u : Unit, positionedParams: PositionedParameters) = {
        filters.foreach(_.apply(positionedParams))
      }
    }
  }

  private def buildSuoritusQuerySql(filters: Iterable[SuoritusFilter]): String = {
    def buildWhereClause(keys: Iterable[SuoritusFilter], first: Boolean = true): String = keys.toList match {
      case head::tail =>
        val prefix = if (first) " where " else " and "
        prefix + head.whereClauseFraction + buildWhereClause(tail, false)
      case _ => ""
    }
    val whereClause = buildWhereClause(filters)
    if (filters.exists(_.recursive)) {
      // At least one filter requires including parent rows of matches -> find those recursively using Common Table Expressions
      """WITH RECURSIVE rekursiivinen AS (
      select * from suoritus""" + whereClause +
        """
      union all
        select suoritus.* from suoritus, rekursiivinen
          where suoritus.id = rekursiivinen.parent_id
      )
      select distinct * from rekursiivinen
        left join arviointi on (rekursiivinen.arviointi_id = arviointi.id)
        """
    } else {
      """select * from suoritus left join arviointi on (suoritus.arviointi_id = arviointi.id)""" +
        whereClause
    }
  }

  private def rowsToSuoritukset(rows: Seq[RowTuple]): List[Suoritus] = {
    rowsToSuoritukset(rows.sortBy(_._1.komoOid), parentId = None) // <- None means root (no parent)
  }

  /** Transforms row structure into hierarchical structure of Suoritus objects */
  private def rowsToSuoritukset(rows: Seq[RowTuple], parentId: Option[Identified.Id]): List[Suoritus] = {
    for {
      (suoritusRow, arviointiRow) <- rows.toList if suoritusRow.parentId == parentId
    } yield {
      val osasuoritukset = rowsToSuoritukset(rows, Some(suoritusRow.id))
      Suoritus(Some(suoritusRow.id), suoritusRow.organisaatioOid, suoritusRow.personOid, suoritusRow.komoOid, suoritusRow.komoTyyppi, suoritusRow.status, suoritusRow.suorituspaiva, mapArviointi(arviointiRow), osasuoritukset)
    }
  }

  private def mapArviointi(arviointiOption: Option[Tables.ArviointiRow]): Option[Arviointi] = {
    arviointiOption.map { row => Arviointi(Some(row.id), row.asteikko, row.numero.toInt, row.kuvaus) }
  }

  def insertSuoritus(t: Suoritus, parentId: Option[Identified.Id] = None): Future[Identified.Id] = {
      for {
        arviointiId <- insertArviointi(t.arviointi);
        suoritusId <- insertAndReturnUpdated(Tables.Suoritus, Tables.SuoritusRow(0, parentId, t.organisaatioId, t.personOid, t.komoOid, t.komoTyyppi, t.status, arviointiId, t.suoritusPäivä.map(toTimestamp))).map(_.id);
        osasuoritusIds <- Future.sequence(t.osasuoritukset.map { insertSuoritus(_, Some(suoritusId))})
      } yield {
        suoritusId
      }
  }

  private def toTimestamp(d: Date) = new Timestamp(d.getTime)

  private def insertArviointi(arviointiOption: Option[Arviointi]): Future[Option[Identified.Id]] = arviointiOption match {
    case Some(arviointi) => insertAndReturnUpdated(Tables.Arviointi, Tables.ArviointiRow(0, arviointi.asteikko, arviointi.numero, arviointi.kuvaus)).map{ row => Some(row.id) }
    case None => Future(None)
  }

  private def insertAndReturnUpdated[T, TableType <: Table[T]](tableQuery: TableQuery[TableType], row: T): Future[T] = {
    db.run((tableQuery returning tableQuery) += row)
  }

  def runQuery[Seq[U], RowType, TableType, ResultType](queryAction: SqlStreamingAction[Seq[RowType], RowType, Read])(block: (Seq[RowType]) => ResultType): Future[ResultType] = {
    val f = db.run(queryAction).map { result =>
      block(result)
    }
    f.onFailure { case e: Exception =>
      logger.error("Error running query " + queryAction.statements.head, e)
      throw e;
    }
    f
  }
}