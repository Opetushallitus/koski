package fi.oph.koski.valpas.opiskeluoikeusrepository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DatabaseConverters, SQLHelpers}
import fi.oph.koski.log.Logging
import org.json4s.JValue
import slick.jdbc.GetResult
import java.time.LocalDate

import fi.oph.koski.util.Timing

case class ValpasOppijaRow(
  oppijaOid: String,
  kaikkiOppijaOidit: Seq[ValpasHenkilö.Oid],
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  oikeutetutOppilaitokset: Set[ValpasOppilaitos.Oid],
  opiskeluoikeudet: JValue,
  turvakielto: Boolean,
  äidinkieli: Option[String],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate
)

class ValpasOpiskeluoikeusDatabaseService(application: KoskiApplication) extends DatabaseConverters with Logging with Timing {
  private val db = application.raportointiDatabase
  private val rajapäivätService = application.valpasRajapäivätService

  def getPeruskoulunValvojalleNäkyväOppija(oppijaOid: String): Option[ValpasOppijaRow] =
    getOppijat(Some(oppijaOid), None).filterNot(_.oikeutetutOppilaitokset.isEmpty).headOption

  def getPeruskoulunValvojalleNäkyvätOppijat(oppilaitosOid: String): Seq[ValpasOppijaRow] =
    getOppijat(None, Some(Seq(oppilaitosOid))).filterNot(_.oikeutetutOppilaitokset.isEmpty)

  private implicit def getResult: GetResult[ValpasOppijaRow] = GetResult(r => {
    ValpasOppijaRow(
      oppijaOid = r.rs.getString("oppija_oid"),
      kaikkiOppijaOidit = r.getArray("kaikkiOppijaOidit").toSeq,
      hetu = Option(r.rs.getString("hetu")),
      syntymäaika = Option(r.getLocalDate("syntymaaika")),
      etunimet = r.rs.getString("etunimet"),
      sukunimi = r.rs.getString("sukunimi"),
      oikeutetutOppilaitokset = r.getArray("oikeutetutOppilaitokset").toSet,
      opiskeluoikeudet = r.getJson("opiskeluoikeudet"),
      turvakielto = r.rs.getBoolean("turvakielto"),
      äidinkieli = Option(r.rs.getString("aidinkieli")),
      oppivelvollisuusVoimassaAsti = r.getLocalDate("oppivelvollisuusVoimassaAsti"),
      oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = r.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti")
    )
  })

  // Huom: Luotetaan siihen, että käyttäjällä on oikeudet nimenomaan annettuihin oppilaitoksiin!
  // Huom2: Tämä toimii vain peruskoulun hakeutumisen valvojille (ei esim. 10-luokille tai toisen asteen näkymiin yms.)
  // Huom3: Tämä ei filteröi opiskeluoikeuksia sen mukaan, minkä tiedot kuuluisi näyttää listanäkymässä, jos samalla oppijalla on useita opiskeluoikeuksia.
  //        Valinta voidaan jättää joko Scalalle, käyttöliitymälle tai tehdä toinen query, joka tekee valinnan SQL:ssä.
  private def getOppijat(
    oppijaOid: Option[String],
    oppilaitosOids: Option[Seq[String]]
  ): Seq[ValpasOppijaRow] = {
    val keväänValmistumisjaksoAlku = rajapäivätService.keväänValmistumisjaksoAlku
    val keväänValmistumisjaksoLoppu = rajapäivätService.keväänValmistumisjaksoLoppu
    val keväänUlkopuolellaValmistumisjaksoAlku = rajapäivätService.keväänUlkopuolellaValmistumisjaksoAlku()
    val tarkastelupäivä = rajapäivätService.tarkastelupäivä
    val keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä = rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
    val perusopetussuorituksenNäyttämisenAikaraja = rajapäivätService.perusopetussuorituksenNäyttämisenAikaraja

    val timedBlockname = if (oppijaOid.isDefined) "getOppijatSingle" else "getOppijatMultiple"

    timed(timedBlockname, 10) {
      db.runDbSync(SQLHelpers.concatMany(
        Some(
          sql"""
WITH
  """),
        oppijaOid.map(oid =>
          sql"""
  -- CTE: jos pyydettiin vain yhtä oppijaa, hae hänen master oid:nsa
  pyydetty_oppija AS (
    SELECT r_henkilo.master_oid
    FROM r_henkilo
    WHERE r_henkilo.oppija_oid = $oid
  ),
      """),
        Some(
          sql"""
  -- CTE: Kaikki opiskeluoikeudet, jotka ovat oppivelvollisuuden suorittamiseen kelpaavia
  ov_kelvollinen_opiskeluoikeus AS (
    SELECT
      DISTINCT r_opiskeluoikeus.opiskeluoikeus_oid,
      r_opiskeluoikeus.oppilaitos_oid,
      r_opiskeluoikeus.alkamispaiva,
      r_opiskeluoikeus.paattymispaiva,
      r_opiskeluoikeus.viimeisin_tila,
      r_opiskeluoikeus.data,
      r_opiskeluoikeus.koulutusmuoto,
      r_henkilo.master_oid,
      (oppivelvollisuustiedot.oppivelvollisuusvoimassaasti >= $tarkastelupäivä) AS henkilo_on_oppivelvollinen
    FROM
      r_henkilo
      -- oppivelvollisuustiedot-näkymä hoitaa syntymäaika- ja mahdollisen peruskoulusta ennen lain voimaantuloa valmistumisen
      -- tarkistuksen: siinä ei ole tietoja kuin oppijoista, jotka ovat oppivelvollisuuden laajentamislain piirissä
      JOIN oppivelvollisuustiedot ON oppivelvollisuustiedot.oppija_oid = r_henkilo.oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = r_henkilo.oppija_oid
      """),
        oppilaitosOids.map(oids => sql"AND r_opiskeluoikeus.oppilaitos_oid = any($oids)"),
        oppijaOid.map(oid => sql"JOIN pyydetty_oppija ON pyydetty_oppija.master_oid = r_henkilo.master_oid"),
        Some(
          sql"""
    WHERE
      r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava IS TRUE
  )
  -- CTE: opiskeluoikeudet, joiden hakeutumista oppilaitoksella on oikeus valvoa tällä hetkellä
  -- TODO: Tämä toimii vain peruskoulun hakeutumisen valvojille tällä hetkellä. Pitää laajentaa kattamaan myös nivelvaihe.
  , hakeutumisvalvottava_opiskeluoikeus AS (
    SELECT
      DISTINCT ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid,
      ov_kelvollinen_opiskeluoikeus.oppilaitos_oid,
      ov_kelvollinen_opiskeluoikeus.master_oid
    FROM
      ov_kelvollinen_opiskeluoikeus
      JOIN r_paatason_suoritus ON r_paatason_suoritus.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella ON aikajakson_keskella.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      -- Lasketaan voimassaolevien kotiopetusjaksojen määrä ehtoa varten
      CROSS JOIN LATERAL (
        SELECT
          count(*) AS count
        FROM
          jsonb_array_elements(ov_kelvollinen_opiskeluoikeus.data -> 'lisätiedot' -> 'kotiopetusjaksot') jaksot
        WHERE
          jaksot ->> 'loppu' IS NULL
            OR $tarkastelupäivä BETWEEN jaksot ->> 'alku' AND jaksot ->> 'loppu'
      ) kotiopetusjaksoja
    WHERE
      -- (0) Henkilö on oppivelvollinen: hakeutumisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön oppivelvollisuus on päättynyt
      ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen IS TRUE
      -- (1) oppijalla on peruskoulun opiskeluoikeus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'perusopetus'
      AND r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
      -- (2) kyseisessä opiskeluoikeudessa on yhdeksännen luokan suoritus.
      AND r_paatason_suoritus.koulutusmoduuli_koodiarvo = '9'
      -- (3a) valvojalla on oppilaitostason oppilaitosoikeus ja opiskeluoikeuden lisätiedoista ei löydy kotiopetusjaksoa, joka osuu tälle hetkelle
      --      TODO (3b): puuttuu, koska ei vielä ole selvää, miten kotiopetusoppilaat halutaan käsitellä
      AND kotiopetusjaksoja.count = 0
      -- (4)  opiskeluoikeus ei ole eronnut tilassa tällä hetkellä
      AND (
        (aikajakson_keskella.tila IS NOT NULL AND NOT aikajakson_keskella.tila = any('{eronnut, katsotaaneronneeksi, peruutettu}'))
        OR (aikajakson_keskella.tila IS NULL AND $tarkastelupäivä < ov_kelvollinen_opiskeluoikeus.alkamispaiva)
        OR (aikajakson_keskella.tila IS NULL AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva AND NOT ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, peruutettu}'))
      )
      AND (
        -- (5a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tai lomalla tällä hetkellä. Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          (aikajakson_keskella.tila IS NOT NULL AND aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}'))
        )
        -- TAI:
        OR (
          -- (5b.1 ) opiskeluoikeus on valmistunut-tilassa, ja siitä löytyy vahvistettu päättötodistus
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva AND ov_kelvollinen_opiskeluoikeus.viimeisin_tila = 'valmistunut')
          -- (5b.2 ) ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön valmistumisajasta.
          AND (
            (
              -- keväällä valmistunut ja tarkastellaan heille määrättyä rajapäivää aiemmin
              (ov_kelvollinen_opiskeluoikeus.paattymispaiva BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
              AND $tarkastelupäivä <= $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
            )
            OR (
              -- tai muuna aikana valmistunut ja tarkastellaan heille määrättyä rajapäivää aiemmin
              (ov_kelvollinen_opiskeluoikeus.paattymispaiva NOT BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
              AND (ov_kelvollinen_opiskeluoikeus.paattymispaiva >= $keväänUlkopuolellaValmistumisjaksoAlku)
            )
          )
        )
      )
  )
  -- CTE: kaikki uuden lain piirissä olevat oppijat, joilla on vähintään yksi hakeutumisvalvottava opiskeluoikeus,
  -- mukana myös taulukko kelpuutettavien opiskeluoikeuksien oppilaitoksista käyttöoikeustarkastelua varten.
  , oppija AS (
    SELECT
      DISTINCT r_henkilo.master_oid,
      r_henkilo.hetu,
      r_henkilo.syntymaaika,
      r_henkilo.etunimet,
      r_henkilo.sukunimi,
      array_remove(array_agg(DISTINCT hakeutumisvalvottava_opiskeluoikeus.oppilaitos_oid), NULL) AS oikeutettu_oppilaitos_oids,
      array_remove(array_agg(DISTINCT hakeutumisvalvottava_opiskeluoikeus.opiskeluoikeus_oid), NULL) AS hakeutumisvalvottava_opiskeluoikeus_oids,
      r_henkilo.turvakielto,
      r_henkilo.aidinkieli,
      array_agg(DISTINCT kaikki_henkilot.oppija_oid) AS kaikkiOppijaOidit,
      oppivelvollisuustiedot.oppivelvollisuusvoimassaasti AS oppivelvollisuus_voimassa_asti,
      oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti AS oikeus_koulutuksen_maksuttomuuteen_voimassa_asti
    FROM
      r_henkilo
      -- oppivelvollisuustiedot-näkymä hoitaa syntymäaika- ja mahdollisen peruskoulusta ennen lain voimaantuloa valmistumisen
      -- tarkistuksen: siinä ei ole tietoja kuin oppijoista, jotka ovat oppivelvollisuuden laajentamislain piirissä
      JOIN oppivelvollisuustiedot ON oppivelvollisuustiedot.oppija_oid = r_henkilo.oppija_oid
      LEFT JOIN hakeutumisvalvottava_opiskeluoikeus ON hakeutumisvalvottava_opiskeluoikeus.master_oid = r_henkilo.master_oid
      -- Haetaan kaikki oppijan oidit: pitää palauttaa esim. kuntailmoitusten kyselyä varten
      JOIN r_henkilo kaikki_henkilot ON kaikki_henkilot.master_oid = r_henkilo.master_oid
    GROUP BY
      r_henkilo.master_oid,
      r_henkilo.hetu,
      r_henkilo.syntymaaika,
      r_henkilo.etunimet,
      r_henkilo.sukunimi,
      r_henkilo.turvakielto,
      r_henkilo.aidinkieli,
      oppivelvollisuustiedot.oppivelvollisuusvoimassaasti,
      oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti
  )
  -- CTE: kaikki oppija_oidit, joilla pitää opiskeluoikeuksia etsiä
  , oppija_oid AS (
	SELECT
	  DISTINCT oppija_oid,
	  oppija.master_oid
	FROM
	  r_henkilo
	  JOIN oppija ON oppija.master_oid = r_henkilo.master_oid
  )
  -- Tilamäppäykset Kosken tarkkojen ja Valppaan yksinkertaisempien tilojen välillä
  , valpastila AS (
    SELECT
      column1 AS koskiopiskeluoikeudentila,
      column2 AS valpasopiskeluoikeudentila
    FROM
      (VALUES
        ('lasna', 'voimassa'),
        ('valiaikaisestikeskeytynyt', 'voimassa'),
        ('loma', 'voimassa'),
        ('valmistunut', 'valmistunut'),
        ('eronnut', 'eronnut'),
        ('katsotaaneronneeksi', 'katsotaaneronneeksi'),
        ('peruutettu', 'peruutettu'),
        ('mitatoity', 'mitatoity'),
        (NULL, 'tuntematon')
      ) t
  )
  -- CTE: peruskoulun opiskeluoikeudet (ei sama lista kuin ekassa CTE:ssä, koska voi olla rinnakkaisia tai peräkkäisiä muita peruskoulun opiskeluoikeuksia.
  -- Teoriassa varmaan voisi tehostaa kyselyä jotenkin ja välttää näiden hakeminen uudestaan, mutta kysely voisi mennä melko monimutkaiseksi.)
  , peruskoulun_opiskeluoikeus AS (
     SELECT
       oppija_oid.master_oid,
       r_opiskeluoikeus.opiskeluoikeus_oid,
       r_opiskeluoikeus.koulutusmuoto,
       r_opiskeluoikeus.oppilaitos_oid,
       r_opiskeluoikeus.oppilaitos_nimi,
       valittu_r_paatason_suoritus.toimipiste_oid,
       valittu_r_paatason_suoritus.toimipiste_nimi,
       r_opiskeluoikeus.alkamispaiva,
       r_opiskeluoikeus.paattymispaiva,
       r_opiskeluoikeus.paattymispaiva > $tarkastelupäivä AS paattymispaiva_merkitty_tulevaisuuteen,
       coalesce(valittu_r_paatason_suoritus.data ->> 'luokka', r_opiskeluoikeus.luokka) AS ryhmä,
       r_opiskeluoikeus.viimeisin_tila,
       CASE
         WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
         WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
         ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
       END tarkastelupäivän_tila,
       r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava,
       (r_opiskeluoikeus.viimeisin_tila = 'valmistunut' AND coalesce(r_opiskeluoikeus.paattymispaiva < $perusopetussuorituksenNäyttämisenAikaraja, FALSE)) AS naytettava_perusopetuksen_suoritus
     FROM
       oppija_oid
       JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
         AND r_opiskeluoikeus.koulutusmuoto = 'perusopetus'
       LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella ON aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
         AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
       LEFT JOIN valpastila valpastila_aikajakson_keskella ON valpastila_aikajakson_keskella.koskiopiskeluoikeudentila = aikajakson_keskella.tila
       LEFT JOIN valpastila valpastila_viimeisin ON valpastila_viimeisin.koskiopiskeluoikeudentila = r_opiskeluoikeus.viimeisin_tila
       -- Haetaan päätason suoritus, jonka dataa halutaan näyttää (toistaiseksi valitaan alkamispäivän perusteella uusin)
       -- TODO: Ei välttämättä osu oikeaan, koska voi olla esim. monen eri tyyppisiä peruskoulun päätason suorituksia, ja pitäisi oikeasti filteröidä myös tyypin perusteella.
       -- TODO: Pitää toteuttaa tutkittavan ajanhetken tarkistus tähänkin, että näytetään luokkatieto sen mukaan, millä luokalla on ollut tutkittavalla ajanhetkellä.
       CROSS JOIN LATERAL (
         SELECT
           *
         FROM
           r_paatason_suoritus inner_r_paatason_suoritus
         WHERE
           inner_r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
           AND inner_r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
         ORDER BY
           -- hae vain uusin vuosiluokan suoritus (toistaiseksi, myöhemmin pitää pystyä valitsemaan myös esim. edelliseltä keväältä parametrina annetun päivämäärän perusteella)
           inner_r_paatason_suoritus.DATA ->> 'alkamispäivä' DESC
        LIMIT
          1
        ) valittu_r_paatason_suoritus
  )
  -- CTE: Muut kuin peruskoulun opiskeluoikeudet
  -- Pitää hakea erikseen, koska säännöt päätason suorituksen kenttien osalta ovat erilaiset, koska datat ovat erilaiset
  , muu_opiskeluoikeus AS (
     SELECT
       oppija_oid.master_oid,
       r_opiskeluoikeus.opiskeluoikeus_oid,
       r_opiskeluoikeus.koulutusmuoto,
       r_opiskeluoikeus.oppilaitos_oid,
       r_opiskeluoikeus.oppilaitos_nimi,
       valittu_r_paatason_suoritus.toimipiste_oid,
       valittu_r_paatason_suoritus.toimipiste_nimi,
       r_opiskeluoikeus.alkamispaiva,
       r_opiskeluoikeus.paattymispaiva,
       r_opiskeluoikeus.paattymispaiva > $tarkastelupäivä AS paattymispaiva_merkitty_tulevaisuuteen,
       coalesce(valittu_r_paatason_suoritus.data ->> 'ryhmä', r_opiskeluoikeus.luokka) AS ryhmä,
       r_opiskeluoikeus.viimeisin_tila,
       CASE
         WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
         WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
         ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
       END tarkastelupäivän_tila,
       r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava,
       FALSE AS naytettava_perusopetuksen_suoritus
     FROM
       oppija_oid
       JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
         AND r_opiskeluoikeus.koulutusmuoto <> 'perusopetus'
       LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella ON aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
         AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
       LEFT JOIN valpastila valpastila_aikajakson_keskella ON valpastila_aikajakson_keskella.koskiopiskeluoikeudentila = aikajakson_keskella.tila
       LEFT JOIN valpastila valpastila_viimeisin ON valpastila_viimeisin.koskiopiskeluoikeudentila = r_opiskeluoikeus.viimeisin_tila
       -- Haetaan päätason suoritus, jonka dataa halutaan näyttää (TODO: toistaiseksi tulos on random, jos ei ole vahvistusta tai arviointia, mutta data ei riitä muuten.
       -- Teoriassa voisi tutkia päätason suorituksen osasuorituksiin kirjattuja päivämääriä, mutta se on aika monimutkaista ja luultavasti myös hidasta.
       CROSS JOIN LATERAL (
         SELECT
           *
         FROM
           r_paatason_suoritus inner_r_paatason_suoritus
         WHERE
           inner_r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
         ORDER BY
           inner_r_paatason_suoritus.vahvistus_paiva DESC NULLS FIRST,
           inner_r_paatason_suoritus.arviointi_paiva DESC NULLS FIRST
        LIMIT
          1
        ) valittu_r_paatason_suoritus
  )
  -- CTE: Yhdistettynä peruskoulun ja muut opiskeluoikeudet
  , opiskeluoikeus AS (
    SELECT
      *
    FROM
     peruskoulun_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
     muu_opiskeluoikeus
  )
  -- Päätason SELECT: Muodostetaan palautettava rakenne
  SELECT
    oppija.master_oid AS oppija_oid,
    oppija.kaikkiOppijaOidit,
    oppija.hetu,
    oppija.syntymaaika,
    oppija.etunimet,
    oppija.sukunimi,
    oppija.oikeutettu_oppilaitos_oids AS oikeutetutOppilaitokset,
    oppija.turvakielto AS turvakielto,
    oppija.aidinkieli AS aidinkieli,
    oppija.oppivelvollisuus_voimassa_asti AS oppivelvollisuusVoimassaAsti,
    oppija.oikeus_koulutuksen_maksuttomuuteen_voimassa_asti AS oikeusKoulutuksenMaksuttomuuteenVoimassaAsti,
    json_agg(
      json_build_object(
        'oid', opiskeluoikeus.opiskeluoikeus_oid,
        'onHakeutumisValvottava', opiskeluoikeus.opiskeluoikeus_oid = ANY(oppija.hakeutumisvalvottava_opiskeluoikeus_oids),
        'tyyppi', json_build_object(
          'koodiarvo', opiskeluoikeus.koulutusmuoto,
          'koodistoUri', 'opiskeluoikeudentyyppi'
        ),
        'oppilaitos', json_build_object(
          'oid', opiskeluoikeus.oppilaitos_oid,
          'nimi', json_build_object(
            'fi', opiskeluoikeus.oppilaitos_nimi
          )
        ),
        'toimipiste', json_build_object(
          'oid', opiskeluoikeus.toimipiste_oid,
          'nimi', json_build_object(
            'fi', opiskeluoikeus.toimipiste_nimi
          )
        ),
        'alkamispäivä', opiskeluoikeus.alkamispaiva,
        'päättymispäivä', opiskeluoikeus.paattymispaiva,
        'päättymispäiväMerkittyTulevaisuuteen', opiskeluoikeus.paattymispaiva_merkitty_tulevaisuuteen,
        'ryhmä', opiskeluoikeus.ryhmä,
        'tarkastelupäivänTila', json_build_object(
          'koodiarvo', opiskeluoikeus.tarkastelupäivän_tila,
          'koodistoUri', 'valpasopiskeluoikeudentila'
        ),
        'näytettäväPerusopetuksenSuoritus', opiskeluoikeus.naytettava_perusopetuksen_suoritus
      ) ORDER BY
        opiskeluoikeus.alkamispaiva DESC,
        -- Alkamispäivä varmaan riittäisi käyttöliitymälle, mutta lisätään muita kenttiä testien pitämiseksi deteministisempinä myös päällekäisillä opiskeluoikeuksilla:
        opiskeluoikeus.paattymispaiva DESC,
        opiskeluoikeus.koulutusmuoto,
        opiskeluoikeus.ryhmä DESC NULLS LAST,
        opiskeluoikeus.tarkastelupäivän_tila
    ) opiskeluoikeudet
  FROM
    opiskeluoikeus
    JOIN oppija ON oppija.master_oid = opiskeluoikeus.master_oid
  WHERE
    opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava is true
  GROUP BY
    oppija.master_oid,
    oppija.kaikkiOppijaOidit,
    oppija.hetu,
    oppija.syntymaaika,
    oppija.etunimet,
    oppija.sukunimi,
    oppija.oikeutettu_oppilaitos_oids,
    oppija.turvakielto,
    oppija.aidinkieli,
    oppija.oppivelvollisuus_voimassa_asti,
    oppija.oikeus_koulutuksen_maksuttomuuteen_voimassa_asti
  ORDER BY
    oppija.sukunimi,
    oppija.etunimet
    """)).as[ValpasOppijaRow])
    }
  }
}
