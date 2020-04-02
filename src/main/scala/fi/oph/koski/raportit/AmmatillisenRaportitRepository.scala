package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import slick.jdbc.GetResult
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import scala.concurrent.duration._

case class AmmatillisenRaportitRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {

  private val defaultTimeout = 10.minutes

  private type OpiskeluoikeusOid = String
  private type SisältyvOpiskeluoikeuteenOid = String
  private type AikajaksoId = Long
  private type PäätasonSuoritusId = Long
  private type OppijaOid = String

  def suoritustiedot(oppilaitos: Organisaatio.Oid, koulutusmuoto: String, suorituksenTyyppi: String, alku: LocalDate, loppu: LocalDate) = {
    val opiskeluoikeusAikajaksotPäätasonSuoritukset = opiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos, koulutusmuoto, suorituksenTyyppi, Date.valueOf(alku), Date.valueOf(loppu))
    val masterOpiskeluoikeusOids = opiskeluoikeusAikajaksotPäätasonSuoritukset.map(_._1)

    val sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset = sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(masterOpiskeluoikeusOids)
    val sisältyvätOpiskeluoikeusOids = sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset.map(_._1)
    val sisältyvätOpiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet sisältyvätOpiskeluoikeusOids).result, timeout = defaultTimeout)
    val sisältyvätOpiskeluoikeudetGrouped = sisältyvätOpiskeluoikeudet.groupBy(_.sisältyyOpiskeluoikeuteenOid.get)

    val päätasonSuoritusIds = opiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._2).union(sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._2))
    val aikajaksoIds = opiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._3).union(sisältyvätOpiskeluoikeusAikajaksotPäätasonSuoritukset.flatMap(_._3))

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet masterOpiskeluoikeusOids).result, timeout = defaultTimeout).union(sisältyvätOpiskeluoikeudet)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)
    val päätasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet päätasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet päätasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.päätasonSuoritusId)
    val henkilöt = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid)).result, timeout = defaultTimeout).groupBy(_.oppijaOid).mapValues(_.head)

    opiskeluoikeudet.flatMap { opiskeluoikeus =>
      päätasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map { päätasonSuoritus =>
        (
          opiskeluoikeus,
          henkilöt(opiskeluoikeus.oppijaOid),
          aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty).map(_.truncateToDates(Date.valueOf(alku), Date.valueOf(loppu))).sortBy(_.alku)(sqlDateOrdering),
          päätasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty),
          sisältyvätOpiskeluoikeudetGrouped.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Seq.empty),
          osasuoritukset.getOrElse(päätasonSuoritus.päätasonSuoritusId, Seq.empty)
        )
      }
    }
}

  private def opiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitosOid: String, koulutusmuoto: String, suorituksenTyyppi: String, alku: Date, loppu: Date) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](r => (r.nextString(), r.nextArray(), r.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPäätasonSuorituksetSQL(oppilaitosOid, koulutusmuoto, suorituksenTyyppi, alku, loppu).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPäätasonSuorituksetSQL(oppilaitosOid: String, koulutusmuoto: String, suorituksenTyyppi: String, alku: Date, loppu: Date) = {
    sql"""
      select
        oo.opiskeluoikeus_oid,
        array_agg(pts.paatason_suoritus_id),
        array_agg(aikaj.id)
      from r_opiskeluoikeus oo
      join r_paatason_suoritus pts
        on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikaj
        on aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where
        oo.oppilaitos_oid = $oppilaitosOid and
        oo.koulutusmuoto = $koulutusmuoto and
        pts.suorituksen_tyyppi = $suorituksenTyyppi and
        aikaj.alku <= $loppu and (aikaj.loppu >= $alku or aikaj.loppu is null)
      group by oo.opiskeluoikeus_oid
       """
  }

  private def sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(masterOids: Seq[OpiskeluoikeusOid]) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](r => (r.nextString(), r.nextArray(), r.nextArray()))
    runDbSync(sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetSQL(masterOids).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = defaultTimeout)
  }

  private def sisältyvätOpiskeluoikeusAikajaksotPäätasonSuorituksetSQL(masterOids: Seq[OpiskeluoikeusOid]) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    sql"""
      select
        oo.opiskeluoikeus_oid,
        array_agg(pts.paatason_suoritus_id),
        array_agg(aikaj.id)
      from r_opiskeluoikeus oo
      join r_paatason_suoritus pts
        on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikaj
        on aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where
        (oo.sisaltyy_opiskeluoikeuteen_oid = any ($masterOids))
      group by oo.opiskeluoikeus_oid
      """
  }
}
