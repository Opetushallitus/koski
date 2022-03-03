package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.DB
import fi.oph.koski.localization.LocalizationReader
import slick.jdbc.GetResult

import scala.concurrent.duration.DurationInt

case class MuuAmmatillinenRaporttiBuilder(db: DB) extends QueryMethods {

  implicit val getResult = GetResult(r =>
    MuuAmmatillinenRaporttiRow(
      opiskeluoikeusOid = r.<<,
      sisältyyOpiskeluoikeuteenOid = r.<<,
      lähdejärjestelmäKoodiarvo = r.<<,
      lähdejärjestelmäId = r.<<,
      aikaleima = r.nextTimestamp.toLocalDateTime.toLocalDate,
      toimipisteOid = r.<<,
      suorituksenNimi = r.<<,
      opiskeluoikeudenAlkamispäivä = r.nextDate.toLocalDate,
      opiskeluoikeudenViimeisinTila = r.<<,
      yksilöity = r.<<,
      oppijaOid = r.<<,
      hetu = r.<<,
      etunimet = r.<<,
      sukunimi = r.<<,
      suoritutettujenOsasuoritustenLkm = r.<<,
      keskeneräistenOsasuoritustenLkm = r.<<,
      kaikkienOsasuoritustenYhteislaajuus = r.<<,
      kaikkienOsasuoritustenLaajuudenYksiköt = r.<<,
      suoritettujenYhteistenTutkinnonOsienOsaalueidenLkm = r.<<,
      suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm = r.<<,
      suoritettujenMuuAmmatillisenKoulutuksenOsasuoritustenLkm = r.<<
    )
  )

  def build(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): DataSheet = {
    val rows = runDbSync(queryMuuAmmatillisenSuoritukset(oppilaitosOid, alku, loppu, t.language).as[MuuAmmatillinenRaporttiRow], timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-muu-ammatillinen-sheet-name"),
      rows,
      columnSettings(t)
    )
  }

  private def queryMuuAmmatillisenSuoritukset(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, lang: String) = sql"""
    with oppilaitoksen_opiskeluoikeudet_ja_paatason_suoritukset as (
      select
        oo.opiskeluoikeus_oid,
        oo.oppija_oid,
        oo.sisaltyy_opiskeluoikeuteen_oid,
        oo.oppilaitos_nimi,
        oo.oppilaitos_oid,
        oo.viimeisin_tila,
        oo.alkamispaiva opiskeluoikeuden_alkamispaiva,
        oo.viimeisin_tila opiskeluoikeuden_viimeisin_tila,
        oo.lahdejarjestelma_koodiarvo,
        oo.lahdejarjestelma_id,
        oo.aikaleima,
        pts.paatason_suoritus_id,
        COALESCE(pts.data -> 'koulutusmoduuli' -> 'tunniste' -> 'nimi' ->> $lang, pts.koulutusmoduuli_nimi) as koulutusmoduuli_nimi,
        pts.vahvistus_paiva,
        pts.toimipiste_nimi,
        pts.toimipiste_oid
      from r_opiskeluoikeus oo
      join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where oppilaitos_oid = $oppilaitosOid
        and pts.suorituksen_tyyppi = 'muuammatillinenkoulutus'
        and oo.alkamispaiva <= $loppu
        and (oo.paattymispaiva is null or oo.paattymispaiva >= $alku)
    ),

    sisaltyvat_opiskeluoikeudet_ja_paatason_suoritukset as (
      select
        oo.opiskeluoikeus_oid,
        oo.oppija_oid,
        oo.sisaltyy_opiskeluoikeuteen_oid,
        oo.oppilaitos_nimi,
        oo.oppilaitos_oid,
        oo.viimeisin_tila,
        oo.alkamispaiva opiskeluoikeuden_alkamispaiva,
        oo.viimeisin_tila opiskeluoikeuden_viimeisin_tila,
        oo.lahdejarjestelma_koodiarvo,
        oo.lahdejarjestelma_id,
        oo.aikaleima,
        pts.paatason_suoritus_id,
        COALESCE(pts.data -> 'koulutusmoduuli' -> 'tunniste' -> 'nimi' ->> $lang, pts.koulutusmoduuli_nimi) as koulutusmoduuli_nimi,
        pts.vahvistus_paiva,
        pts.toimipiste_nimi,
        pts.toimipiste_oid
      from r_opiskeluoikeus oo
      join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      where oo.sisaltyy_opiskeluoikeuteen_oid in (select opiskeluoikeus_oid from oppilaitoksen_opiskeluoikeudet_ja_paatason_suoritukset)
        and pts.suorituksen_tyyppi = 'muuammatillinenkoulutus'
    ),

    opiskeluoikeudet_ja_paatason_suoritukset as (
      select * from oppilaitoksen_opiskeluoikeudet_ja_paatason_suoritukset
      union all
      select * from sisaltyvat_opiskeluoikeudet_ja_paatason_suoritukset
    ),

    osasuoritukset as (
      select * from muu_ammatillinen_raportointi where paatason_suoritus_id in (select paatason_suoritus_id from opiskeluoikeudet_ja_paatason_suoritukset)
    ),

    suoritettujen_osasuoritusten_lukumäärä as (
      select paatason_suoritus_id, count(*) as lkm from osasuoritukset where arviointi_hyvaksytty group by paatason_suoritus_id
    ),

    keskeneräisten_osasuoritusten_lukumäärä as (
      select paatason_suoritus_id, count(*) as lkm from osasuoritukset where not arviointi_hyvaksytty group by paatason_suoritus_id
    ),

    kaikkien_osasuoritusten_yhteislaajuus as (
      select paatason_suoritus_id, sum(koulutusmoduuli_laajuus_arvo) as laajuus from osasuoritukset group by paatason_suoritus_id
    ),

    opintojenlaajuus_koodisto as (
      select * from r_koodisto_koodi where koodisto_uri = 'opintojenlaajuusyksikko'
    ),

    kaikkien_osasuoritusten_laajuuden_yksiköt as (
      select paatason_suoritus_id, string_agg(distinct opintojenlaajuus_koodisto.nimi, ',') as yksiköt
      from osasuoritukset
      join opintojenlaajuus_koodisto on opintojenlaajuus_koodisto.koodiarvo = osasuoritukset.koulutusmoduuli_laajuus_yksikko
      where koulutusmoduuli_laajuus_yksikko is not null
      group by paatason_suoritus_id
    ),

    suoritettujen_yhteisten_tutkinnon_osien_osa_alueiden_lukumäärä as (
      select paatason_suoritus_id, count(*) as lkm from osasuoritukset where toteuttavan_luokan_nimi = 'yhteisentutkinnonosanosaalueensuoritus' and arviointi_hyvaksytty group by paatason_suoritus_id
    ),

    suoritettujen_tutkinnon_osaa_pienempien_kokonaisuuksien_lukumäärä as (
      select paatason_suoritus_id, count(*) as lkm from osasuoritukset where toteuttavan_luokan_nimi = 'tutkinnonosaapienemmänkokonaisuudensuoritus' and arviointi_hyvaksytty group by paatason_suoritus_id
    ),

    suoritettujen_muun_ammatillisen_koulutuksen_osasuoritusten_lukumäärä as (
      select paatason_suoritus_id, count(*) as lkm from osasuoritukset where toteuttavan_luokan_nimi = 'muunammatillisenkoulutuksenosasuorituksensuoritus' and arviointi_hyvaksytty group by paatason_suoritus_id
    )

    select
      oo_ja_pts.opiskeluoikeus_oid,
      oo_ja_pts.sisaltyy_opiskeluoikeuteen_oid,
      oo_ja_pts.lahdejarjestelma_koodiarvo,
      oo_ja_pts.lahdejarjestelma_id,
      oo_ja_pts.aikaleima,
      oo_ja_pts.toimipiste_oid,
      oo_ja_pts.koulutusmoduuli_nimi,
      oo_ja_pts.opiskeluoikeuden_alkamispaiva,
      oo_ja_pts.opiskeluoikeuden_viimeisin_tila,
      henkilo.yksiloity,
      henkilo.oppija_oid,
      henkilo.hetu,
      henkilo.etunimet,
      henkilo.sukunimi,
      coalesce(suoritettujen_osasuoritusten_lukumäärä.lkm, 0),
      coalesce(keskeneräisten_osasuoritusten_lukumäärä.lkm, 0),
      coalesce(kaikkien_osasuoritusten_yhteislaajuus.laajuus, 0.0),
      kaikkien_osasuoritusten_laajuuden_yksiköt.yksiköt,
      coalesce(suoritettujen_yhteisten_tutkinnon_osien_osa_alueiden_lukumäärä.lkm, 0),
      coalesce(suoritettujen_tutkinnon_osaa_pienempien_kokonaisuuksien_lukumäärä.lkm, 0),
      coalesce(suoritettujen_muun_ammatillisen_koulutuksen_osasuoritusten_lukumäärä.lkm, 0)
    from opiskeluoikeudet_ja_paatason_suoritukset oo_ja_pts
    join r_henkilo henkilo on henkilo.oppija_oid = oo_ja_pts.oppija_oid
    left join suoritettujen_osasuoritusten_lukumäärä on oo_ja_pts.paatason_suoritus_id = suoritettujen_osasuoritusten_lukumäärä.paatason_suoritus_id
    left join keskeneräisten_osasuoritusten_lukumäärä on oo_ja_pts.paatason_suoritus_id = keskeneräisten_osasuoritusten_lukumäärä.paatason_suoritus_id
    left join kaikkien_osasuoritusten_yhteislaajuus on oo_ja_pts.paatason_suoritus_id = kaikkien_osasuoritusten_yhteislaajuus.paatason_suoritus_id
    left join kaikkien_osasuoritusten_laajuuden_yksiköt on oo_ja_pts.paatason_suoritus_id = kaikkien_osasuoritusten_laajuuden_yksiköt.paatason_suoritus_id
    left join suoritettujen_yhteisten_tutkinnon_osien_osa_alueiden_lukumäärä on oo_ja_pts.paatason_suoritus_id = suoritettujen_yhteisten_tutkinnon_osien_osa_alueiden_lukumäärä.paatason_suoritus_id
    left join suoritettujen_tutkinnon_osaa_pienempien_kokonaisuuksien_lukumäärä on oo_ja_pts.paatason_suoritus_id = suoritettujen_tutkinnon_osaa_pienempien_kokonaisuuksien_lukumäärä.paatason_suoritus_id
    left join suoritettujen_muun_ammatillisen_koulutuksen_osasuoritusten_lukumäärä on oo_ja_pts.paatason_suoritus_id = suoritettujen_muun_ammatillisen_koulutuksen_osasuoritusten_lukumäärä.paatason_suoritus_id
  """

  private def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid"  -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "sisältyyOpiskeluoikeuteenOid" -> Column(t.get("raportti-excel-kolumni-sisältyyOpiskeluoikeuteenOid")),
    "lähdejärjestelmäKoodiarvo" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    "lähdejärjestelmäId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    "aikaleima" -> Column(t.get("raportti-excel-kolumni-päivitetty")),
    "toimipisteOid" -> Column(t.get("raportti-excel-kolumni-toimipisteOid")),
    "suorituksenNimi" -> Column(t.get("raportti-excel-kolumni-päätasonSuorituksenNimi")),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "opiskeluoikeudenViimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
    "yksilöity" -> Column(t.get("raportti-excel-kolumni-yksiloity")),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "suoritutettujenOsasuoritustenLkm" -> Column(t.get("raportti-excel-kolumni-suoritutettujenOsasuoritustenLkm")),
    "keskeneräistenOsasuoritustenLkm" -> Column(t.get("raportti-excel-kolumni-keskeneräistenOsasuoritustenLkm")),
    "kaikkienOsasuoritustenYhteislaajuus" -> Column(t.get("raportti-excel-kolumni-kaikkienOsasuoritustenYhteislaajuus")),
    "kaikkienOsasuoritustenLaajuudenYksiköt" -> Column(t.get("raportti-excel-kolumni-kaikkienOsasuoritustenLaajuudenYksiköt")),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenLkm" -> Column(t.get("raportti-excel-kolumni-suoritettujenYhteistenTutkinnonOsienOsaalueidenLkm")),
    "suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm" -> Column(t.get("raportti-excel-kolumni-suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm")),
    "suoritettujenMuuAmmatillisenKoulutuksenOsasuoritustenLkm" -> Column(t.get("raportti-excel-kolumni-suoritettujenMuuAmmatillisenKoulutuksenOsasuoritustenLkm"))
  )
}

case class MuuAmmatillinenRaporttiRow(
  opiskeluoikeusOid: String,
  sisältyyOpiskeluoikeuteenOid: Option[String],
  lähdejärjestelmäKoodiarvo: Option[String],
  lähdejärjestelmäId: Option[String],
  aikaleima: LocalDate,
  toimipisteOid: Option[String],
  suorituksenNimi: String,
  opiskeluoikeudenAlkamispäivä: LocalDate,
  opiskeluoikeudenViimeisinTila: String,
  yksilöity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
  suoritutettujenOsasuoritustenLkm: Int,
  keskeneräistenOsasuoritustenLkm: Int,
  kaikkienOsasuoritustenYhteislaajuus: Double,
  kaikkienOsasuoritustenLaajuudenYksiköt: Option[String],
  suoritettujenYhteistenTutkinnonOsienOsaalueidenLkm: Int,
  suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm: Int,
  suoritettujenMuuAmmatillisenKoulutuksenOsasuoritustenLkm: Int
)
