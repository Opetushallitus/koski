package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.{GlobalExecutionContext, HenkilöRow, KoskiDatabaseMethods, PostgresDriverWithJsonSupport}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.util.Futures
import PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._

class KoskiHenkilöCache(val db: DB, val henkilöt: HenkilöRepository) extends Logging with GlobalExecutionContext with KoskiDatabaseMethods {
  {
    logger.info("Initializing cache")
    val missingOids: List[String] = Futures.await(db.run(sql"""select distinct oppija_oid from opiskeluoikeus
          where oppija_oid not in (select oid from henkilo)""".as[String])).toList
    logger.info(s"Storing ${missingOids.length} persons to local database")
    missingOids.grouped(1000).toList.foreach { oids =>
      runDbSync(DBIO.sequence(henkilöt.findByOids(oids).map(addHenkilöAction)))
    }
  }

  def addHenkilöAction(henkilö: TäydellisetHenkilötiedot) = {
    Henkilöt.filter(_.oid === henkilö.oid).result.map(_.toList).flatMap {
      case Nil =>
        Henkilöt += HenkilöRow(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi)
      case _ =>
        DBIO.successful(0)
    }
  }
}
