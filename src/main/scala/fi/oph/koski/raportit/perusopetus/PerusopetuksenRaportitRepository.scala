package fi.oph.koski.raportit.perusopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.util.DateOrdering.sqlDateOrdering
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import org.json4s.jackson.JsonMethods
import slick.jdbc.GetResult

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class PerusopetuksenRaportitRepository(db: DB) extends QueryMethods with RaportointikantaTableQueries {

  type OpiskeluoikeusOid = String
  type PäätasonSuoritusId = Long
  type AikajaksoId = Long
  type Tunnisteet = (OpiskeluoikeusOid, Seq[PäätasonSuoritusId], Seq[AikajaksoId])

  def perusopetuksenvuosiluokka(
    organisaatioOidit: Seq[Organisaatio.Oid],
    päivä: LocalDate,
    vuosiluokka: String,
    t: LocalizationReader
  ): Seq[PerusopetuksenRaporttiRows] = {
    val potentiaalisestiKeskeyttäneet = mahdollisestiKeskeyttäneet(organisaatioOidit, päivä, vuosiluokka)
    val tunnisteet = opiskeluoikeusAikajaksotPaatasonSuorituksetQuery(organisaatioOidit, päivä, vuosiluokka).union(potentiaalisestiKeskeyttäneet)
    val opiskeluoikeusOids = tunnisteet.map(_._1)
    val paatasonSuoritusIds = tunnisteet.flatMap(_._2)
    val aikajaksoIds = tunnisteet.flatMap(_._3)

    suoritustiedot(päivä, opiskeluoikeusOids, paatasonSuoritusIds, aikajaksoIds, vuosiluokka, t)
  }

  def peruskoulunPaattavatJaLuokalleJääneet(
    organisaatioOidit: Seq[Organisaatio.Oid],
    päivä: LocalDate,
    vuosiluokka: String,
    t: LocalizationReader
  ): Seq[PerusopetuksenRaporttiRows] = {
    val luokalleJäävienTunnisteet = queryLuokalleJäävienTunnisteet(organisaatioOidit, päivä)
    val luokalleJaavienOidit = luokalleJäävienTunnisteet.map(_._1)
    val peruskoulunPäättävienTunnisteet = queryPeruskoulunPäättäneidenTunnisteet(organisaatioOidit, päivä, luokalleJaavienOidit.distinct)
    val opiskeluoikeusOids = luokalleJaavienOidit.union(peruskoulunPäättävienTunnisteet.map(_._1))
    val paatasonSuoritusIds = luokalleJäävienTunnisteet.flatMap(_._2).union(peruskoulunPäättävienTunnisteet.flatMap(_._2))
    val aikajaksoIds = luokalleJäävienTunnisteet.flatMap(_._3).union(peruskoulunPäättävienTunnisteet.flatMap(_._3))

    suoritustiedot(päivä, opiskeluoikeusOids, paatasonSuoritusIds, aikajaksoIds, vuosiluokka, t)
  }

  private def suoritustiedot(
    päivä: LocalDate,
    opiskeluoikeusOids: Seq[OpiskeluoikeusOid],
    paatasonSuoritusIds: Seq[PäätasonSuoritusId],
    aikajaksoIds: Seq[AikajaksoId],
    vuosiluokka: String,
    t: LocalizationReader
  ) = {
    val opiskeluoikeudet = runDbSync(ROpiskeluoikeudet.filter(_.opiskeluoikeusOid inSet opiskeluoikeusOids).result, timeout = 5.minutes)
    val aikajaksot = runDbSync(ROpiskeluoikeusAikajaksot.filter(_.id inSet aikajaksoIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val paatasonSuoritukset = runDbSync(RPäätasonSuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result, timeout = 5.minutes).groupBy(_.opiskeluoikeusOid)
    val osasuoritukset = runDbSync(ROsasuoritukset.filter(_.päätasonSuoritusId inSet paatasonSuoritusIds).result).groupBy(_.päätasonSuoritusId)
    val henkilot = runDbSync(RHenkilöt.filter(_.oppijaOid inSet opiskeluoikeudet.map(_.oppijaOid).distinct).result).groupBy(_.oppijaOid).mapValues(_.head)
    val voimassaOlevatVuosiluokat = runDbSync(voimassaOlevatVuosiluokatQuery(opiskeluoikeusOids).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).toSeq)
    val luokat = runDbSync(luokkatiedotVuosiluokalleQuery(opiskeluoikeusOids, vuosiluokka).result, timeout = 5.minutes).groupBy(_._1).mapValues(_.map(_._2).distinct.sorted.mkString(","))
    val organisaatiohistoriat = fetchOrganisaatiohistoriat(päivä, opiskeluoikeusOids, t.language).groupBy(_.opiskeluoikeusOid)

    opiskeluoikeudet.flatMap { oo =>
      paatasonSuoritukset.getOrElse(oo.opiskeluoikeusOid, Nil).map { päätasonSuoritus =>
        PerusopetuksenRaporttiRows(
          opiskeluoikeus = oo,
          henkilo = henkilot(oo.oppijaOid),
          aikajaksot = aikajaksot.getOrElse(oo.opiskeluoikeusOid, Nil).sortBy(_.alku)(sqlDateOrdering),
          päätasonSuoritus = päätasonSuoritus,
          osasuoritukset = osasuoritukset.getOrElse(päätasonSuoritus.päätasonSuoritusId, Nil),
          voimassaolevatVuosiluokat = voimassaOlevatVuosiluokat.getOrElse(oo.opiskeluoikeusOid, Nil),
          luokka = luokat.get(oo.opiskeluoikeusOid),
          organisaatiohistoriaResult = organisaatiohistoriat.getOrElse(oo.opiskeluoikeusOid, Nil)
        )
      }
    }
  }

  private def fetchOrganisaatiohistoriat(päivä: LocalDate, opiskeluoikeusOids: Seq[String], lang: String) = {
    implicit val getResult: GetResult[OrganisaatiohistoriaResult] = GetResult(r => OrganisaatiohistoriaResult(
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid"),
      alku = r.getLocalDate("alku"),
      loppu = r.getLocalDate("loppu"),
      oppilaitosOid = r.rs.getString("oppilaitos_oid"),
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      koulutustoimijaOid = r.rs.getString("koulutustoimija_oid"),
      koulutustoimijaNimi = r.rs.getString("koulutustoimija_nimi")
    ))
    val organisaatioNimiSarake = if(lang == "sv") "nimi_sv" else "nimi"
    val query = sql"""
      select
        oh.opiskeluoikeus_oid,
        alku,
        loppu,
        oppilaitos.organisaatio_oid as oppilaitos_oid,
        oppilaitos.#$organisaatioNimiSarake as oppilaitos_nimi,
        koulutustoimija.organisaatio_oid as koulutustoimija_oid,
        koulutustoimija.#$organisaatioNimiSarake as koulutustoimija_nimi
      from r_organisaatiohistoria oh
      join r_organisaatio oppilaitos on oppilaitos_oid = oppilaitos.organisaatio_oid
      join r_organisaatio koulutustoimija on koulutustoimija_oid = koulutustoimija.organisaatio_oid
      where opiskeluoikeus_oid = any($opiskeluoikeusOids)
        and alku <= $päivä
        and loppu >= $päivä
    """
    runDbSync(query.as[OrganisaatiohistoriaResult])
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
      aikaj.alku <= $päivä and aikaj.loppu >= $päivä
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
      aikaj.alku <= $päivä and aikaj.loppu >= $päivä
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
        aikaj.alku <= $päivä and aikaj.loppu >= $päivä
      group by
        oo.opiskeluoikeus_oid"""

    runDbSync(query.as[Tunnisteet], timeout = 5.minutes)
  }

  // Tarkoituksena hakea tapauksia joilla on vuosiluokka valmiina, ei ole merkitty seuraavalle luokalle, eikä niiden opintoja ole merkitty päättyviksi.
  // Nämä mahdollisesti keskeyttäneet halutaan saada näkyviin oppilaitoksille, koska ne ovat potentiaalisesti virheellisiä.
  private def mahdollisestiKeskeyttäneet(oppilaitokset: Seq[Organisaatio.Oid], päivä: LocalDate, vuosiluokka: String): Seq[Tunnisteet] = {
    aktiivistenSuoritukset(oppilaitokset, päivä).flatMap {
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
        aikaj.alku <= $päivä and aikaj.loppu >= $päivä
      group by oo.opiskeluoikeus_oid"""

    runDbSync(query.as[(OpiskeluoikeusOid, Seq[String], Seq[AikajaksoId])], timeout = 5.minutes)
  }
}

case class VuosiluokanTiedot(
   id: Long,
   vahvistuspäivä: Option[LocalDate],
   vuosiluokka: String
)

case class OrganisaatiohistoriaResult(
  opiskeluoikeusOid: String,
  alku: LocalDate,
  loppu: LocalDate,
  oppilaitosOid: String,
  oppilaitosNimi: String,
  koulutustoimijaOid: String,
  koulutustoimijaNimi: String
) {
  override def toString: String = (
    s"${finnishDateFormat.format(alku)}-${finnishDateFormat.format(loppu)}: ${oppilaitosNimi} (${koulutustoimijaNimi})"
  )
}

case class PerusopetuksenRaporttiRows(
  opiskeluoikeus: ROpiskeluoikeusRow,
  henkilo: RHenkilöRow,
  aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow],
  päätasonSuoritus: RPäätasonSuoritusRow,
  osasuoritukset: Seq[ROsasuoritusRow],
  voimassaolevatVuosiluokat: Seq[String],
  luokka: Option[String],
  organisaatiohistoriaResult: Seq[OrganisaatiohistoriaResult]
)
