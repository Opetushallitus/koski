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

  def getSuoritukset(query: SuoritusQuery): Future[Seq[Suoritus]] = {
    runQuery(buildSuoritusQueryAction(query)) { rows =>
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

  private def buildSuoritusQueryAction(query: SuoritusQuery) = {
    // SQL query is used because Slick query API doesn't support recursive query
    val sql = buildSuoritusQuerySql(query)
    val setParams = buildSuoritusQueryParameterSetter(query.filters)
    SQLActionBuilder(List(sql), setParams)
      .as[(Tables.SuoritusRow, Tables.ArviointiRow)]
  }

  private def buildSuoritusQueryParameterSetter(filters: Iterable[SuoritusQueryFilter]): SetParameter[Unit] = {
    new SetParameter[Unit] {
      override def apply(u : Unit, positionedParams: PositionedParameters) = {
        filters.foreach(_.apply(positionedParams))
      }
    }
  }

  private def buildSuoritusQuerySql(query: SuoritusQuery): String = {
    def buildWhereClause(keys: Iterable[SuoritusQueryFilter], first: Boolean = true): String = keys.toList match {
      case head::tail =>
        val prefix = if (first) " where " else " and "
        prefix + head.whereClauseFraction + buildWhereClause(tail, false)
      case _ => ""
    }
    val whereClause = buildWhereClause(query.filters)
    val searchParentsRecursively: Boolean = query.filters.exists(_.searchParentsRecursively)
    val searchChildrenRecursively = query.includeChildren
    def joinQuery = """ select * from suoritusquery left join arviointi on (suoritusquery.arviointi_id = arviointi.id)"""

    if (searchParentsRecursively || searchChildrenRecursively) {
      // At least one filter requires including parent+children of matches -> find those recursively using Common Table Expressions
      val childrenQuery = if (searchChildrenRecursively) Some("""select * from children_included""") else None
      val parentsQuery = if (searchParentsRecursively) Some("""select * from parents_included""") else None
      val unionQuery =  List(childrenQuery, parentsQuery).flatten.mkString(" union all ")
      """
      WITH RECURSIVE matching AS (
        select * from suoritus""" + whereClause + """
      ), parents_included AS (
        select * from matching
        union all
          select suoritus.* from suoritus, parents_included
            where suoritus.id = parents_included.parent_id
      ), children_included AS (
        select * from matching
        union all
          select suoritus.* from suoritus, children_included
            where suoritus.parent_id = children_included.id
      ), suoritusquery AS ( select distinct * from (""" + unionQuery + """) as unionquery)""" + joinQuery
    } else {
      // No recursive search needed, rows from suoritus table are fetched directly
      "WITH suoritusquery AS (select * from suoritus) " + joinQuery + whereClause
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