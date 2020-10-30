package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.raportointikanta._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.util.DateOrdering.sqlDateOrdering

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.util.SQL.setLocalDate
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Organisaatio
import org.json4s.jackson.JsonMethods
import slick.jdbc.GetResult

case class PerusopetuksenRaportitRepository(db: DB) extends KoskiDatabaseMethods with RaportointikantaTableQueries {

  type OpiskeluoikeusOid = String
  type PäätasonSuoritusId = Long
  type AikajaksoId = Long
  type Tunnisteet = (OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])

  def perusopetuksenvuosiluokka(organisaatioOidit: Set[Organisaatio.Oid], päivä: LocalDate, vuosiluokka: String): Seq[PerusopetuksenRaporttiRows] = {
    val potentiaalisestiKeskeyttäneet = mahdollisestiKeskeyttäneet(organisaatioOidit, päivä, vuosiluokka)
    val tunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(organisaatioOidit.toSeq, päivä, vuosiluokka).union(potentiaalisestiKeskeyttäneet)
    val opiskeluoikeusOids = tunnisteet.map(_._1)
    val paatasonSuoritusIds = tunnisteet.flatMap(_._2)
    val aikajaksoIds = tunnisteet.flatMap(_._3)

    suoritustiedot(päivä, opiskeluoikeusOids, paatasonSuoritusIds, aikajaksoIds, vuosiluokka)
  }

  def peruskoulunPaattavatJaLuokalleJääneet(organisaatioOidit: Set[Organisaatio.Oid], päivä: LocalDate, vuosiluokka: String): Seq[PerusopetuksenRaporttiRows] = {
    val luokalleJäävienTunnisteet = queryLuokalleJäävienTunnisteet(organisaatioOidit.toSeq, päivä)
    val luokalleJaavienOidit = luokalleJäävienTunnisteet.map(_._1)
    val peruskoulunPäättävienTunnisteet = queryPeruskoulunPäättäneidenTunnisteet(organisaatioOidit.toSeq, päivä, luokalleJaavienOidit.distinct)
    val opiskeluoikeusOids = luokalleJaavienOidit.union(peruskoulunPäättävienTunnisteet.map(_._1))
    val paatasonSuoritusIds = luokalleJäävienTunnisteet.flatMap(_._2).union(peruskoulunPäättävienTunnisteet.flatMap(_._2))
    val aikajaksoIds = luokalleJäävienTunnisteet.flatMap(_._3).union(peruskoulunPäättävienTunnisteet.flatMap(_._3))

    suoritustiedot(päivä, opiskeluoikeusOids, paatasonSuoritusIds, aikajaksoIds, vuosiluokka)
  }

  private def suoritustiedot(
    päivä: LocalDate,
    opiskeluoikeusOids: Seq[OpiskeluoikeusOid],
    paatasonSuoritusIds: Seq[PäätasonSuoritusId],
    aikajaksoIds: Seq[AikajaksoId],
    vuosiluokka: String
  ) = {
    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = 5.minutes)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result).groupBy(_.päätasonSuoritusId)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)
    val voimassaOlevatVuosiluokat = runDbSync(voimassaOlevatVuosiluokatQuery(opiskeluoikeusOids).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).toSeq)
    val luokat = runDbSync(luokkatiedotVuosiluokalleQuery(opiskeluoikeusOids, vuosiluokka).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).distinct.sorted.mkString(","))

    opiskeluoikeudet.flatMap { oo =>
      paatasonSuoritukset.getOrElse(oo.opiskeluoikeusOid, Nil).map { päätasonSuoritus =>
        PerusopetuksenRaporttiRows(
          opiskeluoikeus = oo,
          henkilo = henkilot(oo.oppijaOid),
          aikajaksot = aikajaksot.getOrElse(oo.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
          päätasonSuoritus = päätasonSuoritus,
          osasuoritukset.getOrElse(päätasonSuoritus.päätasonSuoritusId, Nil),
          voimassaOlevatVuosiluokat.getOrElse(oo.opiskeluoikeusOid, Nil),
          luokat.get(oo.opiskeluoikeusOid)
        )
      }
    }
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

  private def opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(oppilaitokset: Seq[String], päivä: LocalDate, vuosiluokka: String) = {
    implicit val getResult = GetResult(rs => (rs.nextString, rs.nextArray, rs.nextArray))

    val query = sql"""
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
      oo.oppilaitos_oid = any ($oppilaitokset) and
      oo.koulutusmuoto = 'perusopetus' and
      pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
      pts.koulutusmoduuli_koodiarvo = $vuosiluokka and
      (pts.vahvistus_paiva >= $päivä or pts.vahvistus_paiva is null) and
      (pts.data->>'alkamispäivä' <= $päivä or pts.data->>'alkamispäivä' is null) and
      aikaj.alku <= $päivä and (aikaj.loppu >= $päivä or aikaj.loppu is null)
    group by oo.opiskeluoikeus_oid"""

    runDbSync(query.as[Tunnisteet], timeout = 5.minutes)
  }

  private def queryLuokalleJäävienTunnisteet(oppilaitokset: Seq[String], päivä: LocalDate) = {
    implicit val getResult = GetResult(rs => (rs.nextString, rs.nextArray, rs.nextArray))

    val query = sql"""
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
      oo.oppilaitos_oid = any($oppilaitokset) and
      oo.koulutusmuoto = 'perusopetus' and
      pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
      pts.koulutusmoduuli_koodiarvo = '9' and
      (pts.data->>'jääLuokalle')::boolean and
      (pts.vahvistus_paiva is null or pts.vahvistus_paiva >= $päivä) and
      (pts.data->>'alkamispäivä' <= $päivä or pts.data->>'alkamispäivä' is null) and
      aikaj.alku <= $päivä and (aikaj.loppu >= $päivä or aikaj.loppu is null)
    group by
      oo.opiskeluoikeus_oid"""

    runDbSync(query.as[Tunnisteet], timeout = 5.minutes)
  }

  private def queryPeruskoulunPäättäneidenTunnisteet(oppilaitokset: Seq[String], päivä: LocalDate, luokalleJaavatOidit: Seq[String]) = {
    implicit val getResult = GetResult(rs => (rs.nextString, rs.nextArray, rs.nextArray))

    val query = sql"""
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
          oo.oppilaitos_oid = any ($oppilaitokset) and
          oo.koulutusmuoto = 'perusopetus' and
          not (oo.opiskeluoikeus_oid = any ($luokalleJaavatOidit)) and
          pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
          pts.koulutusmoduuli_koodiarvo = '9' and
          (pts.vahvistus_paiva is null or pts.vahvistus_paiva >= $päivä) and
          (pts.data->>'alkamispäivä' <= $päivä or pts.data->>'alkamispäivä' is null)
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
        (pts.vahvistus_paiva is null or pts.vahvistus_paiva >= $päivä) and
        aikaj.alku <= $päivä and (aikaj.loppu >= $päivä or aikaj.loppu is null)
      group by
        oo.opiskeluoikeus_oid"""

    runDbSync(query.as[Tunnisteet], timeout = 5.minutes)
  }

  // Tarkoituksena hakea tapauksia joilla on vuosiluokka valmiina, ei ole merkitty seuraavalle luokalle, eikä niiden opintoja ole merkitty päättyviksi.
  // Nämä mahdollisesti keskeyttäneet halutaan saada näkyviin oppilaitoksille, koska ne ovat potentiaalisesti virheellisiä.
  private def mahdollisestiKeskeyttäneet(oppilaitokset: Set[Organisaatio.Oid], päivä: LocalDate, vuosiluokka: String): Seq[Tunnisteet] = {
    aktiivistenSuoritukset(oppilaitokset.toSeq, päivä).flatMap {
      case (opiskeluoikeusOid, vuosiluokkienTiedot, aikajaksoIds) => {
        val vahvistettujenVuosiluokkienIds = vuosiluokkienTiedot
          .takeWhile { x => x.vuosiluokka == vuosiluokka && x.vahvistuspäivä.isDefined }
          .map(_.id)

        vahvistettujenVuosiluokkienIds match {
          case Nil => None
          case ids => Some((opiskeluoikeusOid, ids, aikajaksoIds))
        }
      }
    }
  }

  private def aktiivistenSuoritukset(oppilaitokset: Seq[String], päivä: LocalDate) = {
    queryAktiivisetSuoritukset(oppilaitokset, päivä)
      .map { case (opiskeluoikeusOid, jsonStrings, aikajaksoIds) =>
        val vuosiluokkienTiedot = jsonStrings
          .map(JsonMethods.parse(_))
          .map(JsonSerializer.extract[VuosiluokanTiedot](_))

        (opiskeluoikeusOid, vuosiluokkienTiedot, aikajaksoIds)
      }
  }

  private def queryAktiivisetSuoritukset(oppilaitokset: Seq[String], päivä: LocalDate) = {
    implicit val getResult = GetResult(rs => (rs.nextString, rs.nextArray, rs.nextArray))

    val query = sql"""
      select
        oo.opiskeluoikeus_oid,
        array_agg(
          json_build_object(
            'id', pts.paatason_suoritus_id,
            'vahvistuspäivä', pts.vahvistus_paiva,
            'vuosiluokka', pts.koulutusmoduuli_koodiarvo
          )
          order by pts.koulutusmoduuli_koodiarvo::integer desc, pts.vahvistus_paiva  desc nulls first
        )::jsonb[],
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
        oo.oppilaitos_oid = any($oppilaitokset) and
        oo.koulutusmuoto = 'perusopetus' and
        pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka' and
        aikaj.alku <= $päivä and (aikaj.loppu >= $päivä or aikaj.loppu is null)
      group by oo.opiskeluoikeus_oid"""

    runDbSync(query.as[(OpiskeluoikeusOid, Seq[String], Seq[AikajaksoId])], timeout = 5.minutes)
  }
}

case class VuosiluokanTiedot(
   id: Long,
   vahvistuspäivä: Option[LocalDate],
   vuosiluokka: String
)

case class PerusopetuksenRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
  voimassaolevatVuosiluokat: Seq[String],
  luokka: Option[String]
)
