package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import slick.jdbc.GetResult

case class MuuAmmatillinenRaporttiBuilder(db: DB) extends KoskiDatabaseMethods {

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

  def build(oppilaitosOid: String, alku: Date, loppu: Date): DataSheet = {
    val rows = runDbSync(queryMuuAmmatillisenSuoritukset(oppilaitosOid, alku, loppu).as[MuuAmmatillinenRaporttiRow], timeout = 5.minutes)
    DataSheet(
      title = "Muu_ammatillisen_koulutuksen_raportti",
      rows,
      columnSettings
    )
  }

  private def queryMuuAmmatillisenSuoritukset(oppilaitosOid: String, alku: Date, loppu: Date) = sql"""
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
        pts.koulutusmoduuli_nimi,
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
        pts.koulutusmoduuli_nimi,
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

  private lazy val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid"  -> Column("Opiskeluoikeuden oid"),
    "sisältyyOpiskeluoikeuteenOid" -> Column("Sisältyvän opiskeluoikeuden oid"),
    "lähdejärjestelmäKoodiarvo" -> Column("Lähdejärjestelmä"),
    "lähdejärjestelmäId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    "aikaleima" -> Column("Opiskeluoikeus päivitettv"),
    "toimipisteOid" -> Column("Toimipisteen oid"),
    "suorituksenNimi" -> Column("Päätason suoritus"),
    "opiskeluoikeudenAlkamispäivä" -> Column("Opiskeluoikeuden alkamispäivä"),
    "opiskeluoikeudenViimeisinTila" -> Column("Opiskeluoikeuden viimeisin tila"),
    "yksilöity" -> Column("Yksilöity"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Henkilötunnus"),
    "etunimet" -> Column("Etunimet"),
    "sukunimi" -> Column("Sukunimi"),
    "suoritutettujenOsasuoritustenLkm" -> Column("Suoritettujen osasuoritusten lukumäärä"),
    "keskeneräistenOsasuoritustenLkm" -> Column("Keskeneräisten osasuoritusten lukumäärä"),
    "kaikkienOsasuoritustenYhteislaajuus" -> Column("Kaikkien osasuoritusten yhteislaajuus"),
    "kaikkienOsasuoritustenLaajuudenYksiköt" -> Column("Kaikkien osasuoritusten laajuuden yksiköt"),
    "suoritettujenYhteistenTutkinnonOsienOsaalueidenLkm" -> Column("Suoritettujen yhteisten tutkinnon osien osa-alueiden lukumäärä"),
    "suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm" -> Column("Suoritettujen tutkinnon osaa pienempien kokonaisuuksien lukumäärä"),
    "suoritettujenMuuAmmatillisenKoulutuksenOsasuoritustenLkm" -> Column("Suoritettujen Muu ammatillisen koulutuksen osasuoritusten lukumäärä")
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
