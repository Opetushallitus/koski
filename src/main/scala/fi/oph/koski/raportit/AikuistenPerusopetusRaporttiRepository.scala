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

case class AikuistenPerusopetusRaporttiRepository(
  db: DB
) extends KoskiDatabaseMethods with RaportointikantaTableQueries {
  private val defaultTimeout = 5.minutes
  private type OpiskeluoikeusOid = String
  private type OppijaOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long
  private type HasAlkuvaihe = Boolean
  private type HasPäättövaihe = Boolean

  def suoritustiedot(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    päätasonSuorituksenTyyppi: String
  ): Seq[AikuistenPerusopetusRaporttiRows] = {
    def osasuoritusMukanaAikarajauksessa(row: ROsasuoritusRow) = {
      !osasuoritustenAikarajaus || arvioituAikavälillä(alku, loppu)(row)
    }

    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitosOid, alku, loppu, päätasonSuorituksenTyyppi)
    val hasAlkuvaihePäättövaihe = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet
      .map(data => (data._1, data._4, data._5))
      .groupBy(_._1)

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
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot, hasAlkuvaihePäättövaihe)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: Seq[AikuistenPerusopetusRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow],
    hasAlkuvaihePäättövaihe: Map[OpiskeluoikeusOid, Vector[(OpiskeluoikeusOid, HasAlkuvaihe, HasPäättövaihe)]]
  ) = {
    paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).map(paatasonsuoritus =>
      AikuistenPerusopetusRaporttiRows(
        opiskeluoikeus,
        henkilot(opiskeluoikeus.oppijaOid),
        aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
        paatasonsuoritus,
        osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, Nil),
        hasAlkuvaihe = hasAlkuvaihePäättövaihe(opiskeluoikeus.opiskeluoikeusOid).exists(_._2),
        hasPäättövaihe = hasAlkuvaihePäättövaihe(opiskeluoikeus.opiskeluoikeusOid).exists(_._3)
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
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
      oppilaitos,
      Date.valueOf(alku),
      Date.valueOf(loppu),
      päätasonSuorituksenTyyppi
    ).as[(
      OpiskeluoikeusOid,
      Seq[PäätasonSuoritusId],
      Seq[AikajaksoId],
      HasAlkuvaihe,
      HasPäättövaihe
    )], timeout = defaultTimeout)
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(
    oppilaitos: String,
    alku: Date,
    loppu: Date,
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
      and aikaj.alku <= $loppu and (aikaj.loppu >= $alku or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""
  }
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
