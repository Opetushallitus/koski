package fi.oph.tor

import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Futures, Tables}
import fi.oph.tor.model._
import fi.vm.sade.utils.slf4j.Logging
import slick.driver.PostgresDriver.api._
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

class TodennetunOsaamisenRekisteri(db: DB)(implicit val executor: ExecutionContext) extends Futures with Logging {
  private type RowTuple = (Tables.SuoritusRow, Option[Tables.ArviointiRow])

  def getSuoritukset(filter: SuoritusFilter = KaikkiSuoritukset): Future[Seq[Suoritus]] = {
    val query = filter.apply(Tables.Suoritus) joinLeft Tables.Arviointi on (_.arviointiId === _.id);
    runQuery(query) { (rows: Seq[RowTuple]) =>
      rowsToSuoritukset(rows.sortBy(_._1.komoOid), parentId = None) // <- None means root (no parent)
    }
  }

  /** Transforms row structure into hierarchical structure of Suoritus objects */
  private def rowsToSuoritukset(rows: Seq[RowTuple], parentId: Option[Identified.Id]): List[Suoritus] = {
    for {
      (suoritusRow, arviointiRow) <- rows.toList if suoritusRow.parentId == parentId
    } yield {
      val osasuoritukset = rowsToSuoritukset(rows, Some(suoritusRow.id))
      Suoritus(Some(suoritusRow.id), suoritusRow.organisaatioOid, suoritusRow.personOid, suoritusRow.komoOid, suoritusRow.komoTyyppi, suoritusRow.status, mapArviointi(arviointiRow), osasuoritukset)
    }
  }

  private def mapArviointi(arviointiOption: Option[Tables.ArviointiRow]): Option[Arviointi] = {
    arviointiOption.map { row => Arviointi(Some(row.id), row.asteikko, row.numero.toInt, row.kuvaus) }
  }

  def insertSuoritus(t: Suoritus, parentId: Option[Identified.Id] = None): Future[Identified.Id] = {
      for {
        arviointiId <- insertArviointi(t.arviointi);
        suoritusId <- insertAndReturnUpdated(Tables.Suoritus, Tables.SuoritusRow(0, parentId, t.organisaatioId, t.personOid, t.komoOid, t.komoTyyppi, t.status, arviointiId)).map(_.id);
        osasuoritusIds <- Future.sequence(t.osasuoritukset.map { insertSuoritus(_, Some(suoritusId))})
      } yield {
        suoritusId
      }
  }

  private def insertArviointi(arviointiOption: Option[Arviointi]): Future[Option[Identified.Id]] = arviointiOption match {
    case Some(arviointi) => insertAndReturnUpdated(Tables.Arviointi, Tables.ArviointiRow(0, arviointi.asteikko, arviointi.numero, arviointi.kuvaus)).map{ row => Some(row.id) }
    case None => Future(None)
  }

  private def insertAndReturnUpdated[T, TableType <: Table[T]](tableQuery: TableQuery[TableType], row: T): Future[T] = {
    db.run((tableQuery returning tableQuery) += row)
  }

  private def runQuery[ResultType, TableType, RowType, Seq[U]](query: Query[TableType, RowType, Seq])(block: Seq[RowType] => ResultType): Future[ResultType] = {
    val f = db.run(query.result).map{ result =>
      block(result)
    }
    f.onFailure {
      case e: Exception =>
        logger.error("Error running query " + query.result.statements.head, e)
        throw e;
    }
    f
  }
}