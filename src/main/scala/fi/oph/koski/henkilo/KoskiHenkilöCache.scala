package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{NimitiedotJaOid, TäydellisetHenkilötiedot}
import fi.oph.koski.servlet.InvalidRequestException

class KoskiHenkilöCache(val db: DB) extends Logging with GlobalExecutionContext with KoskiDatabaseMethods {
  def findOids(queryString: String): List[String] = {
    if (queryString == "") {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort())
    }
    if (queryString == "#error#") {
      throw new TestingException("Testing error handling") // TODO: how to inject error properly
    }
    val tableQuery = Henkilöt
      .filter(henkilö => KoskiHenkilöCache.filterByQuery(queryString)(henkilö))
    runDbSync((tableQuery.map(_.oid)).result).toList
  }

  def findByOid(oid: String): Option[NimitiedotJaOid] = runDbSync(Henkilöt.filter(_.oid === oid).result).toList.headOption.map(_.toNimitiedotJaOid)
}

class KoskiHenkilöCacheUpdater(val db: DB, val henkilöt: HenkilöRepository) extends Logging with GlobalExecutionContext with KoskiDatabaseMethods {
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
