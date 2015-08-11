package fi.oph.tor

import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Futures, Tables}
import fi.oph.tor.model.{Tutkinnonosasuoritus, Identified, Arviointi, Tutkintosuoritus, Kurssisuoritus}
import fi.vm.sade.utils.slf4j.Logging
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}

class TodennetunOsaamisenRekisteri(db: DB)(implicit val executor: ExecutionContext) extends Futures with Logging {
  def getTutkintosuoritukset: Future[Seq[Tutkintosuoritus]] = {
    val query = for {
      (ts, tsa) <- Tables.Tutkintosuoritus joinLeft Tables.Arviointi on (_.arviointiId === _.id);
      (tos, tosa) <- Tables.Tutkinnonosasuoritus joinLeft Tables.Arviointi on (_.arviointiId === _.id) if (tos.tutkintosuoritusId === ts.id);
      (ks, ksa) <- Tables.Kurssisuoritus joinLeft Tables.Arviointi on { (ks, ksa) => ks.arviointiId === ksa.id } filter (_._1.tutkinnonosasuoritusId === tos.id)
    } yield {
      (ts, tsa, tos, tosa, ks, ksa)
    }
    runQuery(query) { rows: Seq[(Tables.TutkintosuoritusRow, Option[Tables.ArviointiRow], Tables.TutkinnonosasuoritusRow, Option[Tables.ArviointiRow], Tables.KurssisuoritusRow, Option[Tables.ArviointiRow])] =>
      val groupedByTutkinto = rows.groupBy { case (ts, tsa, _, _, _, _) => (ts, tsa) }
      groupedByTutkinto.map { case ((ts, tsa), tutkinnonOsat) =>
          val groupedByTutkinnonosa = tutkinnonOsat.groupBy { case (_, _, tos, tosa, _, _) => (tos, tosa) }
          val osasuoritukset = groupedByTutkinnonosa.map { case ((tos, tosa), kurssit) =>
            val kurssisuoritukset = kurssit.map { case (_, _, _, _, ks, ksa) =>
              Kurssisuoritus(Some(ks.id), ks.komoOid, ks.status, mapArviointi(ksa))
            }.toList.sortBy(_.komoOid)
            Tutkinnonosasuoritus(Some(tos.id), tos.komoOid, tos.status, mapArviointi(tosa), kurssisuoritukset)
          }.toList.sortBy(_.komoOid)
          Tutkintosuoritus(Some(ts.id), ts.organisaatioOid, ts.personOid, ts.komoOid, ts.status, mapArviointi(tsa), osasuoritukset)
      }.toList.sortBy(_.komoOid)
    }
  }

  def mapArviointi(arviointiOption: Option[Tables.ArviointiRow]): Option[Arviointi] = {
    arviointiOption.map { row => Arviointi(Some(row.id), row.asteikko, row.numero.toInt, row.kuvaus) }
  }

  private def runQuery[T, E, U, C[_]](query: Query[E, U, C])(block: C[U] => T): Future[T] = {
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

  def insertTutkintosuoritus(t: Tutkintosuoritus): Future[Identified.Id] = {
      for {
        arviointiId <- maybeInsertArviointi(t.arviointi);
        tutkintosuoritusId <- insertAndReturnUpdated(Tables.Tutkintosuoritus, Tables.TutkintosuoritusRow(0, t.organisaatioId, t.personOid, t.komoOid, t.status, arviointiId)).map(_.id);
        osasuoritusIds <- Future.sequence(t.osasuoritukset.map { insertTutkinnonosasuoritus(_, tutkintosuoritusId)})
      } yield {
        tutkintosuoritusId
      }
  }

  private def insertTutkinnonosasuoritus(t: Tutkinnonosasuoritus, tutkintosuoritusId: Identified.Id): Future[Identified.Id] = {
    for {
      arviointiId <- maybeInsertArviointi(t.arviointi)
      osasuoritusId <- insertAndReturnUpdated(Tables.Tutkinnonosasuoritus, Tables.TutkinnonosasuoritusRow(0, tutkintosuoritusId, t.komoOid, t.status, arviointiId)).map(_.id)
      kurssisuoritusIds <- Future.sequence(t.kurssisuoritukset.map { insertKurssisuoritus(_, osasuoritusId) })
    } yield {
      osasuoritusId
    }
  }

  private def insertKurssisuoritus(k: Kurssisuoritus, tutkinnonosasuoritusId: Identified.Id): Future[Identified.Id] = {
    for {
      arviointiId <- maybeInsertArviointi(k.arviointi)
      kurssisuoritusId <- insertAndReturnUpdated(Tables.Kurssisuoritus, Tables.KurssisuoritusRow(0, tutkinnonosasuoritusId, k.komoOid, k.status, arviointiId)).map(_.id)
    } yield {
      kurssisuoritusId
    }
  }

  private def maybeInsertArviointi(arviointiOption: Option[Arviointi]): Future[Option[Identified.Id]] = arviointiOption match {
    case Some(arviointi) => insertAndReturnUpdated(Tables.Arviointi, Tables.ArviointiRow(0, arviointi.asteikko, arviointi.numero, arviointi.kuvaus)).map{ row => Some(row.id) }
    case None => Future(None)
  }

  private def insertArviointi(arviointi: Arviointi): Future[Identified.Id] = {
    insertAndReturnUpdated(Tables.Arviointi, Tables.ArviointiRow(0, arviointi.asteikko, arviointi.numero, arviointi.kuvaus)).map(_.id)
  }

  private def insertAndReturnUpdated[T, TableType <: Table[T]](tableQuery: TableQuery[TableType], row: T): Future[T] = {
    db.run((tableQuery returning tableQuery) += row)
  }
}
