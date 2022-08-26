package fi.oph.koski.raportit

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio.Oid
import slick.jdbc.GetResult

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class PerusopetukseenValmistavanRaportitRepository(db: DB) extends QueryMethods with RaportointikantaTableQueries {

  type OpiskeluoikeusOid = String
  type Tunnisteet = (OpiskeluoikeusOid)

  def perusopetukseenValmistavanRaporttiRows(
    oppilaitosOids: Seq[Oid],
    alku: LocalDate,
    loppu: LocalDate
  ): Seq[PerusopetukseenValmistavanRaporttiRows] = {
    val opiskeluoikeudetOppilaitoksille: Seq[OpiskeluoikeusOid] = queryOpiskeluoikeusOids(oppilaitosOids, alku, loppu)

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeudetOppilaitoksille).result, timeout = 5.minutes)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)

    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.opiskeluoikeusOid inSet opiskeluoikeudetOppilaitoksille).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid).mapValues(_.head)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.opiskeluoikeusOid inSet opiskeluoikeudetOppilaitoksille).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)

    opiskeluoikeudet.map { oo =>
      PerusopetukseenValmistavanRaporttiRows(
        opiskeluoikeus = oo,
        henkilo = henkilot(oo.oppijaOid),
        päätasonSuoritus = paatasonSuoritukset(oo.opiskeluoikeusOid),
        aikajaksot = aikajaksot(oo.opiskeluoikeusOid),
        osasuoritukset = Seq()
      )
    }

  }

  private def queryOpiskeluoikeusOids(oppilaitokset: Seq[String], alku: LocalDate, loppu: LocalDate): Seq[OpiskeluoikeusOid] = {
    implicit val getResult = GetResult(rs => (rs.nextString, rs.nextArray, rs.nextArray))

    val query =
      sql"""
     select
      oo.opiskeluoikeus_oid
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

    runDbSync(query.as[Tunnisteet], timeout = 5.minutes)
  }
}

case class PerusopetukseenValmistavanRaporttiRows(
   opiskeluoikeus: ROpiskeluoikeusRow,
   henkilo: RHenkilöRow,
   päätasonSuoritus: RPäätasonSuoritusRow,
   aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
   osasuoritukset: Seq[ROsasuoritusRow]
) extends YleissivistäväRaporttiRows
