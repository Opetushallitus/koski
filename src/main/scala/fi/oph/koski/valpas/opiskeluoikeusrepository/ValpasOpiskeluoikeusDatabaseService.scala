package fi.oph.koski.valpas.opiskeluoikeusrepository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DatabaseConverters, SQLHelpers}
import fi.oph.koski.log.Logging
import fi.oph.koski.util.DateOrdering.localDateOrdering
import org.json4s.{JArray, JNull, JValue}
import slick.jdbc.GetResult

import java.time.LocalDate
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.OppivelvollisuudestaVapautus

import scala.concurrent.duration.DurationInt

case class ValpasOppijaRow(
  oppijaOid: String,
  kaikkiOppijaOidit: Seq[ValpasHenkilö.Oid],
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kotikunta: Option[String],
  hakeutumisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid],
  suorittamisvalvovatOppilaitokset: Set[ValpasOppilaitos.Oid],
  opiskeluoikeudet: JValue,
  turvakielto: Boolean,
  äidinkieli: Option[String],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate,
  onOikeusValvoaMaksuttomuutta: Boolean,
  onOikeusValvoaKunnalla: Boolean,
  oppivelvollisuudestaVapautus: Option[OppivelvollisuudestaVapautus],
) {
  def vapautettuOppivelvollisuudesta: Boolean = oppivelvollisuudestaVapautus.exists(!_.tulevaisuudessa)
}

case class ValpasOppivelvollisuustiedotRow(
  oppijaOid: String,
  kaikkiOppijaOidit: Seq[ValpasHenkilö.Oid],
  hetu: Option[String],
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate
)

object HakeutumisvalvontaTieto extends Enumeration {
  type HakeutumisvalvontaTieto = Value
  val Perusopetus, Nivelvaihe, Kaikki = Value
}

class ValpasOpiskeluoikeusDatabaseService(application: KoskiApplication) extends DatabaseConverters with Logging with Timing {
  private val db = application.raportointiDatabase
  private val rajapäivätService = application.valpasRajapäivätService
  private val oppijanPoistoService = application.valpasOppivelvollisuudestaVapautusService

  def getOppija(
    oppijaOid: String,
    rajaaOVKelpoisiinOpiskeluoikeuksiin: Boolean,
    haeMyösOppivelvollisuudestaVapautettu: Boolean,
  ): Option[ValpasOppijaRow] = {
    val oppija = queryOppijat(List(oppijaOid), None, rajaaOVKelpoisiinOpiskeluoikeuksiin, HakeutumisvalvontaTieto.Kaikki).headOption
    if (haeMyösOppivelvollisuudestaVapautettu || !oppija.exists(_.vapautettuOppivelvollisuudesta)) oppija else None
  }

  def getOppijat(
    oppijaOids: Seq[String],
    rajaaOVKelpoisiinOpiskeluoikeuksiin: Boolean,
    haeMyösOppivelvollisuudestaVapautetut: Boolean,
  ): Seq[ValpasOppijaRow] =
    if (oppijaOids.nonEmpty) {
      val kaikkiOppijat = queryOppijat(oppijaOids, None, rajaaOVKelpoisiinOpiskeluoikeuksiin, HakeutumisvalvontaTieto.Kaikki)
      if (haeMyösOppivelvollisuudestaVapautetut) kaikkiOppijat else kaikkiOppijat.filterNot(_.vapautettuOppivelvollisuudesta)
    } else {
      Seq.empty
    }

  def getOppijatByOppilaitos(oppilaitosOid: String, hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value): Seq[ValpasOppijaRow] =
    queryOppijat(Seq.empty, Some(Seq(oppilaitosOid)), true, hakeutumisvalvontaTieto)
      .filterNot(_.vapautettuOppivelvollisuudesta)

  private implicit def getResult: GetResult[ValpasOppijaRow] = GetResult(r => {
    ValpasOppijaRow(
      oppijaOid = r.rs.getString("oppija_oid"),
      kaikkiOppijaOidit = r.getArray("kaikkiOppijaOidit").toSeq,
      hetu = Option(r.rs.getString("hetu")),
      syntymäaika = Option(r.getLocalDate("syntymaaika")),
      etunimet = r.rs.getString("etunimet"),
      sukunimi = r.rs.getString("sukunimi"),
      kotikunta = Option(r.rs.getString("kotikunta")),
      hakeutumisvalvovatOppilaitokset = r.getArray("hakeutumisvalvovatOppilaitokset").toSet,
      suorittamisvalvovatOppilaitokset = r.getArray("suorittamisvalvovatOppilaitokset").toSet,
      opiskeluoikeudet = r.getJson("opiskeluoikeudet"),
      turvakielto = r.rs.getBoolean("turvakielto"),
      äidinkieli = Option(r.rs.getString("aidinkieli")),
      oppivelvollisuusVoimassaAsti = r.getLocalDate("oppivelvollisuusVoimassaAsti"),
      oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = r.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti"),
      onOikeusValvoaMaksuttomuutta = r.rs.getBoolean("onOikeusValvoaMaksuttomuutta"),
      onOikeusValvoaKunnalla = r.rs.getBoolean("onOikeusValvoaKunnalla"),
      oppivelvollisuudestaVapautus = None,
    )
  })

  private def queryOppijat(
    oppijaOids: Seq[String],
    oppilaitosOids: Option[Seq[String]],
    rajaaOVKelpoisiinOpiskeluoikeuksiin: Boolean = true,
    hakeutumisvalvontaTieto: HakeutumisvalvontaTieto.Value
  ): Seq[ValpasOppijaRow] = {
    val keväänValmistumisjaksoAlku = rajapäivätService.keväänValmistumisjaksoAlku
    val keväänValmistumisjaksoLoppu = rajapäivätService.keväänValmistumisjaksoLoppu
    val keväänUlkopuolellaValmistumisjaksoAlku = rajapäivätService.keväänUlkopuolellaValmistumisjaksoAlku
    val tarkastelupäivä = rajapäivätService.tarkastelupäivä
    val keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä = rajapäivätService.keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
    val hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja = rajapäivätService.perusopetussuorituksenNäyttämisenAikaraja
    val (nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutAlku, nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutLoppu) =
      rajapäivätService.nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutAikaväli

    val timedBlockname = oppijaOids.size match {
      case 0 => "getOppijatMultiple"
      case 1 => "getOppijatSingle"
      case n if n <= 1000 => "getOppijatMultipleOids2To1000"
      case n if n <= 10000 => "getOppijatMultipleOids1001To10000"
      case n if n <= 50000 => "getOppijatMultipleOids10001To50000"
      case n if n <= 100000 => "getOppijatMultipleOids50000To100000"
      case _ => "getOppijatMultipleOidsOver100000"
    }

    val haePerusopetuksenHakeutumisvalvontatiedot = Seq(HakeutumisvalvontaTieto.Perusopetus, HakeutumisvalvontaTieto.Kaikki).contains(hakeutumisvalvontaTieto)
    val haeNivelvaiheenHakeutusmisvalvontatiedot = Seq(HakeutumisvalvontaTieto.Nivelvaihe, HakeutumisvalvontaTieto.Kaikki).contains(hakeutumisvalvontaTieto)

    val nonEmptyOppijaOids = if (oppijaOids.nonEmpty) Some(oppijaOids) else None

    val lokitettavatParametrit = Map(
      "rajaaOVKelpoisiinOpiskeluoikeuksiin" -> rajaaOVKelpoisiinOpiskeluoikeuksiin.toString,
      "keväänValmistumisjaksoAlku" -> keväänValmistumisjaksoAlku.toString,
      "keväänValmistumisjaksoLoppu" -> keväänValmistumisjaksoLoppu.toString,
      "keväänUlkopuolellaValmistumisjaksoAlku" -> keväänUlkopuolellaValmistumisjaksoAlku.toString,
      "tarkastelupäivä" -> tarkastelupäivä.toString,
      "keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä" -> keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä.toString,
      "hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja" -> hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja.toString,
      "nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutAlku" -> nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutAlku.toString,
      "nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutLoppu" -> nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutLoppu.toString,
      "haePerusopetuksenHakeutumisvalvontatiedot" -> haePerusopetuksenHakeutumisvalvontatiedot.toString,
      "haeNivelvaiheenHakeutusmisvalvontatiedot" -> haeNivelvaiheenHakeutusmisvalvontatiedot.toString
    )

    logger.info("queryOppijat SQL-parametrit: " + lokitettavatParametrit.toString)

    val result = timed(timedBlockname, 10) {
      db.runDbSync(timeout = 3.minutes, a = SQLHelpers.concatMany(
        Some(
          sql"""
  WITH
      """),
        nonEmptyOppijaOids.map(oids =>
          sql"""
  -- ********************************************************************************************************
  -- CTE: OPPIJA
  --      jos pyydettiin vain yhtä oppijaa, hae hänen master oid:nsa
  -- ********************************************************************************************************
  pyydetty_oppija AS (
    SELECT r_henkilo.master_oid
    FROM r_henkilo
    WHERE r_henkilo.oppija_oid = any($oids)
  ),
      """),
        Some(
          sql"""
  -- ********************************************************************************************************
  -- CTE: KAIKKI OPISKELUOIKEUDET
  --      jotka ovat oppivelvollisuuden suorittamiseen kelpaavia
  -- ********************************************************************************************************
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
  -- ********************************************************************************************************
  -- CTE: PERUSOPETUS HAKEUTUMISVALVOTTAVAT
  -- ********************************************************************************************************
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
        -- 5a.1 vasta syksyllä (1.8. tai myöhemmin) 9. luokan aloittavia ei näytetä ennen kevään viimeistä rajapäivää.
        -- 17v täyttävät läsnä/väliaikaisesti keskeytynyt- tilassa olevat oppijat näkyvät perusopetuksen hakeutumisvelvollisten listanäkymässä 30.9. jälkeenkin
        -- Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}')
          AND (
            r_paatason_suoritus.data ->> 'alkamispäivä' <= $keväänValmistumisjaksoLoppu
            OR $tarkastelupäivä > $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
            OR ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
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
        -- TAI oppija täyttää 17v, on valmistunut/eronnut kevätlukukauden ulkopuolella ja korkeintaan 2 kk sitten
        OR (
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
          AND ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
          AND (ov_kelvollinen_opiskeluoikeus.paattymispaiva
            NOT BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
          AND ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{valmistunut, hyvaksytystisuoritettu, eronnut, katsotaaneronneeksi, keskeytynyt}')
          AND ($tarkastelupäivä <= ov_kelvollinen_opiskeluoikeus.paattymispaiva + interval '2 months')
        )
      )
  )
  -- ********************************************************************************************************
  -- CTE: PERUSKOULUUN VALMISTAVA HAKEUTUMISVALVOTTAVAT
  -- ********************************************************************************************************
  , hakeutumisvalvottava_peruskouluun_valmistava_opiskeluoikeus AS (
    SELECT
      DISTINCT ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid,
      ov_kelvollinen_opiskeluoikeus.oppilaitos_oid,
      ov_kelvollinen_opiskeluoikeus.master_oid
    FROM
      ov_kelvollinen_opiskeluoikeus
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
    WHERE
      $haePerusopetuksenHakeutumisvalvontatiedot IS TRUE
      -- (0) henkilö on oppivelvollinen: hakeutumisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      AND ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- (1) oppijalla on peruskouluun valmistava opiskeluoikeus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'perusopetukseenvalmistavaopetus'
      -- (2) oppija täyttää 17 vuotta tänä vuonna. Peruskouluun valmistavassa vain heitä hakeutumisvalvotaan.
      AND ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
      AND (
        -- (5a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tai lomalla tällä hetkellä.
        -- Huomaa, että tulevaisuuteen luotuja opiskeluoikeuksia ei tarkoituksella haluta näkyviin.
        (
          (aikajakson_keskella.tila IS NOT NULL
            AND aikajakson_keskella.tila = any('{lasna, valiaikaisestikeskeytynyt, loma}'))
        )
        -- TAI:
        OR (
          -- (5b.1) opiskeluoikeus on päättynyt menneisyydessä
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
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
  -- ********************************************************************************************************
  -- CTE: INTERNATIONAL SCHOOL HAKEUTUMISVALVOTTAVAT
  -- ********************************************************************************************************
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
  -- ********************************************************************************************************
  -- CTE: EUROPEAN SCHOOL OF HELSINKI HAKEUTUMISVALVOTTAVAT
  -- ********************************************************************************************************
  , hakeutumisvalvottava_esh_opiskeluoikeus AS (
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
      LEFT JOIN LATERAL (
            SELECT
              TRUE AS loytyi
            FROM
              r_paatason_suoritus pts
            WHERE
              pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
              AND (
                pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
                OR (
                  pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
                  AND pts.koulutusmoduuli_koodiarvo = 'S5'
                  AND (
                    pts.alkamispaiva < $keväänValmistumisjaksoAlku
                    OR $tarkastelupäivä > $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
                  )
                )
              )
            LIMIT 1
          ) AS esh_perusopetuksen_jalkeisia_suorituksia ON TRUE
    WHERE
      $haePerusopetuksenHakeutumisvalvontatiedot IS TRUE
      -- (0) henkilö on oppivelvollinen: hakeutumisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      AND ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- (1) oppijalla on European School of Helsinki -opiskeluoikeus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'europeanschoolofhelsinki'
      AND (
        -- (B2.1) kyseisessä opiskeluoikeudessa on S4-luokan suoritus.
        (
          r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'S4'
        )
        -- (B2.2) TAI oppija täyttää vähintään 17 vuotta tarkasteluvuonna ja hänellä on jokin ESH:n
        -- perusopetuksen päätason suoritus: heidät näytetään luokka-asteesta
        -- riippumatta, koska voivat lopettaa ESH:n ja siirtyä seuraavaan opetukseen, vaikka
        -- olisivat esim. vasta 8. luokalla
        OR (
          ov_kelvollinen_opiskeluoikeus.henkilo_tayttaa_vahintaan_17_tarkasteluvuonna
          AND (
            r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'S3'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'S2'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'S1'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'P5'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'P4'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'P3'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'P2'
            OR r_paatason_suoritus.koulutusmoduuli_koodiarvo = 'P1'
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
          -- B5a.1 vasta syksyllä (1.8. tai myöhemmin) 9. luokan (ESH:n S4) aloittavia ei näytetä ennen kevään viimeistä rajapäivää.
          AND (
            r_paatason_suoritus.data ->> 'alkamispäivä' <= $keväänValmistumisjaksoLoppu
            OR $tarkastelupäivä > $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
          )
        )
        -- TAI:
        OR (
          -- (B5b.1) 9. (ESH:ssa S4) luokka on tullut suoritetetuksi menneisyydessä
          (
            (
              r_paatason_suoritus.vahvistus_paiva IS NOT NULL
              AND $tarkastelupäivä >= r_paatason_suoritus.vahvistus_paiva
              -- (B5b.2) ministeriön määrittelemä aikaraja ei ole kulunut umpeen henkilön 9. (ESH:ssa S4) luokan suorittamisajasta.
              AND (
                (
                  -- keväällä 9. luokan suorittanut ja tarkastellaan heille määrättyä rajapäivää aiemmin
                  (r_paatason_suoritus.vahvistus_paiva
                    BETWEEN $keväänValmistumisjaksoAlku AND $keväänValmistumisjaksoLoppu)
                  AND $tarkastelupäivä <= $keväänValmistumisjaksollaValmistuneidenViimeinenTarkastelupäivä
                )
                OR (
                  -- tai muuna aikana 9. (ESH:ssa S4) luokan suorittanut ja tarkastellaan heille määrättyä rajapäivää aiemmin
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
      -- Ja oppilas ei jatka opiskelua ESH:ssä
      AND esh_perusopetuksen_jalkeisia_suorituksia.loytyi IS NULL
  )
  -- ********************************************************************************************************
  -- CTE: NIVELVAIHE HAKEUTUMISVALVOTTAVAT
  -- ********************************************************************************************************
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
        OR ov_kelvollinen_opiskeluoikeus.koulutusmuoto = 'tuva'
      )
      AND (
        -- opiskeluoikeus ei ole peruutettu tai keskeytynyt tällä hetkellä
        (aikajakson_keskella.tila IS NOT NULL
          AND NOT aikajakson_keskella.tila = any('{peruutettu, keskeytynyt}'))
        OR (aikajakson_keskella.tila IS NULL
          AND $tarkastelupäivä < ov_kelvollinen_opiskeluoikeus.alkamispaiva)
        OR (aikajakson_keskella.tila IS NULL
          AND $tarkastelupäivä > ov_kelvollinen_opiskeluoikeus.paattymispaiva
          AND NOT ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{peruutettu, keskeytynyt}'))
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
        -- TAI: opiskeluoikeus on päättynyt tilaan 'eronnut' tai 'katsotaan eronneeksi' ministeriön määrittelemän aikajakson sisällä
        OR (
          ov_kelvollinen_opiskeluoikeus.viimeisin_tila = any('{eronnut, katsotaaneronneeksi}')
          AND (ov_kelvollinen_opiskeluoikeus.paattymispaiva
            BETWEEN $nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutAlku
                AND $nivelvaiheenOppilaitokselleHakeutumisvalvottavaJosOppijaEronnutLoppu
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
  -- ********************************************************************************************************
  -- CTE: HAKEUTUMISVALVOTTAVAT YHDESSÄ
  -- ********************************************************************************************************
  , hakeutumisvalvottava_opiskeluoikeus AS (
    SELECT
      *
    FROM
      hakeutumisvalvottava_peruskoulun_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
      hakeutumisvalvottava_peruskouluun_valmistava_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
      hakeutumisvalvottava_international_schoolin_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
      hakeutumisvalvottava_esh_opiskeluoikeus
    UNION ALL
    SELECT
      *
    FROM
      hakeutumisvalvottava_nivelvaiheen_opiskeluoikeus
  )
  -- ********************************************************************************************************
  -- CTE: SUORITTAMISVALVOTTAVAT
  -- ********************************************************************************************************
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
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
          AND pts.suorituksen_tyyppi = 'ammatillinentutkintoosittainen'
        LIMIT 1
      ) AS ammatillisen_tutkinnon_osittaisia_suorituksia ON TRUE
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
          AND (
            pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
            OR (
              pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
              AND pts.koulutusmoduuli_koodiarvo = 'S5'
            )
          )
        LIMIT 1
      ) AS esh_perusopetuksen_jalkeisia_suorituksia ON TRUE
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
          AND pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
          AND pts.koulutusmoduuli_koodiarvo = 'S5'
        LIMIT 1
      ) AS esh_s5_suorituksia ON TRUE
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = ov_kelvollinen_opiskeluoikeus.opiskeluoikeus_oid
          AND pts.suorituksen_tyyppi = 'lukionoppimaara'
        LIMIT 1
      ) AS lukio_oppimaaran_suorituksia ON TRUE
    WHERE
      -- (0) henkilö on oppivelvollinen: suorittamisvalvontaa ei voi suorittaa enää sen jälkeen kun henkilön
      -- oppivelvollisuus on päättynyt
      ov_kelvollinen_opiskeluoikeus.henkilo_on_oppivelvollinen
      -- (1) oppijalla on muu kuin peruskoulun opetusoikeus, esiopetus tai perusopetukseen valmistava opetus
      AND ov_kelvollinen_opiskeluoikeus.koulutusmuoto NOT IN ('perusopetus', 'esiopetus', 'perusopetukseenvalmistavaopetus')
      -- (1b) International school on suorittamisvalvottava vain, jos siinä on 10, 11 tai 12 luokan suoritus
      AND (
        ov_kelvollinen_opiskeluoikeus.koulutusmuoto <> 'internationalschool'
        OR international_school_toisen_asteen_suorituksia.loytyi IS TRUE
      )
      -- (1b) European School of Helsinki on suorittamisvalvottava vain, jos siinä on S5-S7 suoritus
      AND (
        ov_kelvollinen_opiskeluoikeus.koulutusmuoto <> 'europeanschoolofhelsinki'
        OR esh_perusopetuksen_jalkeisia_suorituksia.loytyi IS TRUE
      )
      -- (1c) Lukiossa vain oppimäärän suoritukset ovat suorittamisvalvottavia
      AND (
        ov_kelvollinen_opiskeluoikeus.koulutusmuoto <> 'lukiokoulutus'
        OR lukio_oppimaaran_suorituksia.loytyi IS TRUE
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
        -- TAI
        OR (
          -- (2b.4) Oppija on valmistunut ammatillisen osittaisesta tutkinnosta
          ($tarkastelupäivä >= ov_kelvollinen_opiskeluoikeus.paattymispaiva)
          AND (ov_kelvollinen_opiskeluoikeus.viimeisin_tila = 'valmistunut')
          AND (ammatillisen_tutkinnon_osittaisia_suorituksia.loytyi IS TRUE)
        )
      )
  )
  -- ********************************************************************************************************
  -- CTE: PALAUTETTAVAT OPPIJAT
  --      kaikki uuden lain piirissä olevat oppijat, joilla on vähintään yksi oppivelvollisuuden suorittamiseen
  --      kelpaava opiskeluoikeus. Mukana myös taulukko hakeutumisvalvontaan ja suoritusvalvontaan
  --      kelpuutettavien opiskeluoikeuksien oppilaitoksista käyttöoikeustarkastelua varten.
  -- ********************************************************************************************************
  , oppija AS (
    SELECT
      DISTINCT r_henkilo.master_oid,
      r_henkilo.syntymaaika,
      r_henkilo.etunimet,
      r_henkilo.sukunimi,
      r_henkilo.kotikunta,
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
      -- Tässä haetaan oppijan slave-oidit muista tietokannoista tehtäviä kyselyitä varten. Tämä ei ole täydellinen lista:
      -- käsittelemättä jäävät oidit, jotka ovat vain masterin linkitetyt_oidit sarakkeessa. Tämä ei kuitenkaan haittaa, koska
      -- Valppaassa ei tällä hetkellä ole mahdollista luoda keskeytyksiä tai kuntailmoituksia kuin hetullisille oppijoille, ja
      -- hetulliset oppijat eivät koskaan voi olla slave-oppijoita. Käytännössä koko käsittelyn voisi oletettavasti purkaa, mutta
      -- ennen kuin sen tekee, pitäisi tarkistaa tietokannoista, ettei tällaisia tapauksia ole. Valpas kuitenkin näytti n. syyskuuhun
      -- 2021 asti myös hetuttomia oppijoita.
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
      r_henkilo.kotikunta,
      r_henkilo.turvakielto,
      r_henkilo.aidinkieli,
      oppivelvollisuustiedot.oppivelvollisuusvoimassaasti,
      oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti
  )
  -- ********************************************************************************************************
  -- CTE: OPPIJOIDEN KAIKKI OPPIJA_OIDIT
  --      Oppijoiden opiskeluoikeudet pitää hakea myös heidän muilla oppija-oideillaan
  -- ********************************************************************************************************
  , oppija_oid AS (
	SELECT
	  DISTINCT oppija_oid,
	  oppija.master_oid
	FROM
	  r_henkilo
	  JOIN oppija ON oppija.master_oid = r_henkilo.master_oid
  )
  -- ********************************************************************************************************
  -- CTE: TILAT
  --      Tilamäppäykset Kosken tarkkojen ja Valppaan yksinkertaisempien tilojen välillä
  -- ********************************************************************************************************
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
  -- ********************************************************************************************************
  -- CTE: PERUSOPETUS
  --      peruskoulun opiskeluoikeudet. Ei sama lista kuin aiemmassa CTE:ssä, koska voi olla rinnakkaisia tai
  --      peräkkäisiä muita peruskoulun opiskeluoikeuksia. Teoriassa varmaan voisi tehostaa kyselyä jotenkin ja välttää
  --      näiden hakeminen uudestaan, mutta kysely voisi mennä melko monimutkaiseksi.
  -- ********************************************************************************************************
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
      NULL::jsonb AS muu_kuin_perusopetus_tiedot,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'maksuttomuus' AS maksuttomuus,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'oikeuttaMaksuttomuuteenPidennetty' AS oikeutta_maksuttomuuteen_pidennetty
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
          pts.data->>'alkamispäivä' DESC NULLS LAST
        LIMIT 1
      ) AS valittu_paatason_suoritus
  )
  -- ********************************************************************************************************
  -- CTE: INTERNATIONAL SCHOOL
  --      Pitää hakea erikseen, koska sisältää sekä perusopetuksen että sen jälkeisen koulutuksen tietoja
  -- ********************************************************************************************************
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
      ) AS muu_kuin_perusopetus_tiedot,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'maksuttomuus' AS maksuttomuus,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'oikeuttaMaksuttomuuteenPidennetty' AS oikeutta_maksuttomuuteen_pidennetty
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
  -- ********************************************************************************************************
  -- CTE: EUROPEAN SCHOOL OF HELSINKI
  --      Pitää hakea erikseen, koska sisältää sekä perusopetuksen että sen jälkeisen koulutuksen tietoja
  -- ********************************************************************************************************
  , esh_opiskeluoikeus AS (
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
                WHEN s4suoritus.vahvistus_paiva IS NOT NULL THEN s4suoritus.vahvistus_paiva
                WHEN toisen_asteen_suorituksia.lukumaara > 0 THEN NULL::date
                ELSE r_opiskeluoikeus.paattymispaiva
              END,
            'päättymispäiväMerkittyTulevaisuuteen', (
              CASE
                WHEN s4suoritus.vahvistus_paiva IS NOT NULL THEN s4suoritus.vahvistus_paiva
                WHEN toisen_asteen_suorituksia.lukumaara > 0 THEN NULL::date
                ELSE r_opiskeluoikeus.paattymispaiva
              END) > $tarkastelupäivä,
            'tarkastelupäivänTila', jsonb_build_object(
              'koodiarvo',
                (CASE
                  WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN 'voimassatulevaisuudessa'
                  -- Jos S5 on vahvistettu ja tutkitaan vahvistuspäivämäärän jälkeen, käytetään tilaa valmistunut:
                  WHEN s4suoritus.vahvistus_paiva IS NOT NULL AND s4suoritus.vahvistus_paiva <= $tarkastelupäivä THEN 'valmistunut'
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
                  -- Jos S5 on vahvistettu ja tutkitaan vahvistuspäivämäärän jälkeen, käytetään tilaa valmistunut:
                  WHEN s4suoritus.vahvistus_paiva IS NOT NULL AND s4suoritus.vahvistus_paiva <= $tarkastelupäivä THEN 'valmistunut'
                  -- Muuten käytetään opiskeluoikeuden tietoja:
                  WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.viimeisin_tila
                  ELSE aikajakson_keskella.tila
                END),
              'koodistoUri', 'koskiopiskeluoikeudentila'
            ),
            'tarkastelupäivänKoskiTilanAlkamispäivä',
              (CASE
                WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN r_opiskeluoikeus.alkamispaiva
                -- Jos S5 on vahvistettu ja tutkitaan vahvistuspäivämäärän jälkeen, käytetään vahvistuspäivää:
                WHEN (s4suoritus.vahvistus_paiva IS NOT NULL AND s4suoritus.vahvistus_paiva <= $tarkastelupäivä) THEN s4suoritus.vahvistus_paiva
                -- Muuten käytetään opiskeluoikeuden tietoja:
                WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.paattymispaiva
                ELSE aikajakson_keskella.tila_alkanut
              END),
            'valmistunutAiemminTaiLähitulevaisuudessa', (
              s4suoritus.vahvistus_paiva IS NOT NULL
              AND s4suoritus.vahvistus_paiva < $hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja
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
        -- Tarvitaan, jotta voidaan näyttää ESH:ssa itsessään käytävät jatko-opinnot oikein hakeutumisvalvonnan näkymässä ainoastaan
        -- silloin, kun niissä on oikeasti kirjattuja 10+ luokan suorituksia
        'näytäMuunaPerusopetuksenJälkeisenäOpintona', (toisen_asteen_suorituksia.lukumaara > 0)
      ) AS muu_kuin_perusopetus_tiedot,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'maksuttomuus' AS maksuttomuus,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'oikeuttaMaksuttomuuteenPidennetty' AS oikeutta_maksuttomuuteen_pidennetty
    FROM
      oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
        AND r_opiskeluoikeus.koulutusmuoto = 'europeanschoolofhelsinki'
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
          AND pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
          AND pts.koulutusmoduuli_koodiarvo = 'S4'
      ) AS s4suoritus ON TRUE
      LEFT JOIN LATERAL (
        SELECT
          TRUE AS loytyi
        FROM
          r_paatason_suoritus pts
        WHERE
          pts.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          AND (
            (pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkanursery')
            OR (pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkaprimary')
            OR (
              pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
              AND (pts.koulutusmoduuli_koodiarvo = 'S1'
                OR pts.koulutusmoduuli_koodiarvo = 'S2'
                OR pts.koulutusmoduuli_koodiarvo = 'S3'
                OR pts.koulutusmoduuli_koodiarvo = 'S4'
                -- OR pts.koulutusmoduuli_koodiarvo = 'S5'
              )
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
          AND pts.suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
      ) AS toisen_asteen_suorituksia
  )
  -- ********************************************************************************************************
  -- CTE: VALMISTUNEET YO-TUTKINNOT
  --      Pitää hakea erikseen, koska tilatiedot ovat erilaiset
  -- ********************************************************************************************************
  , yo_opiskeluoikeus AS (
      SELECT
        oppija_oid.master_oid,
        r_opiskeluoikeus.opiskeluoikeus_oid,
        r_opiskeluoikeus.koulutusmuoto,
        r_opiskeluoikeus.oppilaitos_oid,
        r_opiskeluoikeus.oppilaitos_nimi,
        TRUE as oppivelvollisuuden_suorittamiseen_kelpaava,
        kaikki_paatason_suoritukset.data AS paatason_suoritukset,
        r_paatason_suoritus.vahvistus_paiva as alkamispaiva, -- YO-tutkinnoilla ei ole mitään alkamispäivää
        r_paatason_suoritus.vahvistus_paiva as paattymispaiva,
        'valmistunut' AS tarkastelupaivan_tila,
        NULL::jsonb AS perusopetus_tiedot,
        jsonb_build_object(
          'alkamispäivä', r_paatason_suoritus.vahvistus_paiva, -- YO-tutkinnoilla ei ole mitään alkamispäivää
          'päättymispäivä', r_paatason_suoritus.vahvistus_paiva,
          'päättymispäiväMerkittyTulevaisuuteen', FALSE,
          'tarkastelupäivänTila', jsonb_build_object(
            'koodiarvo', 'valmistunut',
            'koodistoUri', 'valpasopiskeluoikeudentila'
          ),
          'tarkastelupäivänKoskiTila', jsonb_build_object(
            'koodiarvo', 'valmistunut',
            'koodistoUri', 'koskiopiskeluoikeudentila'
          ),
          'tarkastelupäivänKoskiTilanAlkamispäivä', r_paatason_suoritus.vahvistus_paiva,
          'valmistunutAiemminTaiLähitulevaisuudessa', TRUE,
          'näytäMuunaPerusopetuksenJälkeisenäOpintona', TRUE
        ) AS muu_kuin_perusopetus_tiedot,
        NULL::jsonb AS maksuttomuus,
        NULL::jsonb AS oikeutta_maksuttomuuteen_pidennetty
      FROM
        oppija_oid
        JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
          AND r_opiskeluoikeus.koulutusmuoto = 'ylioppilastutkinto'
        JOIN r_paatason_suoritus ON r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          AND r_paatason_suoritus.vahvistus_paiva IS NOT NULL
          AND r_paatason_suoritus.vahvistus_paiva <= $tarkastelupäivä
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
              'ryhmä', NULL::jsonb
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
  -- ********************************************************************************************************
  -- CTE: MUUT KUIN PERUSOPETUS/ISH/ESH/YO
  --      Pitää hakea erikseen, koska säännöt päätason suorituksen kenttien osalta ovat erilaiset, koska datat ovat erilaiset
  -- ********************************************************************************************************
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
        ELSE COALESCE(valpastila_aikajakson_keskella.valpasopiskeluoikeudentila, valpastila_esiopetus_aikajakson_keskella.valpasopiskeluoikeudentila)
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
              ELSE COALESCE(valpastila_aikajakson_keskella.valpasopiskeluoikeudentila, valpastila_esiopetus_aikajakson_keskella.valpasopiskeluoikeudentila)
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
              ELSE COALESCE(aikajakson_keskella.tila, esiopetus_aikajakson_keskella.tila)
            END),
          'koodistoUri', 'koskiopiskeluoikeudentila'
        ),
        'tarkastelupäivänKoskiTilanAlkamispäivä', (CASE
          WHEN $tarkastelupäivä < r_opiskeluoikeus.alkamispaiva THEN r_opiskeluoikeus.alkamispaiva
          WHEN $tarkastelupäivä > r_opiskeluoikeus.paattymispaiva THEN r_opiskeluoikeus.paattymispaiva
          ELSE COALESCE(aikajakson_keskella.tila_alkanut, esiopetus_aikajakson_keskella.tila_alkanut)
        END),
        'valmistunutAiemminTaiLähitulevaisuudessa', (
          r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
          AND coalesce(r_opiskeluoikeus.paattymispaiva < $hakeutusmivalvottavanSuorituksenNäyttämisenAikaraja, FALSE)
        ),
        'näytäMuunaPerusopetuksenJälkeisenäOpintona', TRUE
      ) AS muu_kuin_perusopetus_tiedot,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'maksuttomuus' AS maksuttomuus,
      r_opiskeluoikeus.data -> 'lisätiedot' -> 'oikeuttaMaksuttomuuteenPidennetty' AS oikeutta_maksuttomuuteen_pidennetty
    FROM
      oppija_oid
      JOIN r_opiskeluoikeus ON r_opiskeluoikeus.oppija_oid = oppija_oid.oppija_oid
        AND r_opiskeluoikeus.koulutusmuoto <> 'perusopetus'
        AND r_opiskeluoikeus.koulutusmuoto <> 'internationalschool'
        AND r_opiskeluoikeus.koulutusmuoto <> 'ylioppilastutkinto'
        AND r_opiskeluoikeus.koulutusmuoto <> 'europeanschoolofhelsinki'
      LEFT JOIN r_opiskeluoikeus_aikajakso aikajakson_keskella
        ON aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN aikajakson_keskella.alku AND aikajakson_keskella.loppu
      LEFT JOIN valpastila valpastila_aikajakson_keskella
        ON valpastila_aikajakson_keskella.koskiopiskeluoikeudentila = aikajakson_keskella.tila
      LEFT JOIN valpastila valpastila_viimeisin
        ON valpastila_viimeisin.koskiopiskeluoikeudentila = r_opiskeluoikeus.viimeisin_tila
      LEFT JOIN esiopetus_opiskeluoik_aikajakso esiopetus_aikajakson_keskella
        ON esiopetus_aikajakson_keskella.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        AND $tarkastelupäivä BETWEEN esiopetus_aikajakson_keskella.alku AND esiopetus_aikajakson_keskella.loppu
      LEFT JOIN valpastila valpastila_esiopetus_aikajakson_keskella
        ON valpastila_esiopetus_aikajakson_keskella.koskiopiskeluoikeudentila = esiopetus_aikajakson_keskella.tila
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
  -- ********************************************************************************************************
  -- CTE: KAIKKI PALAUTETTAVAT OPISKELUOIKEUDET YHDISTETTYNÄ
  -- ********************************************************************************************************
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
      esh_opiskeluoikeus
    UNION ALL
    SELECT
        *
      FROM
        yo_opiskeluoikeus
      UNION ALL
    SELECT
      *
    FROM
     muu_opiskeluoikeus
  )
  -- ********************************************************************************************************
  -- PÄÄTASON SELECT
  --     Muodostetaan palautettava rakenne CTE:istä
  -- ********************************************************************************************************
  SELECT
    oppija.master_oid AS oppija_oid,
    oppija.kaikkiOppijaOidit,
    oppija.hetu,
    oppija.syntymaaika,
    oppija.etunimet,
    oppija.sukunimi,
    oppija.kotikunta,
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
        'perusopetuksenJälkeinenTiedot', (CASE
          WHEN opiskeluoikeus.koulutusmuoto NOT IN ('esiopetus', 'perusopetukseenvalmistavaopetus') THEN opiskeluoikeus.muu_kuin_perusopetus_tiedot
          ELSE NULL::jsonb END),
        'muuOpetusTiedot', (CASE
          WHEN opiskeluoikeus.koulutusmuoto IN ('esiopetus', 'perusopetukseenvalmistavaopetus') THEN opiskeluoikeus.muu_kuin_perusopetus_tiedot
          ELSE NULL::jsonb END),
        'maksuttomuus', opiskeluoikeus.maksuttomuus,
        'oikeuttaMaksuttomuuteenPidennetty', opiskeluoikeus.oikeutta_maksuttomuuteen_pidennetty
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
    oppija.kotikunta,
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

    oppijanPoistoService.mapVapautetutOppijat(result, (r: ValpasOppijaRow) => r.kaikkiOppijaOidit) {
      case (oppija, vapautus) => {
        oppija.copy(
          oppivelvollisuusVoimassaAsti = if (vapautus.mitätöitymässä) {
            oppija.oppivelvollisuusVoimassaAsti
          } else {
            Seq(oppija.oppivelvollisuusVoimassaAsti, vapautus.oppivelvollisuusVoimassaAsti).min
          },
          oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = if (vapautus.mitätöitymässä) {
            oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
          } else {
            Seq(oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti, vapautus.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti).min
          },
          oppivelvollisuudestaVapautus = Some(vapautus),
          onOikeusValvoaKunnalla = true,
          onOikeusValvoaMaksuttomuutta = true,
        )
      }
    }
  }

  def getOppivelvollisuusTiedot(hetut: Seq[String]): Seq[ValpasOppivelvollisuustiedotRow] = {

    implicit def getResult: GetResult[ValpasOppivelvollisuustiedotRow] = GetResult(r => {
      ValpasOppivelvollisuustiedotRow(
        oppijaOid = r.rs.getString("master_oid"),
        kaikkiOppijaOidit = r.getArray("kaikkiOppijaOidit").toSeq,
        hetu = Some(r.rs.getString("hetu")),
        oppivelvollisuusVoimassaAsti = r.getLocalDate("oppivelvollisuusvoimassaasti"),
        oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = r.getLocalDate("oikeuskoulutuksenmaksuttomuuteenvoimassaasti"),
      )
    })

    val result = db.runDbSync(SQLHelpers.concatMany(
      Some(
        sql"""
  WITH
    pyydetty_oppija AS (
      SELECT
        DISTINCT r_henkilo.master_oid,
        r_henkilo.hetu
      FROM
        r_henkilo
      WHERE
        r_henkilo.hetu = any($hetut)
    )
  SELECT
    pyydetty_oppija.master_oid,
    pyydetty_oppija.hetu,
    oppivelvollisuustiedot.oppivelvollisuusvoimassaasti,
    oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti,
    -- Tässä haetaan oppijan slave-oidit muista tietokannoista tehtäviä kyselyitä varten. Tämä ei ole täydellinen lista:
    -- käsittelemättä jäävät oidit, jotka ovat vain masterin linkitetyt_oidit sarakkeessa. Tämä ei kuitenkaan haittaa, koska
    -- Valppaassa ei tällä hetkellä ole mahdollista luoda keskeytyksiä tai kuntailmoituksia kuin hetullisille oppijoille, ja
    -- hetulliset oppijat eivät koskaan voi olla slave-oppijoita. Käytännössä koko käsittelyn voisi oletettavasti purkaa, mutta
    -- ennen kuin sen tekee, pitäisi tarkistaa tietokannoista, ettei tällaisia tapauksia ole. Valpas kuitenkin näytti n. syyskuuhun
    -- 2021 asti myös hetuttomia oppijoita.
    array_agg(DISTINCT kaikki_henkilot.oppija_oid) AS kaikkiOppijaOidit
  FROM
    oppivelvollisuustiedot
    JOIN
      pyydetty_oppija ON pyydetty_oppija.master_oid = oppivelvollisuustiedot.oppija_oid
    JOIN
      r_henkilo kaikki_henkilot ON kaikki_henkilot.master_oid = pyydetty_oppija.master_oid
  GROUP BY
    pyydetty_oppija.master_oid,
    pyydetty_oppija.hetu,
    oppivelvollisuustiedot.oppivelvollisuusvoimassaasti,
    oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti
        """
      )
    ).as[ValpasOppivelvollisuustiedotRow])

    oppijanPoistoService.mapVapautetutOppijat(result, (r: ValpasOppivelvollisuustiedotRow) => r.kaikkiOppijaOidit) {
      case (oppija, vapautus) => oppija.copy(
        oppivelvollisuusVoimassaAsti = if (vapautus.mitätöitymässä) {
          oppija.oppivelvollisuusVoimassaAsti
        } else {
          Seq(oppija.oppivelvollisuusVoimassaAsti, vapautus.oppivelvollisuusVoimassaAsti).min
        },
        oikeusKoulutuksenMaksuttomuuteenVoimassaAsti = if (vapautus.mitätöitymässä) {
          oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
        } else {
          Seq(oppija.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti, vapautus.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti).min
        },
      )
    }
  }

  def haeOppijatHetuilla(hetut: Seq[String]): Seq[HetuMasterOid] = {
    db.runDbSync(sql"""
      SELECT
        r_henkilo.master_oid as master_oid,
        r_henkilo.hetu as hetu,
        r_henkilo.syntymaaika as syntymaaika,
        COALESCE(${rajapäivätService.tarkastelupäivä} BETWEEN oppivelvollisuustiedot.oppivelvollisuusvoimassaalkaen AND oppivelvollisuustiedot.oppivelvollisuusvoimassaasti, false) as oppivelvollisuus_voimassa
      FROM r_henkilo
      LEFT JOIN oppivelvollisuustiedot ON (r_henkilo.master_oid = oppivelvollisuustiedot.oppija_oid)
      WHERE
        r_henkilo.hetu = any($hetut)
      """.as[HetuMasterOid])
  }

  def haeOppivelvollisetKotikunnalla(kunta: String): Seq[HetuMasterOid] = {
    val tarkastelupäivä = rajapäivätService.tarkastelupäivä

    db.runDbSync(sql"""
      SELECT
        r_henkilo.master_oid AS master_oid,
        r_henkilo.hetu AS hetu,
        r_henkilo.syntymaaika AS syntymaaika,
        TRUE as oppivelvollisuus_voimassa
      FROM
        r_henkilo
        JOIN oppivelvollisuustiedot
          ON (r_henkilo.master_oid = oppivelvollisuustiedot.oppija_oid)
            AND ($tarkastelupäivä BETWEEN oppivelvollisuustiedot.oppivelvollisuusvoimassaalkaen AND oppivelvollisuustiedot.oppivelvollisuusvoimassaasti)
      WHERE
        r_henkilo.kotikunta = $kunta
      """.as[HetuMasterOid])
  }


  def haeTunnettujenOppijoidenOidit(oppijaOidit: Seq[ValpasHenkilö.Oid]): Seq[OidResult] = {
    db.runDbSync(sql"""
      SELECT
        r_henkilo.oppija_oid AS oppijaOid
      FROM r_henkilo
      WHERE
        r_henkilo.oppija_oid = any($oppijaOidit)
      """.as[OidResult])
  }
}

case class HetuMasterOid(
  hetu: String,
  syntymäaika: Option[LocalDate],
  masterOid: ValpasHenkilö.Oid,
  oppivelvollisuusVoimassa: Boolean,
)

case class OidResult(
  oppijaOid: ValpasHenkilö.Oid
)

object OidResult {
  implicit def getReslt: GetResult[OidResult] = GetResult(r => {
    OidResult(
      oppijaOid = r.rs.getString("oppijaOid")
    )
  })
}

object HetuMasterOid {
  implicit def getResult: GetResult[HetuMasterOid] = GetResult(r => {
    HetuMasterOid(
      hetu = r.rs.getString("hetu"),
      syntymäaika = r.getLocalDateOption("syntymaaika"),
      masterOid = r.rs.getString("master_oid"),
      oppivelvollisuusVoimassa = r.rs.getBoolean("oppivelvollisuus_voimassa"),
    )
  })
}

