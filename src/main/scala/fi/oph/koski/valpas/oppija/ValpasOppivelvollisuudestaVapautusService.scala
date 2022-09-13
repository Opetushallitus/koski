package fi.oph.koski.valpas.oppija

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.valpas.valpasuser.ValpasSession
import slick.jdbc.GetResult

import java.time.LocalDate

class ValpasOppivelvollisuudestaVapautusService(application: KoskiApplication) extends Logging {
  val db = new ValpasOppivelvollisuudestaVapautusRepository(application.valpasDatabase.db)

  def mapVapautetutOppijat[T](oppijat: Seq[T], toOids: T => Seq[String])(map: (T, OppivelvollisuudestaVapautus) => T): Seq[T] = {
    val oppijaOids = oppijat.flatMap(toOids)
    val vapautukset = db.getOppivelvollisuudestaVapautetutOppijat(oppijaOids)
    oppijat.map { oppija =>
      vapautukset.find(v => toOids(oppija).contains(v.oppijaOid)) match {
        case Some(vapautus) => map(oppija, vapautus)
        case None => oppija
      }
    }
  }

  def lisääOppivelvollisuudestaVapautus(oppijaOid: Oid, vapautettu: LocalDate)(implicit session: ValpasSession): Unit =
    db.lisääOppivelvollisuudestaVapautus(oppijaOid, session.oid, vapautettu)

  def mitätöiOppivelvollisuudestaVapautus(oppijaOid: Oid)(implicit session: ValpasSession): Boolean =
    db.mitätöiOppivelvollisuudestaVapautus(oppijaOid, session.oid)
}

class ValpasOppivelvollisuudestaVapautusRepository(val db: DB) extends QueryMethods with Logging {
  def getOppivelvollisuudestaVapautetutOppijat(oppijaOids: Seq[String]): Seq[OppivelvollisuudestaVapautus] =
    runDbSync(sql"""
      SELECT oppija_oid, vapautettu
        FROM oppivelvollisuudesta_vapautetut
        WHERE oppija_oid = any($oppijaOids)
          AND mitatoity IS NULL;
    """.as[OppivelvollisuudestaVapautus])

  def lisääOppivelvollisuudestaVapautus(oppijaOid: Oid, virkailijaOid: Oid, vapautettu: LocalDate): Unit = {
    runDbSync(sql"""
      INSERT INTO oppivelvollisuudesta_vapautetut
        (oppija_oid, virkailija_oid, vapautettu)
        VALUES ($oppijaOid, $virkailijaOid, $vapautettu)
    """.asUpdate)
  }

  def mitätöiOppivelvollisuudestaVapautus(oppijaOid: Oid, virkailijaOid: Oid): Boolean =
    runDbSync(sql"""
      UPDATE oppivelvollisuudesta_vapautetut
        SET mitatoity = now()
        WHERE oppija_oid = $oppijaOid
          AND virkailija_oid = $virkailijaOid
          AND mitatoity IS NULL
    """.asUpdate) > 0 // TODO: Heitä mielummin poikkeus, jos mätsäävää riviä ei löytynyt

  def deleteAll(): Unit =
    runDbSync(sql"""TRUNCATE oppivelvollisuudesta_vapautetut""".asUpdate)

  private implicit def getOppivelvollisuudestaVapautus: GetResult[OppivelvollisuudestaVapautus] = GetResult(r =>
    OppivelvollisuudestaVapautus(
      oppijaOid = r.rs.getString("oppija_oid"),
      vapautettu = r.rs.getDate("vapautettu").toLocalDate,
    )
  )
}

case class OppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
)
