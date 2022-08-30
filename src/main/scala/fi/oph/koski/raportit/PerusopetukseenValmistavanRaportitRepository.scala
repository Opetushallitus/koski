package fi.oph.koski.raportit

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio.Oid
import slick.jdbc.GetResult

import java.time.LocalDate
import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class PerusopetukseenValmistavanRaportitRepository(db: DB) extends QueryMethods with RaportointikantaTableQueries {

  type OpiskeluoikeusOid = String
  type Tunnisteet = (OpiskeluoikeusOid)
  private type PäätasonSuoritusId = Long

  private val defaultTimeout: FiniteDuration = 5.minutes

  def perusopetukseenValmistavanRaporttiRows(
    oppilaitosOids: Seq[Oid],
    alku: LocalDate,
    loppu: LocalDate
  ): Seq[PerusopetukseenValmistavanRaporttiRows] = {
    val opiskeluoikeudetOppilaitoksille = queryOpiskeluoikeusOids(oppilaitosOids, alku, loppu)

    val opiskeluoikeusOids = opiskeluoikeudetOppilaitoksille.map(_._1)
    val paatasonSuoritusIds = opiskeluoikeudetOppilaitoksille.flatMap(_._2)

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout)
      .groupBy(_.päätasonSuoritusId)

    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)

    opiskeluoikeudet.foldLeft[Seq[PerusopetukseenValmistavanRaporttiRows]](Seq.empty) { (acc, oo) =>
      paatasonSuoritukset.getOrElse(oo.opiskeluoikeusOid, Nil).map((pts: RPäätasonSuoritusRow) =>
        PerusopetukseenValmistavanRaporttiRows(
          opiskeluoikeus = oo,
          henkilo = henkilot(oo.oppijaOid),
          päätasonSuoritus = pts,
          aikajaksot = aikajaksot(oo.opiskeluoikeusOid),
          osasuoritukset = osasuoritukset.getOrElse(pts.päätasonSuoritusId, Nil)
        )
      ) ++ acc
    }
  }

  private def queryOpiskeluoikeusOids(oppilaitokset: Seq[String], alku: LocalDate, loppu: LocalDate) = {
    implicit val getResult = GetResult(rs => (rs.nextString, rs.nextArray, rs.nextArray))

    val query =
      sql"""
     select
      oo.opiskeluoikeus_oid,
      array_agg(pts.paatason_suoritus_id)
    from
      r_opiskeluoikeus oo
    join
      r_paatason_suoritus pts
    on
      pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join r_opiskeluoikeus_aikajakso aikaj
      on aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    where
      oo.oppilaitos_oid = any ($oppilaitokset) and
      oo.koulutusmuoto = 'perusopetukseenvalmistavaopetus' and
      pts.suorituksen_tyyppi = 'perusopetukseenvalmistavaopetus' and
      aikaj.alku <= $loppu and aikaj.loppu >= $alku
    group by oo.opiskeluoikeus_oid"""

    runDbSync(query.as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId])], timeout = defaultTimeout)
  }
}

case class PerusopetukseenValmistavanRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  päätasonSuoritus: RPäätasonSuoritusRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  osasuoritukset: Seq[ROsasuoritusRow]
) extends YleissivistäväRaporttiRows
