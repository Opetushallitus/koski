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
  hakeutumisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid],
  suorittamisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid],
  opiskeluoikeudet: JValue,
  turvakielto: Boolean,
  äidinkieli: Option[String],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate,
  onOikeusValvoaMaksuttomuutta: Boolean,
  onOikeusValvoaKunnalla: Boolean
)

object HakeutumisvalvontaTieto extends Enumeration {
  type HakeutumisvalvontaTieto = Value
  val Perusopetus, Nivelvaihe, Kaikki = Value
}

class ValpasOpiskeluoikeusDatabaseService(application: KoskiApplication) extends DatabaseConverters with Logging with Timing {
  private val db = application.raportointiDatabase
  private val rajapäivätService = application.valpasRajapäivätService

  def getOppija(oppijaOid: String, rajaaOVKelpoisiinOpiskeluoikeuksiin: Boolean = true): Option[ValpasOppijaRow] =
    getOppijat(List(oppijaOid), None, rajaaOVKelpoisiinOpiskeluoikeuksiin, HakeutumisvalvontaTieto.Kaikki).headOption

  def getOppijat(oppijaOids: Seq[String]): Seq[ValpasOppijaRow] =
    if (oppijaOids.nonEmpty) getOppijat(oppijaOids, None, true, HakeutumisvalvontaTieto.Kaikki) else Seq.empty

  def getOppijatByOppilaitos(oppilaitosOid: String, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value): Seq[ValpasOppijaRow] =
    getOppijat(Seq.empty, Some(Seq(oppilaitosOid)), true, hakeutumisvalvontaTieto)

  private implicit def getResult: GetResult[ValpasOppijaRow] = GetResult(r => {
    ValpasOppijaRow(
      oppijaOid = r.rs.getString("oppija_oid"),
      kaikkiOppijaOidit = r.getArray("kaikkiOppijaOidit").toSeq,
      hetu = Option(r.rs.getString("hetu")),
      syntymäaika = Option(r.getLocalDate("syntymaaika")),
      etunimet = r.rs.getString("etunimet"),
      sukunimi = r.rs.getString("sukunimi"),
      hakeutumisvalvovatOppilaitokset = r.getArray("hakeutumisvalvovatOppilaitokset").toSet,
      suorittamisvalvovatOppilaitokset = r.getArray("suorittamisvalvovatOppilaitokset").toSet,
      opiskeluoikeudet = r.getJson("opiskeluoikeudet"),
      turvakielto = r.rs.getBoolean("turvakielto"),
      äidinkieli = Option(r.rs.getString("aidinkieli")),
      oppivelvollisuusVoimassaAsti = r.getLocalDate("oppivelvollisuusVoimassaAsti"),
      oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = r.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti"),
      onOikeusValvoaMaksuttomuutta = r.rs.getBoolean("onOikeusValvoaMaksuttomuutta"),
      onOikeusValvoaKunnalla = r.rs.getBoolean("onOikeusValvoaKunnalla")
    )
  })

  // Huom: Luotetaan siihen, että käyttäjällä on oikeudet nimenomaan annettuihin oppilaitoksiin!
  // Huom2: Tämä toimii vain peruskoulun hakeutumisen valvojille (ei esim. 10-luokille tai toisen asteen näkymiin yms.)
  // Huom3: Tämä ei filteröi opiskeluoikeuksia sen mukaan, minkä tiedot kuuluisi näyttää listanäkymässä, jos samalla oppijalla on useita opiskeluoikeuksia.
  //        Valinta voidaan jättää joko Scalalle, käyttöliitymälle tai tehdä toinen query, joka tekee valinnan SQL:ssä.
  private def getOppijat(
    oppijaOids: Seq[String],
    oppilaitosOids: Option[Seq[String]],
    rajaaOVKelpoisiinOpiskeluoikeuksiin: Boolean = true,
    hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value
  ): Seq[ValpasOppijaRow] = {
    val keväänValmistumisjaksoAlku = rajapäivätService.keväänValmistumisjaksoAlku
    val keväänValmistumisjaksoLoppu = rajapäivätService.keväänValmistumisjaksoLoppu
    val keväänUlkopuolellaValmistumisjaksoAlku = rajapäivätService.keväänUlkopuolellaValmistumisjaksoAlku()
    val tarkastelupäivä = rajapäivätService.tarkastelupäivä
    val keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä = rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
    val hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja = rajapäivätService.perusopetussuorituksenNäyttämisenAikaraja

    val timedBlockname = oppijaOids.size match {
      case 0 => "getOppijatMultiple"
      case 1 => "getOppijatSingle"
      case _ => "getOppijatMultipleOids"
    }

    val haePerusopetuksenHakeutumisvalvontatiedot = Seq(HakeutumisvalvontaTieto.Perusopetus, HakeutumisvalvontaTieto.Kaikki).contains(hakeutumisvalvontaTieto)
    val haeNivelvaiheenHakeutusmisvalvontatiedot = Seq(HakeutumisvalvontaTieto.Nivelvaihe, HakeutumisvalvontaTieto.Kaikki).contains(hakeutumisvalvontaTieto)

    val nonEmptyOppijaOids = if (oppijaOids.nonEmpty) Some(oppijaOids) else None

    timed(timedBlockname, 10) {
      db.runDbSync(SQLHelpers.concatMany(
        Some(
          sql"""
  WITH
      """),
        nonEmptyOppijaOids.map(oids =>
          sql"""
  -- CTE: jos pyydettiin vain yhtä oppijaa, hae hänen master oid:nsa
  pyydetty_oppija AS (
    SELECT r_henkilo.master_oid
    FROM r_henkilo
    WHERE r_henkilo.oppija_oid = any($oids)
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
      (oppivelvollisuustiedot.oppivelvollisuusvoimassaasti >= $tarkastelupäivä) AS henkilo_on_oppivelvollinen,
      (date_part('year', syntymaaika) <= (date_part('year', to_date($tarkastelupäivä,'YYYY-MM-DD'))-17))
        AS henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
    FROM
      r_henkilo
      -- oppivelvollisuustiedot-näkymä hoitaa syntymäaika- ja mahdollisen peruskoulusta ennen lain voimaantuloa
      -- valmistumisen tarkistuksen: siinä ei ole tietoja kuin oppijoista, jotka ovat oppivelvollisuuden
      -- laajentamislain piirissä
      JOIN oppivelvollisuustiedot ON oppivelvollisuustiedot.oppija_oid = r_henkilo.oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = r_henkilo.oppija_oid
      """),
        oppilaitosOids.map(oids => sql"""
        AND r_opiskeluoikeus.oppilaitos_oid = any($oids)
      """),
        nonEmptyOppijaOids.map(_ => sql"""
      JOIN pyydetty_oppija ON pyydetty_oppija.master_oid = r_henkilo.master_oid
      """),
        if (rajaaOVKelpoisiinOpiskeluoikeuksiin) {
          Some(
            sql"""
      WHERE r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava IS TRUE
      """) }
        else {
          None
        },
          Some(sql"""
  )
  -- CTE: perusopetuksen opiskeluoikeudet, joiden hakeutumista oppilaitoksella on oikeus valvoa tällä hetkellä
  , hakeutumisvalvottava_peruskoulun_opiskeluoikeus AS (
    SELECT
      DISTINCT ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid,
      ov_kelvollinen_opiskeluoikeus.oppilaitos_oid,
      ov_kelvollinen_opiskeluoikeus.master_oid
    FROM
      ov_kelvollinen_opiskeluoikeus
      JOIN r_paatason_suoritus
        ON r_paatason_suoritus.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      -- Lasketaan voimassaolevien kotiopetusjaksojen määrä ehtoa varten
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          jsonb_array_elements(
            ov_kelvollinen_opiskeluoikeus.data -> 'lisätiedot' -> 'kotiopetusjaksot'
          ) jaksot
        WHERE
          jaksot IS NOT NULL
            AND (
              jaksot ->> 'loppu' IS NULL
              OR $tarkastelupäivä BETWEEN jaksot ->> 'alku' AND jaksot ->> 'loppu')
        LIMIT 1
      ) kotiopetusjaksoja ON TRUE
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          to_jsonb(ov_kelvollinen_opiskeluoikeus.data -> 'lisätiedot' -> 'kotiopetus') jakso
        WHERE
          jakso IS NOT NULL
            AND (
              jakso ->> 'loppu' IS NULL
              OR $tarkastelupäivä BETWEEN jakso ->> 'alku' AND jakso ->> 'loppu'
            )
        LIMIT 1
      ) kotiopetus ON TRUE
    WHERE
      $haePerusopetuksenHakeutumisvalvontatiedot IS TRUE
      -- (0) henkilö on oppivelvollinen: hakeutumisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      AND ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- (1) oppijalla on peruskoulun opiskeluoikeus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'perusopetus'
      AND (
        -- (2.1) kyseisessä opiskeluoikeudessa on yhdeksännen luokan suoritus.
        (
          r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
          AND r_paatason_suoritus.koulutusmoduuli_koodiarvo = '9'
        )
        -- (2.2) TAI oppija täyttää vähintään 17 vuotta tarkasteluvuonna: heidät näytetään luokka-asteesta
        -- riippumatta, koska voivat lopettaa peruskoulun ja siirtyä seuraavaan opetukseen, vaikka olisivat esim.
        -- vasta 8. luokalla
        OR (
          ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
        )
        -- (2.3) TAI oppija on valmistunut peruskoulusta: heidät näytetään luokka-asteesta riippumatta,
        -- koska poikkeustapauksissa peruskoulusta voi valmistua myös ilman 9. luokan suoritusmerkintää Koskessa
        OR (
          ov_kelvollinen_opiskeluoikeus.viimeisin_tila = 'valmistunut'
        )
      )
      -- (3a) valvojalla on oppilaitostason oppilaitosoikeus ja opiskeluoikeuden lisätiedoista ei löydy
      -- kotiopetusjaksoa, joka osuu tälle hetkelle
      -- TODO (3b): puuttuu, koska ei vielä ole selvää, miten kotiopetusoppilaat halutaan käsitellä
      AND kotiopetusjaksoja.loytyi IS NOT TRUE
      AND kotiopetus.loytyi IS NOT TRUE
      AND (
        -- (4.1) opiskeluoikeus ei ole eronnut tilassa tällä hetkellä
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND NOT aikajakson_keskella.tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
          OR (aikajakson_keskella.tila IS NULL
            AND $tarkastelupäivä < ov_kelvollinen_opiskeluoikeus.alkamispaiva)
          OR (aikajakson_keskella.tila IS NULL
            AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva
            AND NOT ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
        )
        OR (
        -- (4.2) TAI oppija täyttää vähintään 17 tarkasteluvuonna ja on eronnut tilassa
          ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
          AND (
            (aikajakson_keskella.tila IS NOT NULL
              AND aikajakson_keskella.tila = any('{eronnut, katsotaaneronneeksi, keskeytynyt}'))
            OR (aikajakson_keskella.tila IS NULL
              AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva
              AND ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, keskeytynyt}'))
          )
        )
      )
      AND (
        -- (5a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tai lomalla tällä hetkellä.
        -- Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}'))
          -- 5a.1 vasta syksyllä (1.8. tai myöhemmin) 9. luokan aloittavia ei näytetä ennen kevään viimeistä rajapäivää.
          AND (
            r_paatason_suoritus.data ->> 'alkamispäivä' <= $keväänValmistumisjaksoLoppu
            OR $tarkastelupäivä > $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
          )
        )
        -- TAI:
        OR (
          -- (5b.1) opiskeluoikeus on päättynyt menneisyydessä
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
          AND (
            -- (5b.1.1) ja opiskeluoikeus on valmistunut-tilassa (joten siitä löytyy vahvistettu päättötodistus)
            (
              ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{valmistunut, hyvaksytystisuoritettu}')
            )
            -- (5b.1.2) tai oppija täyttää tarkasteluvuonna vähintään 17 ja opiskeluoikeus on eronnut-tilassa
            OR (
              ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
              AND ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, keskeytynyt}')
            )
          )
          -- (5b.2) ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön valmistumis-/eroamisajasta.
          AND (
            (
              -- keväällä valmistunut/eronnut ja tarkastellaan heille määrättyä rajapäivää aiemmin
              (ov_kelvollinen_opiskeluoikeus.paattymispaiva
                BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
              AND $tarkastelupäivä <= $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
            )
            OR (
              -- tai muuna aikana valmistunut/eronnut ja tarkastellaan heille määrättyä rajapäivää aiemmin
              (ov_kelvollinen_opiskeluoikeus.paattymispaiva
                NOT BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
              AND (ov_kelvollinen_opiskeluoikeus.paattymispaiva >= $keväänUlkopuolellaValmistumisjaksoAlku)
            )
          )
        )
      )
  )
  , hakeutumisvalvottava_international_schoolin_opiskeluoikeus AS (
    SELECT
      DISTINCT ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid,
      ov_kelvollinen_opiskeluoikeus.oppilaitos_oid,
      ov_kelvollinen_opiskeluoikeus.master_oid
    FROM
      ov_kelvollinen_opiskeluoikeus
      JOIN r_paatason_suoritus
        ON r_paatason_suoritus.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
    WHERE
      $haePerusopetuksenHakeutumisvalvontatiedot IS TRUE
      -- (0) henkilö on oppivelvollinen: hakeutumisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      AND ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- (1) oppijalla on International schoolin opiskeluoikeus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'internationalschool'
      AND (
        -- (B2.1) kyseisessä opiskeluoikeudessa on yhdeksännen luokan suoritus.
        (
          r_paatason_suoritus.koulutusmoduuli_koodiarvo = '9'
        )
        -- (B2.2) TAI oppija täyttää vähintään 17 vuotta tarkasteluvuonna ja hänellä on jokin international schoolin
        -- perusopetuksen päätason suoritus: heidät näytetään luokka-asteesta
        -- riippumatta, koska voivat lopettaa international schoolin ja siirtyä seuraavaan opetukseen, vaikka
        -- olisivat esim. vasta 8. luokalla
        OR (
          ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
          AND (
            r_paatason_suoritus.koulutusmoduuli_koodiarvo = '8'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '7'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '6'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '5'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '4'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '3'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '2'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = '1'
          )
        )
      )
      AND (
        -- (4.1) opiskeluoikeus ei ole eronnut tilassa tällä hetkellä
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND NOT aikajakson_keskella.tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
          OR (aikajakson_keskella.tila IS NULL
            AND $tarkastelupäivä < ov_kelvollinen_opiskeluoikeus.alkamispaiva)
          OR (aikajakson_keskella.tila IS NULL
            AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva
            AND NOT ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
        )
        OR (
        -- (4.2) TAI oppija täyttää vähintään 17 tarkasteluvuonna ja on eronnut tilassa
          ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
          AND (
            (aikajakson_keskella.tila IS NOT NULL
              AND aikajakson_keskella.tila = any('{eronnut, katsotaaneronneeksi, keskeytynyt}'))
            OR (aikajakson_keskella.tila IS NULL
              AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva
              AND ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, keskeytynyt}'))
          )
        )
      )
      AND (
        -- (B5a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tai lomalla tällä hetkellä.
        -- Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}'))
          -- B5a.1 vasta syksyllä (1.8. tai myöhemmin) 9. luokan aloittavia ei näytetä ennen kevään viimeistä rajapäivää.
          AND (
            r_paatason_suoritus.data ->> 'alkamispäivä' <= $keväänValmistumisjaksoLoppu
            OR $tarkastelupäivä > $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
          )
        )
        -- TAI:
        OR (
          -- (B5b.1) 9. luokka on tullut suoritetetuksi menneisyydessä
          (
            (
              r_paatason_suoritus.vahvistus_paiva IS NOT NULL
              AND $tarkastelupäivä >= r_paatason_suoritus.vahvistus_paiva
              -- (B5b.2) ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön 9. luokan suorittamisajasta.
              AND (
                (
                  -- keväällä 9. luokan suorittanut ja tarkastellaan heille määrättyä rajapäivää aiemmin
                  (r_paatason_suoritus.vahvistus_paiva
                    BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
                  AND $tarkastelupäivä <= $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
                )
                OR (
                  -- tai muuna aikana 9. luokan suorittanut ja tarkastellaan heille määrättyä rajapäivää aiemmin
                  (r_paatason_suoritus.vahvistus_paiva
                    NOT BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
                  AND (r_paatason_suoritus.vahvistus_paiva >= $keväänUlkopuolellaValmistumisjaksoAlku)
                )
              )
            )
            -- (B5b.1.2) tai oppija täyttää tarkasteluvuonna vähintään 17 ja opiskeluoikeus on eronnut-tilassa
            OR (
              ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
              AND ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, keskeytynyt}')
              -- (B5b.2) ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön eroamisajasta.
              AND (
                (
                  -- keväällä eronnut ja tarkastellaan heille määrättyä rajapäivää aiemmin
                  (ov_kelvollinen_opiskeluoikeus.paattymispaiva
                    BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
                  AND $tarkastelupäivä <= $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
                )
                OR (
                  -- tai muuna aikana eronnut ja tarkastellaan heille määrättyä rajapäivää aiemmin
                  (ov_kelvollinen_opiskeluoikeus.paattymispaiva
                    NOT BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
                  AND (ov_kelvollinen_opiskeluoikeus.paattymispaiva >= $keväänUlkopuolellaValmistumisjaksoAlku)
                )
              )
            )
          )
        )
      )
  )
  , hakeutumisvalvottava_nivelvaiheen_opiskeluoikeus AS (
    SELECT
      DISTINCT ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid,
      ov_kelvollinen_opiskeluoikeus.oppilaitos_oid,
      ov_kelvollinen_opiskeluoikeus.master_oid
    FROM
      ov_kelvollinen_opiskeluoikeus
      JOIN r_paatason_suoritus
        ON r_paatason_suoritus.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      -- Tutkitaan päätason suoritusten tyypit valma/telma-tutkimista varten
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'ammatillinenkoulutus'
          AND pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
          AND pts.suorituksen_tyyppi <> 'telma'
          AND pts.suorituksen_tyyppi <> 'valma'
        LIMIT 1
      ) AS muita_suorituksia_kuin_telma_ja_valma ON TRUE
    WHERE
      $haeNivelvaiheenHakeutusmisvalvontatiedot IS TRUE
      -- (0) henkilö on oppivelvollinen: hakeutumisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      AND ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- Oppijalla on nivelvaiheen opiskeluoikeus-päätason suoritus -pari:
      AND (
        (
          ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'aikuistenperusopetus'
          AND (
            r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe'
            OR r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
          )
        )
        OR ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'luva'
        OR ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'perusopetuksenlisaopetus'
        OR (
          ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'vapaansivistystyonkoulutus'
          AND (
            r_paatason_suoritus.suorituksen_tyyppi = 'vstmaahanmuuttajienkotoutumiskoulutus'
            OR r_paatason_suoritus.suorituksen_tyyppi = 'vstoppivelvollisillesuunnattukoulutus'
            OR r_paatason_suoritus.suorituksen_tyyppi = 'vstlukutaitokoulutus'
          )
        )
        OR (
          ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'ammatillinenkoulutus'
          AND (
            r_paatason_suoritus.suorituksen_tyyppi = 'telma'
            OR r_paatason_suoritus.suorituksen_tyyppi = 'valma'
          )
          AND muita_suorituksia_kuin_telma_ja_valma.loytyi IS NOT TRUE
        )
      )
      AND (
        -- opiskeluoikeus ei ole eronnut tilassa tällä hetkellä
        (aikajakson_keskella.tila IS NOT NULL
          AND NOT aikajakson_keskella.tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
        OR (aikajakson_keskella.tila IS NULL
          AND $tarkastelupäivä < ov_kelvollinen_opiskeluoikeus.alkamispaiva)
        OR (aikajakson_keskella.tila IS NULL
          AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva
          AND NOT ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
      )
      AND (
        -- opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tai lomalla tällä hetkellä.
        -- Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}'))
          -- vasta syksyllä (1.8. tai myöhemmin) nivelvaiheen aloittavia ei näytetä ennen kevään viimeistä rajapäivää.
          AND (
            ov_kelvollinen_opiskeluoikeus.alkamispaiva <= $keväänValmistumisjaksoLoppu
            OR $tarkastelupäivä > $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
          )
        )
        -- TAI:
        OR (
          -- opiskeluoikeus on päättynyt menneisyydessä
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
          AND (
            -- ja opiskeluoikeus on valmistunut-tilassa
            (
              ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{valmistunut, hyvaksytystisuoritettu}')
            )
          )
          -- ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön valmistumis-/eroamisajasta.
          AND (
            (
              -- keväällä valmistunut/eronnut ja tarkastellaan heille määrättyä rajapäivää aiemmin
              (ov_kelvollinen_opiskeluoikeus.paattymispaiva
                BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
              AND $tarkastelupäivä <= $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
            )
            OR (
              -- tai muuna aikana valmistunut/eronnut ja tarkastellaan heille määrättyä rajapäivää aiemmin
              (ov_kelvollinen_opiskeluoikeus.paattymispaiva
                NOT BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
              AND (ov_kelvollinen_opiskeluoikeus.paattymispaiva >= $keväänUlkopuolellaValmistumisjaksoAlku)
            )
          )
        )
      )
  )
  , hakeutumisvalvottava_opiskeluoikeus AS (
    SELECT
      *
    FROM
      hakeutumisvalvottava_peruskoulun_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
      hakeutumisvalvottava_international_schoolin_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
      hakeutumisvalvottava_nivelvaiheen_opiskeluoikeus
  )
  -- CTE: opiskeluoikeudet, joissa oppivelvollisuuden suorittamista oppilaitoksella on oikeus
  -- valvoa tällä hetkellä
  , suorittamisvalvottava_opiskeluoikeus AS (
    SELECT
      DISTINCT ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid,
      ov_kelvollinen_opiskeluoikeus.oppilaitos_oid,
      ov_kelvollinen_opiskeluoikeus.master_oid
    FROM
      ov_kelvollinen_opiskeluoikeus
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
          AND (
            (pts.suorituksen_tyyppi = 'internationalschoolmypvuosiluokka'
              AND pts.koulutusmoduuli_koodiarvo = '10')
            OR pts.suorituksen_tyyppi = 'internationalschooldiplomavuosiluokka'
          )
        LIMIT 1
      ) AS international_school_toisen_asteen_suorituksia ON TRUE
    WHERE
      -- (0) henkilö on oppivelvollinen: suorittamisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- (1) oppijalla on muu kuin peruskoulun opetusoikeus tai esiopetus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto <> 'perusopetus'
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto <> 'esiopetus'
      -- (1b) International school on suorittamisvalvottava vain, jos siinä on 10, 11 tai 12 luokan suoritus
      AND (
        ov_kelvollinen_opiskeluoikeus.koulutusmuoto <> 'internationalschool'
        OR international_school_toisen_asteen_suorituksia.loytyi IS TRUE
      )
      AND (
        -- (2a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tai lomalla tällä hetkellä.
        -- Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}'))
        )
        -- TAI:
        OR (
          -- (2b.1) opiskeluoikeus on päättynyt menneisyydessä eroamiseen. Valmistuneita ei pääsääntöisesti
          -- näytetä lainkaan toiselta asteelta valmistuminen tarkoittaa, että oppivelvollisuus päättyy
          -- kokonaan, ja nivelvaiheen valmistuneiden käsittely tehdään hakeutumisen valvonnan kautta.
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
          AND (ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi, peruutettu, keskeytynyt}'))
        )
        -- TAI
        OR (
          -- (2b.3) Oppija on valmistunut lukiosta: toistaiseksi oletetaan, että hänellä on vielä YO-tutkinto
          -- suorittamatta, koska tietoa sen suorittamisesta ei ole helposti saatavilla ja oppivelvollisuus
          -- päättyy vasta YO-tutkinnon suorittamiseen.
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
          AND (ov_kelvollinen_opiskeluoikeus.viimeisin_tila = 'valmistunut')
          AND (ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'lukiokoulutus')
        )
      )
  )
  -- CTE: kaikki uuden lain piirissä olevat oppijat, joilla on vähintään yksi oppivelvollisuuden suorittamiseen
  -- kelpaava opiskeluoikeus. Mukana myös taulukko hakeutumisvalvontaan ja suoritusvalvontaan
  -- kelpuutettavien opiskeluoikeuksien oppilaitoksista käyttöoikeustarkastelua varten.
  , oppija AS (
    SELECT
      DISTINCT r_henkilo.master_oid,
      r_henkilo.syntymaaika,
      r_henkilo.etunimet,
      r_henkilo.sukunimi,
      -- Jos oppijalla on eri hetu-tieto slave/master:eilla, ei hänen kuitenkaan kuulu näkyä kuin kerran Valppaassa:
      (array_remove(array_agg(DISTINCT r_henkilo.hetu), NULL))[1]
        AS hetu,
      array_remove(array_agg(DISTINCT hakeutumisvalvottava_opiskeluoikeus.oppilaitos_oid), NULL)
        AS hakeutumisvalvova_oppilaitos_oids,
      array_remove(array_agg(DISTINCT hakeutumisvalvottava_opiskeluoikeus.opiskeluoikeus_oid), NULL)
        AS hakeutumisvalvottava_opiskeluoikeus_oids,
      array_remove(array_agg(DISTINCT suorittamisvalvottava_opiskeluoikeus.oppilaitos_oid), NULL)
        AS suorittamisvalvova_oppilaitos_oids,
      array_remove(array_agg(DISTINCT suorittamisvalvottava_opiskeluoikeus.opiskeluoikeus_oid), NULL)
        AS suorittamisvalvottava_opiskeluoikeus_oids,
      r_henkilo.turvakielto,
      r_henkilo.aidinkieli,
      array_agg(DISTINCT kaikki_henkilot.oppija_oid) AS kaikkiOppijaOidit,
      oppivelvollisuustiedot.oppivelvollisuusvoimassaasti AS oppivelvollisuus_voimassa_asti,
      oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti
        AS oikeus_koulutuksen_maksuttomuuteen_voimassa_asti
    FROM
      r_henkilo
      -- oppivelvollisuustiedot-näkymä hoitaa syntymäaika- ja mahdollisen peruskoulusta ennen lain voimaantuloa
      -- valmistumisen tarkistuksen: siinä ei ole tietoja kuin oppijoista, jotka ovat oppivelvollisuuden
      -- laajentamislain piirissä
      JOIN oppivelvollisuustiedot ON oppivelvollisuustiedot.oppija_oid = r_henkilo.oppija_oid
      """),
        oppilaitosOids.map(_ => sql"""
      -- Optimointi: Kun haetaan oppilaitoksen perusteella, palautetaan vain oppilaitoksen valvottavat oppijat.
      -- Muuten esim. peruskoulussa palautettaisiin turhaan kaikki tiedot ekaluokkalaisista lähtien, mikä hidastaa
      -- queryä ja kasvattaa resultsettiä huomattavasti. Subselect unionista on tässä 2 kertaluokkaa nopeampi käytännössä kuin
      -- WHERE-ehto, jossa tarkistettaisiin, onko jommassa kummassa non-NULL -sisältö.
      JOIN (
        SELECT * FROM hakeutumisvalvottava_opiskeluoikeus
          UNION
        SELECT * FROM suorittamisvalvottava_opiskeluoikeus
      ) AS valvottava_opiskeluoikeus ON valvottava_opiskeluoikeus.master_oid = r_henkilo.master_oid
      """),
        Some(sql"""
      LEFT JOIN hakeutumisvalvottava_opiskeluoikeus ON hakeutumisvalvottava_opiskeluoikeus.master_oid = r_henkilo.master_oid
      LEFT JOIN suorittamisvalvottava_opiskeluoikeus ON suorittamisvalvottava_opiskeluoikeus.master_oid = r_henkilo.master_oid
      -- Haetaan kaikki oppijan oidit: pitää palauttaa esim. kuntailmoitusten kyselyä varten
      JOIN r_henkilo kaikki_henkilot ON kaikki_henkilot.master_oid = r_henkilo.master_oid
      """),
        nonEmptyOppijaOids.map(_ => sql"""
      -- Jos haetaan oppijan oid:n perusteella, on oppijalla oltava vähintään yksi oppivelvollisuuskelvollinen
      -- opiskeluoikeus:
    WHERE
      EXISTS (SELECT 1 FROM ov_kelvollinen_opiskeluoikeus
        WHERE ov_kelvollinen_opiskeluoikeus.master_oid = r_henkilo.master_oid)
      """),
      Some(sql"""
    GROUP BY
      r_henkilo.master_oid,
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
        ('keskeytynyt', 'keskeytynyt'),
        ('hyvaksytystisuoritettu', 'hyvaksytystisuoritettu'),
        (NULL, 'tuntematon')
      ) t
  )
  -- CTE: peruskoulun opiskeluoikeudet. Ei sama lista kuin aiemmassa CTE:ssä, koska voi olla rinnakkaisia tai
  -- peräkkäisiä muita peruskoulun opiskeluoikeuksia. Teoriassa varmaan voisi tehostaa kyselyä jotenkin ja välttää
  -- näiden hakeminen uudestaan, mutta kysely voisi mennä melko monimutkaiseksi.
  , peruskoulun_opiskeluoikeus AS (
    SELECT
      oppija_oid.master_oid,
      r_opiskeluoikeus.opiskeluoikeus_oid,
      r_opiskeluoikeus.koulutusmuoto,
      r_opiskeluoikeus.oppilaitos_oid,
      r_opiskeluoikeus.oppilaitos_nimi,
      r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava,
      valittu_paatason_suoritus.data AS paatason_suoritukset,
      r_opiskeluoikeus.alkamispaiva,
      r_opiskeluoikeus.paattymispaiva,
      CASE
        WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
        WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
        ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
      END tarkastelupaivan_tila,
      jsonb_build_object(
        'alkamispäivä', r_opiskeluoikeus.alkamispaiva,
        'päättymispäivä', r_opiskeluoikeus.paattymispaiva,
        'päättymispäiväMerkittyTulevaisuuteen', r_opiskeluoikeus.paattymispaiva > $tarkastelupäivä,
        'tarkastelupäivänTila', jsonb_build_object(
          'koodiarvo',
            (CASE
              WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
              WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
              ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
            END),
          'koodistoUri', 'valpasopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTila', jsonb_build_object(
          'koodiarvo',
            (CASE
              -- Jos opiskeluoikeus on tulevaisuudessa, käytetään läsnä-tilaa toistaiseksi. Tätä tilannetta ei tällä hetkellä koskaan
              -- Valppaassa näytetä, joten jos vaikka opiskeluoikeus alkaisikin muuten kuin läsnä-tilaisena, ei tämä aiheuta mitään
              -- ongelmaa.
              WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'lasna'
              WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.viimeisin_tila
              ELSE aikajakson_keskella.tila
            END),
          'koodistoUri', 'koskiopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTilanAlkamispäivä', (CASE
          WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN r_opiskeluoikeus.alkamispaiva
          WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.paattymispaiva
          ELSE aikajakson_keskella.tila_alkanut
        END),
        'valmistunutAiemminTaiLähitulevaisuudessa', (
          r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
          AND coalesce(r_opiskeluoikeus.paattymispaiva < $hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja, FALSE)
        ),
        'vuosiluokkiinSitomatonOpetus', coalesce((r_opiskeluoikeus.data -> 'lisätiedot' ->> 'vuosiluokkiinSitoutumatonOpetus')::boolean, FALSE)
      ) AS perusopetus_tiedot,
      NULL::jsonb AS perusopetuksen_jalkeinen_tiedot
    FROM
      oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
        AND r_opiskeluoikeus.koulutusmuoto = 'perusopetus'
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      LEFT JOIN valpastila valpastila_aikajakson_keskella
        ON valpastila_aikajakson_keskella.koskiopiskeluoikeudentila = aikajakson_keskella.tila
      LEFT JOIN valpastila valpastila_viimeisin
        ON valpastila_viimeisin.koskiopiskeluoikeudentila = r_opiskeluoikeus.viimeisin_tila
      -- Haetaan päätason suoritus, jonka dataa halutaan näyttää (toistaiseksi valitaan alkamispäivän perusteella uusin)
      -- TODO: Ei välttämättä osu oikeaan, koska voi olla esim. monen eri tyyppisiä peruskoulun päätason suorituksia,
      -- ja pitäisi oikeasti filteröidä myös tyypin perusteella.
      -- TODO: Voisi toteuttaa tutkittavan ajanhetken tarkistuksen tähänkin, että näytetään luokkatieto sen mukaan,
      -- millä luokalla on ollut tutkittavalla ajanhetkellä. Menneisyyden tarkastelulle tällä tarkkuudella ei
      -- kuitenkaan ole toistaiseksi ilmennyt tarvetta.
      CROSS JOIN LATERAL (
        SELECT
          jsonb_build_array(
            jsonb_build_object(
              'suorituksenTyyppi', jsonb_build_object(
                'koodiarvo', pts.suorituksen_tyyppi,
                'koodistoUri', 'suorituksentyyppi'
              ),
              'toimipiste', jsonb_build_object(
                'oid', pts.toimipiste_oid,
                'nimi', jsonb_build_object(
                  'fi', pts.toimipiste_nimi
                )
              ),
              'ryhmä', pts.data->>'luokka'
            )
          ) AS data
        FROM r_paatason_suoritus pts
        WHERE pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          AND (
            pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
            OR pts.suorituksen_tyyppi = 'perusopetuksenoppimaara'
          )
        -- Ensisijaisesti uusin vuosiluokan suoritus, koska siinä on luultavasti relevantein ryhmätieto.
        ORDER BY
          CASE
            WHEN (pts.suorituksen_tyyppi = 'perusopetuksenvuosiluokka') THEN 1
            WHEN (pts.suorituksen_tyyppi = 'perusopetuksenoppimaara') THEN 2
          END,
          pts.data->>'alkamispäivä' DESC
        LIMIT 1
      ) AS valittu_paatason_suoritus
  )
  -- CTE: International schoolin opiskeluoikeudet
  -- Pitää hakea erikseen, koska sisältää sekä perusopetuksen että sen jälkeisen koulutuksen tietoja
  , international_schoolin_opiskeluoikeus AS (
    SELECT
      oppija_oid.master_oid,
      r_opiskeluoikeus.opiskeluoikeus_oid,
      r_opiskeluoikeus.koulutusmuoto,
      r_opiskeluoikeus.oppilaitos_oid,
      r_opiskeluoikeus.oppilaitos_nimi,
      r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava,
      kaikki_paatason_suoritukset.data AS paatason_suoritukset,
      r_opiskeluoikeus.alkamispaiva,
      r_opiskeluoikeus.paattymispaiva,
      (CASE
        WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
        WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
        ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
      END) AS tarkastelupaivan_tila,
      (CASE
        WHEN perusopetuksen_suorituksia.loytyi IS TRUE THEN
          (jsonb_build_object(
            'alkamispäivä', r_opiskeluoikeus.alkamispaiva,
            'päättymispäivä',
              -- Jos perusopetuksen 9. luokka on vahvistettu, käytetään sitä. Jos sitä ei ole vahvistettu, ja on toisen asteen suorituksia,
              -- ei päättymispäivää ole lainkaan. Jos taas toisen asteen suorituksia ei ole lainkaan, käytetään koko oo:n päättymispäivää.
              CASE
                WHEN ysiluokan_suoritus.vahvistus_paiva IS NOT NULL THEN ysiluokan_suoritus.vahvistus_paiva
                WHEN toisen_asteen_suorituksia.lukumaara > 0 THEN NULL::date
                ELSE r_opiskeluoikeus.paattymispaiva
              END,
            'päättymispäiväMerkittyTulevaisuuteen', (
              CASE
                WHEN ysiluokan_suoritus.vahvistus_paiva IS NOT NULL THEN ysiluokan_suoritus.vahvistus_paiva
                WHEN toisen_asteen_suorituksia.lukumaara > 0 THEN NULL::date
                ELSE r_opiskeluoikeus.paattymispaiva
              END) > $tarkastelupäivä,
            'tarkastelupäivänTila', jsonb_build_object(
              'koodiarvo',
                (CASE
                  WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
                  -- Jos ysiluokka on vahvistettu ja tutkitaan vahvistuspäivämäärän jälkeen, käytetään tilaa valmistunut:
                  WHEN ysiluokan_suoritus.vahvistus_paiva IS NOT NULL AND ysiluokan_suoritus.vahvistus_paiva <= $tarkastelupäivä THEN 'valmistunut'
                  -- Muuten käytetään opiskeluoikeuden tietoja:
                  WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
                  ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
                END),
              'koodistoUri', 'valpasopiskeluoikeudentila'
            ),
            'tarkastelupäivänKoskiTila', jsonb_build_object(
              'koodiarvo',
                (CASE
                  -- Jos opiskeluoikeus on tulevaisuudessa, käytetään läsnä-tilaa toistaiseksi. Tätä tilannetta ei tällä hetkellä koskaan
                  -- Valppaassa näytetä, joten jos vaikka opiskeluoikeus alkaisikin muuten kuin läsnä-tilaisena, ei tämä aiheuta mitään
                  -- ongelmaa.
                  WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'lasna'
                  -- Jos ysiluokka on vahvistettu ja tutkitaan vahvistuspäivämäärän jälkeen, käytetään tilaa valmistunut:
                  WHEN ysiluokan_suoritus.vahvistus_paiva IS NOT NULL AND ysiluokan_suoritus.vahvistus_paiva <= $tarkastelupäivä THEN 'valmistunut'
                  -- Muuten käytetään opiskeluoikeuden tietoja:
                  WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.viimeisin_tila
                  ELSE aikajakson_keskella.tila
                END),
              'koodistoUri', 'koskiopiskeluoikeudentila'
            ),
            'tarkastelupäivänKoskiTilanAlkamispäivä',
              (CASE
                WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN r_opiskeluoikeus.alkamispaiva
                -- Jos ysiluokka on vahvistettu ja tutkitaan vahvistuspäivämäärän jälkeen, käytetään vahvistuspäivää:
                WHEN (ysiluokan_suoritus.vahvistus_paiva IS NOT NULL AND ysiluokan_suoritus.vahvistus_paiva <= $tarkastelupäivä) THEN ysiluokan_suoritus.vahvistus_paiva
                -- Muuten käytetään opiskeluoikeuden tietoja:
                WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.paattymispaiva
                ELSE aikajakson_keskella.tila_alkanut
              END),
            'valmistunutAiemminTaiLähitulevaisuudessa', (
              ysiluokan_suoritus.vahvistus_paiva IS NOT NULL
              AND ysiluokan_suoritus.vahvistus_paiva < $hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja
            ),
            'vuosiluokkiinSitomatonOpetus', FALSE
          ))
        ELSE NULL::jsonb
      END) AS perusopetus_tiedot,
      jsonb_build_object(
        'alkamispäivä',
          (CASE
            -- Jos ei ole toisen asteen suorituksia, eikä perusopetuksen suorituksia, käytetään opiskeluoikeuden alkamispäivää
            WHEN (
              (toisen_asteen_suorituksia.lukumaara IS NULL OR toisen_asteen_suorituksia.lukumaara = 0)
              AND (perusopetuksen_suorituksia.loytyi IS NOT TRUE)
            ) THEN r_opiskeluoikeus.alkamispaiva
            -- Jos on pelkkiä toisen asteen suorituksia, mutta niille ei ole suorituksissa määritelty alkamispäivää, voidaan käyttää
            -- fallbackinä koko opiskeluoikeuden alkamispäivää
            WHEN (
              (perusopetuksen_suorituksia.loytyi IS NOT TRUE)
              AND (toisen_asteen_suorituksia.aikaisin_alkamispaiva IS NULL)
            ) THEN r_opiskeluoikeus.alkamispaiva
            -- Muutoin käytetään toisen asteen suoritusten aikaisinta alkamispäivää (joka saattaa myös olla null, jos toisen asteen suorituksia ei ole siirretty tai
            -- niille ei ole siirretty alkamispäivää)
            ELSE toisen_asteen_suorituksia.aikaisin_alkamispaiva
          END),
        'päättymispäivä', r_opiskeluoikeus.paattymispaiva,
        'päättymispäiväMerkittyTulevaisuuteen', r_opiskeluoikeus.paattymispaiva > $tarkastelupäivä,
        'tarkastelupäivänTila', jsonb_build_object(
          'koodiarvo',
            (CASE
              -- Jos toisen asteen alkamispäivä on määritelty, ja tutkitaan sitä aikaisemmin, käytetään voimassatulevaisuudessa:
              WHEN toisen_asteen_suorituksia.aikaisin_alkamispaiva IS NOT NULL AND $tarkastelupäivä < toisen_asteen_suorituksia.aikaisin_alkamispaiva THEN 'voimassatulevaisuudessa'
              -- Muuten käytetään opiskeluoikeuden tietoja:
              WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
              WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
              ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
            END),
          'koodistoUri', 'valpasopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTila', jsonb_build_object(
          'koodiarvo',
            (CASE
              -- Jos opiskeluoikeus on tulevaisuudessa, käytetään läsnä-tilaa toistaiseksi. Tätä tilannetta ei tällä hetkellä koskaan
              -- Valppaassa näytetä, joten jos vaikka opiskeluoikeus alkaisikin muuten kuin läsnä-tilaisena, ei tämä aiheuta mitään
              -- ongelmaa.
              WHEN toisen_asteen_suorituksia.aikaisin_alkamispaiva IS NOT NULL AND $tarkastelupäivä < toisen_asteen_suorituksia.aikaisin_alkamispaiva THEN 'lasna'
              WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'lasna'
              WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.viimeisin_tila
              ELSE aikajakson_keskella.tila
            END),
          'koodistoUri', 'koskiopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTilanAlkamispäivä', (CASE
          WHEN toisen_asteen_suorituksia.aikaisin_alkamispaiva IS NOT NULL AND $tarkastelupäivä <= toisen_asteen_suorituksia.aikaisin_alkamispaiva THEN toisen_asteen_suorituksia.aikaisin_alkamispaiva
          WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN r_opiskeluoikeus.alkamispaiva
          WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.paattymispaiva
          ELSE aikajakson_keskella.tila_alkanut
        END),
        'valmistunutAiemminTaiLähitulevaisuudessa', (
          r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
          AND coalesce(r_opiskeluoikeus.paattymispaiva < $hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja, FALSE)
        ),
        -- Tarvitaan, jotta voidaan näyttää international schoolissa itsessään käytävät jatko-opinnot oikein hakeutumisvalvonnan näkymässä ainoastaan
        -- silloin, kun niissä on oikeasti kirjattuja 10+ luokan suorituksia
        'näytäMuunaPerusopetuksenJälkeisenäOpintona', (toisen_asteen_suorituksia.lukumaara > 0)
      ) AS perusopetuksen_jalkeinen_tiedot
    FROM
      oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
        AND r_opiskeluoikeus.koulutusmuoto = 'internationalschool'
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      LEFT JOIN valpastila valpastila_aikajakson_keskella
        ON valpastila_aikajakson_keskella.koskiopiskeluoikeudentila = aikajakson_keskella.tila
      LEFT JOIN valpastila valpastila_viimeisin
        ON valpastila_viimeisin.koskiopiskeluoikeudentila = r_opiskeluoikeus.viimeisin_tila
      CROSS JOIN LATERAL (
        SELECT jsonb_agg(
          jsonb_build_object(
            'suorituksenTyyppi', jsonb_build_object(
              'koodiarvo', pts.suorituksen_tyyppi,
              'koodistoUri', 'suorituksentyyppi'
            ),
            'toimipiste', jsonb_build_object(
              'oid', pts.toimipiste_oid,
              'nimi', jsonb_build_object(
                'fi', pts.toimipiste_nimi
              )
            ),
            'ryhmä', pts.data->>'luokka'
          )
          -- Haetaan päätason suoritukset vahvistus- tai arviointipäivien mukaisessa järjestyksessä.
          -- Yleensä käytetään tässä järjestyksessä ensimmäistä, mutta tähän on poikkeus.
          -- Teoriassa voisi tutkia päätason suorituksen osasuorituksiin kirjattuja päivämääriä, mutta se on aika
          -- monimutkaista ja luultavasti myös hidasta.
          ORDER BY
            pts.vahvistus_paiva DESC NULLS FIRST,
            pts.arviointi_paiva DESC NULLS FIRST
        ) AS data
        FROM r_paatason_suoritus pts
        WHERE pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        GROUP BY pts.opiskeluoikeus_oid
      ) AS kaikki_paatason_suoritukset
      -- Optimointi: PostgreSQL optimoi LEFT JOIN LATERAL ... ON TRUE :n huomattavasti paremmin kuin tavallisen LEFT JOINin
      LEFT JOIN LATERAL (
        SELECT
          vahvistus_paiva
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          AND pts.suorituksen_tyyppi = 'internationalschoolmypvuosiluokka'
          AND pts.koulutusmoduuli_koodiarvo = '9'
      ) AS ysiluokan_suoritus ON TRUE
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          AND (
            (pts.suorituksen_tyyppi = 'internationalschoolpypvuosiluokka')
            OR (
              pts.suorituksen_tyyppi = 'internationalschoolmypvuosiluokka'
              AND pts.koulutusmoduuli_koodiarvo <> '10'
            )
          )
        LIMIT 1
      ) AS perusopetuksen_suorituksia ON TRUE
      CROSS JOIN LATERAL (
        SELECT
          COUNT(*) AS lukumaara,
          MIN(pts.data->>'alkamispäivä')::date aikaisin_alkamispaiva
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          AND (
            (pts.suorituksen_tyyppi = 'internationalschooldiplomavuosiluokka')
            OR (pts.suorituksen_tyyppi = 'internationalschoolmypvuosiluokka'
              AND pts.koulutusmoduuli_koodiarvo = '10')
          )
      ) AS toisen_asteen_suorituksia
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
      r_opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava,
      kaikki_paatason_suoritukset.data AS paatason_suoritukset,
      r_opiskeluoikeus.alkamispaiva,
      r_opiskeluoikeus.paattymispaiva,
      CASE
        WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
        WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
        ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
      END tarkastelupaivan_tila,
      NULL::jsonb AS perusopetus_tiedot,
      jsonb_build_object(
        'alkamispäivä', r_opiskeluoikeus.alkamispaiva,
        'päättymispäivä', r_opiskeluoikeus.paattymispaiva,
        'päättymispäiväMerkittyTulevaisuuteen', r_opiskeluoikeus.paattymispaiva > $tarkastelupäivä,
        'tarkastelupäivänTila', jsonb_build_object(
          'koodiarvo',
            (CASE
              WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
              WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN valpastila_viimeisin.valpasopiskeluoikeudentila
              ELSE valpastila_aikajakson_keskella.valpasopiskeluoikeudentila
            END),
          'koodistoUri', 'valpasopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTila', jsonb_build_object(
          'koodiarvo',
            (CASE
              -- Jos opiskeluoikeus on tulevaisuudessa, käytetään läsnä-tilaa toistaiseksi. Tätä tilannetta ei tällä hetkellä koskaan
              -- Valppaassa näytetä, joten jos vaikka opiskeluoikeus alkaisikin muuten kuin läsnä-tilaisena, ei tämä aiheuta mitään
              -- ongelmaa.
              WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'lasna'
              WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.viimeisin_tila
              ELSE aikajakson_keskella.tila
            END),
          'koodistoUri', 'koskiopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTilanAlkamispäivä', (CASE
          WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN r_opiskeluoikeus.alkamispaiva
          WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.paattymispaiva
          ELSE aikajakson_keskella.tila_alkanut
        END),
        'valmistunutAiemminTaiLähitulevaisuudessa', (
          r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
          AND coalesce(r_opiskeluoikeus.paattymispaiva < $hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja, FALSE)
        ),
        'näytäMuunaPerusopetuksenJälkeisenäOpintona', TRUE
      ) AS perusopetuksen_jalkeinen_tiedot
    FROM
      oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
        AND r_opiskeluoikeus.koulutusmuoto <> 'perusopetus'
        AND r_opiskeluoikeus.koulutusmuoto <> 'internationalschool'
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      LEFT JOIN valpastila valpastila_aikajakson_keskella
        ON valpastila_aikajakson_keskella.koskiopiskeluoikeudentila = aikajakson_keskella.tila
      LEFT JOIN valpastila valpastila_viimeisin
        ON valpastila_viimeisin.koskiopiskeluoikeudentila = r_opiskeluoikeus.viimeisin_tila
      CROSS JOIN LATERAL (
        SELECT jsonb_agg(
          jsonb_build_object(
            'suorituksenTyyppi', jsonb_build_object(
              'koodiarvo', pts.suorituksen_tyyppi,
              'koodistoUri', 'suorituksentyyppi'
            ),
            'toimipiste', jsonb_build_object(
              'oid', pts.toimipiste_oid,
              'nimi', jsonb_build_object(
                'fi', pts.toimipiste_nimi
              )
            ),
            -- Osassa opiskeluoikeuksia kentän nimi on luokka eikä ryhmä
            'ryhmä', coalesce(pts.data->>'ryhmä', pts.data->>'luokka')
          )
          -- Haetaan päätason suoritukset vahvistus- tai arviointipäivien mukaisessa järjestyksessä.
          -- Yleensä käytetään tässä järjestyksessä ensimmäistä, mutta tähän on poikkeus.
          -- Teoriassa voisi tutkia päätason suorituksen osasuorituksiin kirjattuja päivämääriä, mutta se on aika
          -- monimutkaista ja luultavasti myös hidasta.
          ORDER BY
            pts.vahvistus_paiva DESC NULLS FIRST,
            pts.arviointi_paiva DESC NULLS FIRST
        ) AS data
        FROM r_paatason_suoritus pts
        WHERE pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        GROUP BY pts.opiskeluoikeus_oid
      ) AS kaikki_paatason_suoritukset
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
      international_schoolin_opiskeluoikeus
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
    oppija.hakeutumisvalvova_oppilaitos_oids AS hakeutumisvalvovatOppilaitokset,
    oppija.suorittamisvalvova_oppilaitos_oids AS suorittamisvalvovatOppilaitokset,
    oppija.turvakielto AS turvakielto,
    oppija.aidinkieli AS aidinkieli,
    oppija.oppivelvollisuus_voimassa_asti AS oppivelvollisuusVoimassaAsti,
    oppija.oikeus_koulutuksen_maksuttomuuteen_voimassa_asti AS oikeusKoulutuksenMaksuttomuuteenVoimassaAsti,
    (oppija.oikeus_koulutuksen_maksuttomuuteen_voimassa_asti >= $tarkastelupäivä) AS onOikeusValvoaMaksuttomuutta,
    (oppija.oppivelvollisuus_voimassa_asti >= $tarkastelupäivä) AS onOikeusValvoaKunnalla,
    jsonb_agg(
      jsonb_build_object(
        'oid', opiskeluoikeus.opiskeluoikeus_oid,
        'onHakeutumisValvottava',
          opiskeluoikeus.opiskeluoikeus_oid = ANY(oppija.hakeutumisvalvottava_opiskeluoikeus_oids),
        'onSuorittamisValvottava',
          opiskeluoikeus.opiskeluoikeus_oid = ANY(oppija.suorittamisvalvottava_opiskeluoikeus_oids),
        'tyyppi', jsonb_build_object(
          'koodiarvo', opiskeluoikeus.koulutusmuoto,
          'koodistoUri', 'opiskeluoikeudentyyppi'
        ),
        'oppilaitos', jsonb_build_object(
          'oid', opiskeluoikeus.oppilaitos_oid,
          'nimi', jsonb_build_object(
            'fi', opiskeluoikeus.oppilaitos_nimi
          )
        ),
        'oppivelvollisuudenSuorittamiseenKelpaava', opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava IS TRUE,
        'päätasonSuoritukset', opiskeluoikeus.paatason_suoritukset,
        'perusopetusTiedot', opiskeluoikeus.perusopetus_tiedot,
        'perusopetuksenJälkeinenTiedot', opiskeluoikeus.perusopetuksen_jalkeinen_tiedot
      ) ORDER BY
        opiskeluoikeus.alkamispaiva DESC,
        -- Alkamispäivä varmaan riittäisi käyttöliitymälle, mutta lisätään muita kenttiä testien pitämiseksi
        -- deteministisempinä myös päällekäisillä opiskeluoikeuksilla:
        opiskeluoikeus.paattymispaiva DESC,
        opiskeluoikeus.koulutusmuoto,
        opiskeluoikeus.paatason_suoritukset->0->>'ryhmä' DESC NULLS LAST,
        opiskeluoikeus.tarkastelupaivan_tila,
        opiskeluoikeus.oppilaitos_oid,
        opiskeluoikeus.paatason_suoritukset->0->'suorituksentyyppi'->>'koodiarvo',
        opiskeluoikeus.paatason_suoritukset->0->'toimipiste'->>'oid'
    ) opiskeluoikeudet
  FROM
    opiskeluoikeus
    JOIN oppija ON oppija.master_oid = opiskeluoikeus.master_oid
  """),
  if (rajaaOVKelpoisiinOpiskeluoikeuksiin) {
    Some(
      sql"""
  WHERE opiskeluoikeus.oppivelvollisuuden_suorittamiseen_kelpaava IS TRUE
    """)
  } else {
    None
  },
  Some(sql"""
  GROUP BY
    oppija.master_oid,
    oppija.kaikkiOppijaOidit,
    oppija.hetu,
    oppija.syntymaaika,
    oppija.etunimet,
    oppija.sukunimi,
    oppija.hakeutumisvalvova_oppilaitos_oids,
    oppija.suorittamisvalvova_oppilaitos_oids,
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
