package fi.oph.tor

import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Futures, Tables}
import fi.oph.tor.model.{Identified, Arviointi, Tutkintosuoritus}
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}

class TodennetunOsaamisenRekisteri(db: DB)(implicit val executor: ExecutionContext) extends Futures {
  def getTutkintosuoritukset: Future[Seq[Tutkintosuoritus]] = {
    val query = for {
      ts <- Tables.Tutkintosuoritus;
      tsa <- Tables.Arviointi if ts.arviointiId === tsa.id
    } yield {
      (ts, tsa)
    }
    val queryResultFuture: Future[Seq[(Tables.TutkintosuoritusRow, Tables.ArviointiRow)]] = db.run(query.result)
    queryResultFuture.map { rows =>
      rows.map {
        case (ts: Tables.TutkintosuoritusRow, tsa: Tables.ArviointiRow) =>
          val arviointi = Arviointi(Some(tsa.id), tsa.asteikko, tsa.numero.toInt, tsa.kuvaus)
          Tutkintosuoritus(Some(ts.id), ts.organisaatioOid, ts.personOid, ts.komoOid, ts.status, Some(arviointi), List())
      }.toList
    }
  }
  def insertTutkintosuoritus(t: Tutkintosuoritus): Future[Identified.Id] = {
    insertAndReturnUpdated(Tables.Tutkintosuoritus, Tables.TutkintosuoritusRow(0, t.organisaatioId, t.personOid, t.komoOid, t.status, t.arviointi.map(insertArviointi)))
      .map(_.id)
  }

  private def insertArviointi(arviointi: Arviointi): Identified.Id = {
    await(insertAndReturnUpdated(Tables.Arviointi, Tables.ArviointiRow(0, arviointi.asteikko, arviointi.numero, arviointi.kuvaus))).id
  }

  private def insertAndReturnUpdated[T, TableType <: Table[T]](tableQuery: TableQuery[TableType], row: T): Future[T] = {
    db.run((tableQuery returning tableQuery) += row)
  }
}
