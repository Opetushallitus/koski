package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportointikanta._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.Organisaatio
import slick.jdbc.GetResult


case class PerusopetuksenRaportitRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {

  def perusopetuksenvuosiluokka(oppilaitos: Organisaatio.Oid, paiva: LocalDate, vuosiluokka: String): List[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String])] = {
    val opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitos, paiva, vuosiluokka)

    val opiskeluoikeusOids = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.map(_._1)
    val paatasonSuoritusIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._2)
    val aikajaksoIds = opiskeluoikeusAikajaksotPaatasonsuorituksetTunnisteet.flatMap(_._3)

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = 5.minutes)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result).groupBy(_.päätasonSuoritusId)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)
    val voimassaOlevatVuosiluokat = runDbSync(voimassaOlevatVuosiluokatQuery(opiskeluoikeusOids).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).toSeq)

    opiskeluoikeudet.foldLeft[List[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String])]](List.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot, voimassaOlevatVuosiluokat)
    }
  }

  def peruskoulunPaattavatJaLuokalleJääneet(oppilaitos: Organisaatio.Oid, paiva: LocalDate, vuosiluokka: String) = {
    val luokalleJäävienTunnisteet = luokalleJäävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(oppilaitos, paiva, "9")
    val luokalleJaavienOidit = luokalleJäävienTunnisteet.map(_._1)

    val peruskoulunPäättävienTunnisteet = peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(oppilaitos, paiva, vuosiluokka, luokalleJaavienOidit.distinct)

    val opiskeluoikeusOids: Seq[String] = luokalleJaavienOidit.union(peruskoulunPäättävienTunnisteet.map(_._1))
    val paatasonSuoritusIds = luokalleJäävienTunnisteet.flatMap(_._2).union(peruskoulunPäättävienTunnisteet.flatMap(_._2))
    val aikajaksoIds = luokalleJäävienTunnisteet.flatMap(_._3).union(peruskoulunPäättävienTunnisteet.flatMap(_._3))

    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = 5.minutes)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result).groupBy(_.päätasonSuoritusId)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)
    val voimassaOlevatVuosiluokat = runDbSync(voimassaOlevatVuosiluokatQuery(opiskeluoikeusOids).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).toSeq)

    opiskeluoikeudet.foldLeft[List[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String])]](List.empty) {
      combineOpiskeluoikeusWith(_, _, aikajaksot, paatasonSuoritukset, osasuoritukset, henkilot, voimassaOlevatVuosiluokat)
    }
  }

  private def combineOpiskeluoikeusWith
  (
    acc: List[(ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String])],
    opiskeluoikeus: ROpiskeluoikeusRow,
    aikajaksot: Map[String, Seq[ROpiskeluoikeusAikajaksoRow]],
    paatasonsuorituskset: Map[String, Seq[RPäätasonSuoritusRow]],
    osasuoritukset: Map[Long, Seq[ROsasuoritusRow]],
    henkilot: Map[String, RHenkilöRow],
    voimassaOlevatVuosiluokat: Map[String, Seq[String]]
  ) = {
    (acc, opiskeluoikeus) match {
      case (tail, oo) => {
        val pts = paatasonsuorituskset.getOrElse(oo.opiskeluoikeusOid, List.empty)
        pts.map(paatasonsuoritus => {
          (
            oo,
            henkilot.get(oo.oppijaOid),
            aikajaksot.getOrElse(oo.opiskeluoikeusOid, List.empty),
            paatasonsuoritus,
            osasuoritukset.getOrElse(paatasonsuoritus.päätasonSuoritusId, List.empty),
            voimassaOlevatVuosiluokat.getOrElse(oo.opiskeluoikeusOid, List.empty)
          )
        }).toList ::: tail
      }
    }
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetResult(oppilaitos: Organisaatio.Oid, paiva: LocalDate, vuosiluokka: String) = {
    implicit val getResult = GetResult[(String, Seq[Long], Seq[Long])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos, Date.valueOf(paiva), vuosiluokka).as[(String, Seq[Long], Seq[Long])])
  }

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: String, paiva: Date, vuosiluokka: String) = {
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
      oo.koulutusmuoto = 'perusopetus' and
      pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
      pts.koulutusmoduuli_koodiarvo = $vuosiluokka and
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

  private def luokalleJäävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(oppilaitos: String, paiva: LocalDate, vuosiluokka: String) = {
    implicit val getResult = GetResult[(String, Seq[Long], Seq[Long])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(luokalleJäävätOpiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos, Date.valueOf(paiva), vuosiluokka).as[(String, Seq[Long], Seq[Long])])
  }

  private def luokalleJäävätOpiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitos: String, paiva: Date, vuosiluokka: String) = {
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
      oo.koulutusmuoto = 'perusopetus' and
      pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
      pts.koulutusmoduuli_koodiarvo = $vuosiluokka and
      ((pts.data->>'jääLuokalle')::boolean) and
      aikaj.alku <= $paiva and (aikaj.loppu >= $paiva or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""
  }

  private def peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetResult(oppilaitos: String, paiva: LocalDate, vuosiluokka: String, luokalleJaavat: Seq[String]) = {
    implicit val getResult = GetResult[(String, Seq[Long], Seq[Long])](pr => (pr.nextString(), pr.nextArray(), pr.nextArray()))
    runDbSync(peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos, Date.valueOf(paiva), vuosiluokka, luokalleJaavat).as[(String, Seq[Long], Seq[Long])])
  }

  private def peruskoulunPäättävätOpiskeluoikeusAikajaksotPäätasonSuorituksetQuery(oppilaitos: String, paiva: Date, vuosiluokka: String, luokalleJaavatOidit: Seq[String]) = {
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
      oo.koulutusmuoto = 'perusopetus' and
      not (oo.opiskeluoikeus_oid = any ($luokalleJaavatOidit)) and
      pts.suorituksen_tyyppi = 'perusopetuksenoppimaara' and
      aikaj.alku <= $paiva and (aikaj.loppu >= $paiva or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""
  }
}
