package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportointikanta._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.Organisaatio
import slick.jdbc.GetResult

case class AikuistenPerusopetusRaporttiRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {
  private val defaultTimeout = 5.minutes
  private type OpiskeluoikeusOid = String
  private type OppijaOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long

  def suoritustiedot(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean
  ): Seq[AikuistenPerusopetusRaporttiRows] = {
    def osasuoritusMukanaAikarajauksessa(row: ROsasuoritusRow) = {
      !osasuoritustenAikarajaus || arvioituAikavälillä(alku, loppu)(row)
    }

    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitosOid, alku, loppu)

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_._1)
    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = defaultTimeout)

    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._3)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._2)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout).groupBy(_.opiskeluoikeusOid)

    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = defaultTimeout)
      .filter(osasuoritusMukanaAikarajauksessa)
      .groupBy(_.päätasonSuoritusId)

    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result, timeout = defaultTimeout).groupBy(_.oppijaOid).mapValues(_.head)

    opiskeluoikeudet.foldLeft[Seq[AikuistenPerusopetusRaporttiRows]](Seq.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: Seq[AikuistenPerusopetusRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow]
  ) = {
    paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(paatasonsuoritus =>
      AikuistenPerusopetusRaporttiRows(
        opiskeluoikeus,
        henkilot(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        paatasonsuoritus,
        osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, Nil)
      )
    ) ++ acc
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitos: String, alku: LocalDate, loppu: LocalDate) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos, Date.valueOf(alku), Date.valueOf(loppu)).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: String, alku: Date, loppu: Date) = {
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
      oo.koulutusmuoto = 'aikuistenperusopetus' and
      aikaj.alku <= $loppu and (aikaj.loppu >= $alku or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""
  }
}

case class AikuistenPerusopetusRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow]
) extends YleissivistäväRaporttiRows
