package fi.oph.koski.raportit.aikuistenperusopetus

import java.time.LocalDate

import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportit.YleissivistäväRaporttiRows
import fi.oph.koski.db.DB
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import slick.jdbc.GetResult
import scala.concurrent.duration.DurationInt

case class AikuistenPerusopetusRaporttiRepository(
  db: DB
) extends QueryMethods with RaportointikantaTableQueries {
  private val defaultTimeout = 5.minutes
  private type OppijaOid = String
  private type OpiskeluoikeusOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long
  private type HasAlkuvaihe = Boolean
  private type HasPäättövaihe = Boolean

  def suoritustiedot(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritusMukanaAikarajauksessa: ROsasuoritusRow => Boolean,
    päätasonSuorituksenTyyppi: String
  ): Seq[AikuistenPerusopetusRaporttiRows] = {

    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitosOid, alku, loppu, päätasonSuorituksenTyyppi)
    val dataByOpiskeluoikeusOid = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.groupBy(_.opiskeluoikeusOid)

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_.opiskeluoikeusOid)
    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout)

    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_.aikajaksoIds)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_.päätasonSuoritusIds)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout)
      .filter(osasuoritusMukanaAikarajauksessa)
      .groupBy(_.päätasonSuoritusId)

    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result, timeout = defaultTimeout).groupBy(_.oppijaOid).view.mapValues(_.head).toMap

    opiskeluoikeudet.foldLeft[Seq[AikuistenPerusopetusRaporttiRows]](Seq.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot, dataByOpiskeluoikeusOid)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: Seq[AikuistenPerusopetusRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow],
    dataByOpiskeluoikeusOid: Map[OpiskeluoikeusOid, Seq[AikuistenPerusopetusRaporttiDbResultRow]]
  ) = {
    paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(paatasonsuoritus =>
      AikuistenPerusopetusRaporttiRows(
        opiskeluoikeus,
        henkilot(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        paatasonsuoritus,
        osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, Nil),
        hasAlkuvaihe = dataByOpiskeluoikeusOid(opiskeluoikeus.opiskeluoikeusOid).exists(_.hasAlkuvaihe),
        hasPäättövaihe = dataByOpiskeluoikeusOid(opiskeluoikeus.opiskeluoikeusOid).exists(_.hasPäättövaihe)
      )
    ) ++ acc
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(
    oppilaitos: String,
    alku: LocalDate,
    loppu: LocalDate,
    päätasonSuorituksenTyyppi: String
  ) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[AikuistenPerusopetusRaporttiDbResultRow](r =>
      AikuistenPerusopetusRaporttiDbResultRow(r.<<, r.<<, r.<<, r.<<, r.<<)
    )
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
      oppilaitos, alku, loppu, päätasonSuorituksenTyyppi
    ).as[AikuistenPerusopetusRaporttiDbResultRow], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
    oppilaitos: String,
    alku: LocalDate,
    loppu: LocalDate,
    päätasonSuorituksenTyyppi: String
  ) = {
    sql"""
     select
      oo.opiskeluoikeus_oid,
      array_agg(pts.paatason_suoritus_id),
      array_agg(aikaj.id),
      exists(
        select 1
        from r_paatason_suoritus pts_alkuvaihe
        where pts_alkuvaihe.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
        and pts_alkuvaihe.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe'
      ) as alkuvaihe,
      exists(
        select 1
        from r_paatason_suoritus pts_paattovaihe
        where pts_paattovaihe.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
        and pts_paattovaihe.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
      ) as paattovaihe
    from r_opiskeluoikeus oo
    join r_paatason_suoritus pts
      on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join r_opiskeluoikeus_aikajakso aikaj
      on aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    where
      oo.oppilaitos_oid = $oppilaitos
      and oo.koulutusmuoto = 'aikuistenperusopetus'
      and pts.suorituksen_tyyppi = $päätasonSuorituksenTyyppi
      and aikaj.alku <= $loppu and aikaj.loppu >= $alku
    group by oo.opiskeluoikeus_oid"""
  }

  case class AikuistenPerusopetusRaporttiDbResultRow(
    opiskeluoikeusOid: OpiskeluoikeusOid,
    päätasonSuoritusIds: Seq[PäätasonSuoritusId],
    aikajaksoIds: Seq[AikajaksoId],
    hasAlkuvaihe: HasAlkuvaihe,
    hasPäättövaihe: HasPäättövaihe
  )
}

case class AikuistenPerusopetusRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
  hasAlkuvaihe: Boolean,
  hasPäättövaihe: Boolean
) extends YleissivistäväRaporttiRows
