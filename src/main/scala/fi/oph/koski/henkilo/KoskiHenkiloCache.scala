package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db._
import fi.oph.koski.log.Logging

class KoskiHenkilöCache(val db: DB) extends Logging with DatabaseExecutionContext with QueryMethods {
  def addHenkilöAction(data: OppijaHenkilöWithMasterInfo) =
    addMasterIfNecessary(data.master)
      .andThen(addHenkilö(data.henkilö.oid, toHenkilöRow(data.henkilö, data.master.map(_.oid))))

  def updateHenkilö(data: OppijaHenkilöWithMasterInfo): Int =
    runDbSync(addMasterIfNecessary(data.master)
      .andThen(Henkilöt.filter(_.oid === data.henkilö.oid).update(toHenkilöRow(data.henkilö, data.master.map(_.oid)))))

  def getCachedAction(oppijaOid: String): DBIOAction[Option[HenkilöRowWithMasterInfo], NoStream, Effect.Read] = (Henkilöt.filter(_.oid === oppijaOid).joinLeft(Henkilöt).on(_.masterOid === _.oid)).result.map(x => x.headOption.map { case (row, masterRow) =>
    HenkilöRowWithMasterInfo(row, masterRow)
  })

  def filterOidsByCache(oids: List[String]) = {
    // split to groups of 10000 to ensure this works with larger batches. Tested: 10000 works, 100000 does not.
    oids.grouped(10000).flatMap(group => runDbSync(Henkilöt.map(_.oid).filter(_ inSetBind(group)).result))
  }

  def resolveLinkedOids(masterOids: Seq[String]): Seq[String] =
    runDbSync(Henkilöt.filter(_.masterOid inSetBind masterOids).map(_.oid).result)

  def resolveLinkedOids(masterOid: String): Seq[String] =
    runDbSync(Henkilöt.filter(_.masterOid === masterOid).map(_.oid).result)

  private def addMasterIfNecessary(master: Option[OppijaHenkilö]) =
    master.map { m =>
      addHenkilö(m.oid, toHenkilöRow(m, None))
    }.getOrElse(DBIO.successful(Unit))

  private def addHenkilö(oid: String, row: HenkilöRow) = {
    Henkilöt.filter(_.oid === oid).result.map(_.toList).flatMap {
      case Nil =>
        Henkilöt += row
      case _ =>
        DBIO.successful(0)
    }
  }

  private def toHenkilöRow(data: OppijaHenkilö, masterOid: Option[String]) = HenkilöRow(data.oid, data.sukunimi, data.etunimet, data.kutsumanimi, masterOid)
}

