package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportointikanta._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.Organisaatio
import slick.jdbc.GetResult


case class PerusopetuksenRaportitRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {

  private type OpiskeluoikeusOid = String
  private type OppijaOid = String
  private type PäätasonSuoritusId = Long
  private type AikajaksoId = Long

  def perusopetuksenvuosiluokka(organisaatioOidit: Set[Organisaatio.Oid], paiva: LocalDate, vuosiluokka: String): List[PerusopetuksenRaporttiRows] = {
    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(organisaatioOidit, paiva, vuosiluokka)
    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_._1)
    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._2)
    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._3)

    suoritustiedot(opiskeluoikeusOids, paatasonSuoritusIds, aikajaksoIds, vuosiluokka)
  }

  def peruskoulunPaattavatJaLuokalleJääneet(organisaatioOidit: Set[Organisaatio.Oid], paiva: LocalDate, vuosiluokka: String): List[PerusopetuksenRaporttiRows] = {
    val luokalleJäävienTunnisteet = luokalleJäävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(organisaatioOidit, paiva, vuosiluokka)
    val luokalleJaavienOidit = luokalleJäävienTunnisteet.map(_._1)
    val peruskoulunPäättävienTunnisteet = peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(organisaatioOidit, paiva, vuosiluokka, luokalleJaavienOidit.distinct)
    val opiskeluoikeusOids = luokalleJaavienOidit.union(peruskoulunPäättävienTunnisteet.map(_._1))
    val paatasonSuoritusIds = luokalleJäävienTunnisteet.flatMap(_._2).union(peruskoulunPäättävienTunnisteet.flatMap(_._2))
    val aikajaksoIds = luokalleJäävienTunnisteet.flatMap(_._3).union(peruskoulunPäättävienTunnisteet.flatMap(_._3))

    suoritustiedot(opiskeluoikeusOids, paatasonSuoritusIds, aikajaksoIds, vuosiluokka)
  }

  private def suoritustiedot(opiskeluoikeusOids: Seq[OpiskeluoikeusOid], paatasonSuoritusIds: Seq[PäätasonSuoritusId], aikajaksoIds: Seq[AikajaksoId], vuosiluokka: String) = {
    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = 5.minutes)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result).groupBy(_.päätasonSuoritusId)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)
    val voimassaOlevatVuosiluokat = runDbSync(voimassaOlevatVuosiluokatQuery(opiskeluoikeusOids).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).toSeq)
    val luokat = runDbSync(luokkatiedotVuosiluokalleQuery(opiskeluoikeusOids, vuosiluokka).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).distinct.sorted.mkString(","))

    opiskeluoikeudet.foldLeft[List[PerusopetuksenRaporttiRows]](List.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot, voimassaOlevatVuosiluokat, luokat)
    }
  }

  private def combineOpiskeluoikeusWith(
    acc: List[PerusopetuksenRaporttiRows],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[OpiskeluoikeusOid, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonSuoritukset: Map[OpiskeluoikeusOid, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[PäätasonSuoritusId, Seq[ROsasuoritusRow]],
    henkilot: Map[OppijaOid, RHenkilöRow],
    voimassaOlevatVuosiluokat: Map[OpiskeluoikeusOid, Seq[String]],
    luokat: Map[OpiskeluoikeusOid, String]
  ) = {
    val pts = paatasonSuoritukset.getOrElse(opiskeluoikeus.opiskeluoikeusOid, List.empty)
    pts.map(paatasonsuoritus => {
      PerusopetuksenRaporttiRows(
        opiskeluoikeus = opiskeluoikeus,
        henkilo = henkilot.get(opiskeluoikeus.oppijaOid),
        aikajaksot = aikajaksot.getOrElse(opiskeluoikeus.opiskeluoikeusOid, List.empty).sortBy(_.alku)(sqlDateOrdering),
        päätasonSuoritus = paatasonsuoritus,
        osasuoritukset = osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, List.empty),
        voimassaolevatVuosiluokat = voimassaOlevatVuosiluokat.getOrElse(opiskeluoikeus.opiskeluoikeusOid, List.empty),
        luokka = luokat.get(opiskeluoikeus.opiskeluoikeusOid)
      )
    }).toList ::: acc
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitos: Set[Organisaatio.Oid], paiva: LocalDate, vuosiluokka: String) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos.toSeq, Date.valueOf(paiva), vuosiluokka).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = 5.minutes)
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: Seq[String], paiva: Date, vuosiluokka: String) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    sql"""
     select
      oo.opiskeluoikeus_oid,
      array_agg(pts.paatason_suoritus_id),
      array_agg(aikaj.id)
    from
      r_opiskeluoikeus oo
    join
      r_paatason_suoritus pts
    on
      pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join
      r_opiskeluoikeus_aikajakso aikaj
    on
      aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    where
      oo.oppilaitos_oid = any ($oppilaitos) and
      oo.koulutusmuoto = 'perusopetus' and
      pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
      pts.koulutusmoduuli_koodiarvo = $vuosiluokka and
      (pts.vahvistus_paiva >= $paiva or pts.vahvistus_paiva is null) and
      aikaj.alku <= $paiva and (aikaj.loppu >= $paiva or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""
  }

  private def voimassaOlevatVuosiluokatQuery(opiskeluoikeusOids: Seq[String]) = {
    RPäätasonSuoritukset
      .filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids)
      .filter(_.suorituksenTyyppi === "perusopetuksenvuosiluokka")
      .filter(_.vahvistusPäivä.isEmpty)
      .map(row => (row.opiskeluoikeusOid, row.koulutusmoduuliKoodiarvo))
  }

  private def luokkatiedotVuosiluokalleQuery(opiskeluoikeusOid: Seq[String], vuosiluokka: String) = {
    RPäätasonSuoritukset
      .filter(_.opiskeluoikeusOid inSet opiskeluoikeusOid)
      .filter(_.suorituksenTyyppi === "perusopetuksenvuosiluokka")
      .filter(_.koulutusmoduuliKoodiarvo === vuosiluokka)
      .map(row => (row.opiskeluoikeusOid, row.data+>>("luokka")))
  }

  private def luokalleJäävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(oppilaitos: Set[String], paiva: LocalDate, vuosiluokka: String) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(luokalleJäävätOpiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos.toSeq, Date.valueOf(paiva), vuosiluokka).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = 5.minutes)
  }

  private def luokalleJäävätOpiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: Seq[String], paiva: Date, vuosiluokka: String) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    sql"""
     select
      oo.opiskeluoikeus_oid,
      array_agg(pts.paatason_suoritus_id),
      array_agg(aikaj.id)
    from
      r_opiskeluoikeus oo
    join
      r_paatason_suoritus pts
    on
      pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join
      r_opiskeluoikeus_aikajakso aikaj
    on
      aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    where
      oo.oppilaitos_oid = any($oppilaitos) and
      oo.koulutusmuoto = 'perusopetus' and
      pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
      pts.koulutusmoduuli_koodiarvo = '9' and
      (pts.data->>'jääLuokalle')::boolean and
      (pts.vahvistus_paiva is null or pts.vahvistus_paiva >= $paiva) and
      aikaj.alku <= $paiva and (aikaj.loppu >= $paiva or aikaj.loppu is null)
    group by
      oo.opiskeluoikeus_oid"""
  }

  private def peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(oppilaitos: Set[String], paiva: LocalDate, vuosiluokka: String, luokalleJaavat: Seq[String]) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    implicit val getResult = GetResult[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos.toSeq, Date.valueOf(paiva), vuosiluokka, luokalleJaavat).as[(OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])], timeout = 5.minutes)
  }

  private def peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos: Seq[String], paiva: Date, vuosiluokka: String, luokalleJaavatOidit: Seq[String]) = {
    import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
    sql"""
      with koulun_paattavat_ysiluokkalaiset as (
        select
          oo.opiskeluoikeus_oid
        from
          r_opiskeluoikeus oo
        inner join
          r_paatason_suoritus pts
        on
          pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
        where
          oo.oppilaitos_oid = any ($oppilaitos) and
          oo.koulutusmuoto = 'perusopetus' and
          not (oo.opiskeluoikeus_oid = any ($luokalleJaavatOidit)) and
          pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
          pts.koulutusmoduuli_koodiarvo = '9' and
          (pts.vahvistus_paiva is null or pts.vahvistus_paiva >= $paiva)
      )
      select
        oo.opiskeluoikeus_oid,
        array_agg(pts.paatason_suoritus_id),
        array_agg(aikaj.id)
      from
        koulun_paattavat_ysiluokkalaiset oo
      join
        r_opiskeluoikeus_aikajakso aikaj
      on
        aikaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join
        r_paatason_suoritus pts
      on
        pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where
        pts.suorituksen_tyyppi = 'perusopetuksenoppimaara' and
        (pts.vahvistus_paiva is null or pts.vahvistus_paiva >= $paiva) and
        aikaj.alku <= $paiva and (aikaj.loppu >= $paiva or aikaj.loppu is null)
      group by
        oo.opiskeluoikeus_oid"""
  }
}

case class PerusopetuksenRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: Option[RHenkilöRow],
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
  voimassaolevatVuosiluokat: Seq[String],
  luokka: Option[String]
)
