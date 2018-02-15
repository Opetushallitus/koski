package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{TäydellisetHenkilötiedot, TäydellisetHenkilötiedotWithMasterInfo}

class KoskiHenkilöCache(val db: DB, val henkilöt: HenkilöRepository) extends Logging with DatabaseExecutionContext with KoskiDatabaseMethods {
  def addHenkilöAction(data: TäydellisetHenkilötiedotWithMasterInfo) = {
    def addHenkilö(oid: String, row: HenkilöRow) = {
      Henkilöt.filter(_.oid === oid).result.map(_.toList).flatMap {
        case Nil =>
          Henkilöt += row
        case _ =>
          DBIO.successful(0)
      }
    }

    val addMasterIfNecessary = data.master.map { m =>
      addHenkilö(m.oid, toHenkilöRow(m, None))
    }.getOrElse(DBIO.successful())

    addMasterIfNecessary
      .andThen(addHenkilö(data.henkilö.oid, toHenkilöRow(data.henkilö, data.master.map(_.oid))))
  }

  def updateHenkilöAction(data: TäydellisetHenkilötiedotWithMasterInfo): Int =
    runDbSync(Henkilöt.filter(_.oid === data.henkilö.oid).update(toHenkilöRow(data.henkilö, data.master.map(_.oid))))


  def getCachedAction(oppijaOid: String): DBIOAction[Option[TäydellisetHenkilötiedotWithMasterInfo], NoStream, Effect.Read] = (Henkilöt.filter(_.oid === oppijaOid).joinLeft(Henkilöt).on(_.masterOid === _.oid)).result.map(x => x.headOption.map { case (row, masterRow) =>
    TäydellisetHenkilötiedotWithMasterInfo(row.toHenkilötiedot, masterRow.map(_.toHenkilötiedot))
  })

  def filterOidsByCache(oids: List[String]) = {
    // split to groups of 10000 to ensure this works with larger batches. Tested: 10000 works, 100000 does not.
    oids.grouped(10000).flatMap(group => runDbSync(Henkilöt.map(_.oid).filter(_ inSetBind(group)).result))
  }

  private def toHenkilöRow(data: TäydellisetHenkilötiedot, masterOid: Option[String]) = HenkilöRow(data.oid, data.sukunimi, data.etunimet, data.kutsumanimi, masterOid)
}

object KoskiHenkilöCache {
  def filterByQuery(hakusanat: String)(henkilö: Tables.HenkilöTable) = {
    val tsq = hakusanat.toLowerCase.split(" ").map(sana => toTsQuery("\"" + sana + "\"" + ":*", Some("koski"))).reduce(_ @& _) // "koski" refers to our custom text search configuration, see migration file V26__

    val tsv = List(henkilö.etunimet, henkilö.sukunimi)
      .map(toTsVector(_, Some("koski")))
      .reduce(_ @+ _)
    (tsv @@ tsq)
  }
}
