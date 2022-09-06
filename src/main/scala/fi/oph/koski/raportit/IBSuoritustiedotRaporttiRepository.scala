package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.DB
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.sqlDateOrdering
import slick.jdbc.{GetResult, SQLActionBuilder}

import scala.concurrent.duration.DurationInt

case class IBSuoritustiedotRaporttiRepository(
  db: DB
) extends QueryMethods with RaportointikantaTableQueries {
  private val defaultTimeout = 5.minutes
  private type OppijaOid = String
  private type OpiskeluoikeusOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long

  def suoritustiedot(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    päätasonSuorituksenTyyppi: String
  ): Seq[IBRaporttiRows] = {

    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitosOid, alku, loppu, päätasonSuorituksenTyyppi)

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_.opiskeluoikeusOid)
    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout)

    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_.aikajaksoIds)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_.päätasonSuoritusIds)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout)
      .filter(osasuoritus => !osasuoritustenAikarajaus || arvioituAikavälillä(alku, loppu)(osasuoritus))
      .groupBy(_.päätasonSuoritusId)

    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result, timeout = defaultTimeout).groupBy(_.oppijaOid).mapValues(_.head)

    opiskeluoikeudet.foldLeft[Seq[IBRaporttiRows]](Seq.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: Seq[IBRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow],
  ): Seq[IBRaporttiRows] = {
    paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(paatasonsuoritus =>
      IBRaporttiRows(
        opiskeluoikeus,
        henkilot(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        paatasonsuoritus,
        osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, Nil)
      )
    ) ++ acc
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(
    oppilaitos: String,
    alku: LocalDate,
    loppu: LocalDate,
    päätasonSuorituksenTyyppi: String
  ): Vector[IBRaporttiDbResultRow] = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[IBRaporttiDbResultRow](r =>
      IBRaporttiDbResultRow(r.<<, r.<<, r.<<)
    )
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
      oppilaitos, alku, loppu, päätasonSuorituksenTyyppi
    ).as[IBRaporttiDbResultRow], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
    oppilaitos: String,
    alku: LocalDate,
    loppu: LocalDate,
    päätasonSuorituksenTyyppi: String
  ): SQLActionBuilder = {
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
      oo.oppilaitos_oid = $oppilaitos and
      oo.koulutusmuoto = 'ibtutkinto' and
      pts.suorituksen_tyyppi = $päätasonSuorituksenTyyppi and
      aikaj.alku <= $loppu and
      aikaj.loppu >= $alku
    group by oo.opiskeluoikeus_oid"""
  }

  case class IBRaporttiDbResultRow(
    opiskeluoikeusOid: OpiskeluoikeusOid,
    päätasonSuoritusIds: Seq[PäätasonSuoritusId],
    aikajaksoIds: Seq[AikajaksoId],
  )
}

case class IBRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
) extends YleissivistäväRaporttiRows
