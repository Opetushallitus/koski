package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db._
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, TäydellisetHenkilötiedot}
import fi.oph.koski.util.Futures
import PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._

class KoskiHenkilöCache(val db: DB) extends Logging with GlobalExecutionContext with KoskiDatabaseMethods {
  def find(queryString: String): List[String] = {
    if (queryString == "#error#") {
      throw new TestingException("Testing error handling") // TODO: how to inject error properly
    }
    val tableQuery = queryString match {
      case "" => Henkilöt
      case _ => Henkilöt
        .filter(henkilö => KoskiHenkilöCache.filterByQuery(queryString)(henkilö))
    }
    runDbSync((tableQuery.map(_.oid)).result).toList
  }
}

class KoskiHenkilöCacheUpdater(val db: DB, val henkilöt: HenkilöRepository) extends Logging with GlobalExecutionContext with KoskiDatabaseMethods {
  {
    logger.info("Initializing cache")
    val missingOids: List[String] = Futures.await(db.run(sql"""
      select distinct oppija_oid from opiskeluoikeus
      where oppija_oid not in (select oid from henkilo)
      """.as[String])).toList
    if (missingOids.nonEmpty) {
      logger.info(s"Copying ${missingOids.length} persons to local database")
      missingOids.grouped(1000).toList.foreach { oids =>
        runDbSync(DBIO.sequence(henkilöt.findByOids(oids).map(addHenkilöAction)))
      }
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


object KoskiHenkilöCache {
  def filterByQuery(hakusanat: String)(henkilö: Tables.HenkilöTable) = {
    val tsq = hakusanat.toLowerCase.split(" ").map(sana => toTsQuery(sana + ":*", Some("koski"))).reduce(_ @& _) // "koski" refers to our custom text search configuration, see migration file V26__

    val tsv = List(henkilö.etunimet, henkilö.sukunimi)
      .map(toTsVector(_, Some("koski")))
      .reduce(_ @+ _)
    (tsv @@ tsq)
  }
}
