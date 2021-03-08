package fi.oph.koski.valpas.repository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.SQLHelpers
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema.{RHenkilöTable, ROpiskeluoikeusTable}
import slick.jdbc.GetResult

import java.sql.ResultSet

class ValpasDatabaseService(application: KoskiApplication) extends Logging {
  val db = application.raportointiDatabase
  lazy val RHenkilöt = TableQuery[RHenkilöTable]
  lazy val ROpiskeluoikeudet = TableQuery[ROpiskeluoikeusTable]

  def getPeruskoulunValvojalleNäkyväOppija(oppijaOid: String): Option[ValpasOppijaResult] =
    getOppijat(Some(oppijaOid), None).headOption

  def getPeruskoulunValvojalleNäkyvätOppijat(oppilaitosOids: Option[Seq[String]]): Seq[ValpasOppijaResult] =
    getOppijat(None, oppilaitosOids)

  // Huom: Luotetaan siihen, että käyttäjällä on oikeudet nimenomaan annettuihin oppilaitoksiin!
  // Huom2: Tämä toimii vain peruskoulun hakeutumisen valvojille (ei esim. 10-luokille tai toisen asteen näkymiin yms.)
  // Huom3: Tämä ei filteröi opiskeluoikeuksia sen mukaan, minkä tiedot kuuluisi näyttää listanäkymässä, jos samalla oppijalla on useita opiskeluoikeuksia.
  //        Valinta voidaan jättää joko Scalalle, käyttöliitymälle tai tehdä toinen query, joka tekee valinnan SQL:ssä.
  private def getOppijat(oppijaOid: Option[String], oppilaitosOids: Option[Seq[String]]): Seq[ValpasOppijaResult] = {
    db.runDbSync(SQLHelpers.concatMany(
      Some(sql"""
WITH
  """),
      oppijaOid.map(oid => sql"""
  -- CTE: jos pyydettiin vain yhtä oppijaa, hae hänen master oid:nsa
  pyydetty_oppija AS (
    SELECT r_henkilo.master_oid
    FROM r_henkilo
    WHERE r_henkilo.oppija_oid = $oid
  ),
      """),
      Some(sql"""
  -- CTE: kaikki oppijat, joilla on vähintään yksi kelpuutettava peruskoulun opetusoikeus, mukana
  -- myös taulukko kelpuutettavien opiskeluoikeuksien oppilaitoksista käyttöoikeustarkastelua varten.
  oppija AS (
    SELECT
      DISTINCT r_henkilo.master_oid,
      r_henkilo.hetu,
      r_henkilo.syntymaaika,
      r_henkilo.etunimet,
      r_henkilo.sukunimi,
      array_agg(DISTINCT r_opiskeluoikeus.oppilaitos_oid) AS oikeutettu_oppilaitos_oids
    FROM
      r_henkilo
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = r_henkilo.oppija_oid
      """),
      oppilaitosOids.map(oids => sql"AND r_opiskeluoikeus.oppilaitos_oid = any($oids)"),
      oppijaOid.map(oid => sql"JOIN pyydetty_oppija ON pyydetty_oppija.master_oid = r_henkilo.master_oid"),
      Some(sql"""
      JOIN r_paatason_suoritus ON r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
      -- Lasketaan voimassaolevien kotiopetusjaksojen määrä ehtoa 4a varten
      CROSS JOIN LATERAL (
        SELECT
          count(*) AS count
        FROM
          jsonb_array_elements(r_opiskeluoikeus.data -> 'lisätiedot' -> 'kotiopetusjaksot') jaksot
        WHERE
          jaksot ->> 'loppu' IS NULL
            OR to_char(NOW(), 'YYYY-MM-DD') BETWEEN jaksot ->> 'alku' AND jaksot ->> 'loppu'
      ) kotiopetusjaksoja
    WHERE
      -- (1) oppija on potentiaalisesti oppivelvollinen, eli syntynyt 2004 tai myöhemmin
      EXTRACT(YEAR FROM r_henkilo.syntymaaika) >= 2004
      -- (2) oppijalla on peruskoulun opiskeluoikeus
      AND r_opiskeluoikeus.koulutusmuoto = 'perusopetus'
      AND r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
      -- (3) kyseisessä opiskeluoikeudessa on yhdeksännen luokan suoritus.
      AND r_paatason_suoritus.koulutusmoduuli_koodiarvo = '9'
      -- (4a) valvojalla on oppilaitostason oppilaitosoikeus ja opiskeluoikeuden lisätiedoista ei löydy kotiopetusjaksoa, joka osuu tälle hetkelle (TODO: tutkittavalle ajanhetkelle)
      --      TODO (4b): puuttuu, koska ei vielä ole selvää, miten kotiopetusoppilaat halutaan käsitellä
      AND kotiopetusjaksoja.count = 0
      -- (5)  opiskeluoikeus ei ole eronnut tilassa tällä hetkellä (TODO: tutkittavalla ajanhetkellä)
      AND r_opiskeluoikeus.viimeisin_tila <> 'eronnut'
      AND (
        -- (6a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tällä hetkellä (TODO: tutkittavalla ajanhetkellä)
        r_opiskeluoikeus.viimeisin_tila = any('{lasna, valiaikaisestikeskeytynyt}')
        -- TAI:
        OR (
          -- (6b.1 ) opiskeluoikeus on valmistunut-tilassa, ja siitä löytyy vahvistettu päättötodistus
          r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
          -- TODO (6b.2 ) ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön valmistumisajasta. Säännöt on jotakuinkin selvillä, on vaan toteuttamatta.
        )
      )
    GROUP BY
      r_henkilo.master_oid,
      r_henkilo.hetu,
      r_henkilo.syntymaaika,
      r_henkilo.etunimet,
      r_henkilo.sukunimi
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
       coalesce(valittu_r_paatason_suoritus.data ->> 'luokka', r_opiskeluoikeus.luokka) AS ryhmä,
       r_opiskeluoikeus.viimeisin_tila
     FROM
       oppija_oid
       JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
         AND r_opiskeluoikeus.koulutusmuoto = 'perusopetus'
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
           -- hae vain uusin vuosiluokan suoritus (toistaiseksi, möyhemmin pitää pystyä valitsemaan myös esim. edelliseltä keväältä parametrina annetun päivämäärän perusteella)
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
       coalesce(valittu_r_paatason_suoritus.data ->> 'ryhmä', r_opiskeluoikeus.luokka) AS ryhmä,
       r_opiskeluoikeus.viimeisin_tila
     FROM
       oppija_oid
       JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
         AND r_opiskeluoikeus.koulutusmuoto <> 'perusopetus'
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
    oppija.hetu,
    oppija.syntymaaika,
    oppija.etunimet,
    oppija.sukunimi,
    oppija.oikeutettu_oppilaitos_oids AS oikeutetutOppilaitokset,
    json_agg(
      json_build_object(
        'oid', opiskeluoikeus.opiskeluoikeus_oid,
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
        'ryhmä', opiskeluoikeus.ryhmä,
        'viimeisinTila', json_build_object(
          'koodiarvo', opiskeluoikeus.viimeisin_tila,
          'koodistoUri', 'koskiopiskeluoikeudentila'
        )
      ) ORDER BY
        opiskeluoikeus.alkamispaiva DESC,
        -- Alkamispäivä varmaan riittäisi käyttöliitymälle, mutta lisätään muita kenttiä testien pitämiseksi deteministisempinä myös päällekäisillä opiskeluoikeuksilla:
        opiskeluoikeus.paattymispaiva DESC,
        opiskeluoikeus.koulutusmuoto,
        opiskeluoikeus.ryhmä DESC NULLS LAST,
        opiskeluoikeus.viimeisin_tila
    ) opiskeluoikeudet
  FROM
    opiskeluoikeus
    JOIN oppija ON oppija.master_oid = opiskeluoikeus.master_oid
  GROUP BY
    oppija.master_oid,
    oppija.hetu,
    oppija.syntymaaika,
    oppija.etunimet,
    oppija.sukunimi,
    oppija.oikeutettu_oppilaitos_oids
  ORDER BY
    oppija.sukunimi,
    oppija.etunimet
    """)).as[(ValpasOppijaResult)])
  }

  implicit private val getValpasOppijaResult: GetResult[ValpasOppijaResult] = GetResult(r => {
    val rs: ResultSet = r.rs
    ValpasOppijaResult(
      henkilö = ValpasHenkilö(
        oid = rs.getString("oppija_oid"),
        hetu = Option(rs.getString("hetu")),
        syntymäaika = Option(rs.getString("syntymaaika")),
        etunimet = rs.getString("etunimet"),
        sukunimi = rs.getString("sukunimi")
      ),
      oikeutetutOppilaitokset = rs.getArray("oikeutetutOppilaitokset").getArray.asInstanceOf[Array[String]].toSet,
      opiskeluoikeudet = JsonSerializer.parse[List[ValpasOpiskeluoikeus]](rs.getString("opiskeluoikeudet"))
    )
  })
}
