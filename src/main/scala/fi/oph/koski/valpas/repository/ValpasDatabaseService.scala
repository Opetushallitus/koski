package fi.oph.koski.valpas.repository

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.SQLHelpers
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.ROpiskeluoikeusRow
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema.{RHenkilöTable, ROpiskeluoikeusTable}
import slick.jdbc.GetResult

import java.sql.ResultSet

class ValpasDatabaseService(application: KoskiApplication) extends Logging {
  val db = application.raportointiDatabase
  lazy val RHenkilöt = TableQuery[RHenkilöTable]
  lazy val ROpiskeluoikeudet = TableQuery[ROpiskeluoikeusTable]

  def getPeruskoulunValvojalleNäkyväOppija(oppijaOid: String, oppilaitosOids: Option[Seq[String]]): Option[ValpasOppija] =
    getOppijat(Some(oppijaOid), oppilaitosOids).headOption

  def getPeruskoulunValvojalleNäkyvätOppijat(oppilaitosOids: Option[Seq[String]]): Seq[ValpasOppija] =
    getOppijat(None, oppilaitosOids)

  private def getOppijat(oppijaOid: Option[String], oppilaitosOids: Option[Seq[String]]): Seq[ValpasOppija] =
    db.runDbSync(SQLHelpers.concatMany(
      Some(sql"""
      SELECT
        r_henkilo.oppija_oid,
        r_henkilo.hetu,
        r_henkilo.syntymaaika,
        r_henkilo.etunimet,
        r_henkilo.sukunimi,
        json_agg(json_build_object(
          'oid', r_opiskeluoikeus.opiskeluoikeus_oid,
          'tyyppi', json_build_object(
            'koodiarvo', r_opiskeluoikeus.koulutusmuoto,
            'koodistoUri', 'opiskeluoikeudentyyppi'
          ),
          'oppilaitos', json_build_object(
            'oid', r_opiskeluoikeus.oppilaitos_oid,
            'nimi', json_build_object(
              'fi', r_opiskeluoikeus.oppilaitos_nimi
            )
          ),
          'alkamispäivä', r_opiskeluoikeus.alkamispaiva,
          'päättymispäivä', r_opiskeluoikeus.paattymispaiva,
          'ryhmä', r_opiskeluoikeus.luokka,
          'viimeisinTila', json_build_object(
            'koodiarvo', r_opiskeluoikeus.viimeisin_tila,
            'koodistoUri', 'koskiopiskeluoikeudentila'
          )
        )) opiskeluoikeudet
      FROM
        r_henkilo
        JOIN r_opiskeluoikeus ON
          r_opiskeluoikeus.oppija_oid = r_henkilo.oppija_oid
      """),
      oppilaitosOids.map(oids => sql"AND r_opiskeluoikeus.oppilaitos_oid = any($oids)"),
      Some(sql"""
        JOIN r_paatason_suoritus ON r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        -- Lasketaan voimassaolevien kotiopetusjaksojen määrä ehtoa 4a varten
        CROSS JOIN LATERAL (
          SELECT count(*) AS count
          FROM jsonb_array_elements(r_opiskeluoikeus.data -> 'lisätiedot' -> 'kotiopetusjaksot') jaksot
          WHERE jaksot ->> 'loppu' IS NULL
             OR to_char(NOW(), 'YYYY-MM-DD') BETWEEN jaksot ->> 'alku' AND jaksot ->> 'loppu'
        ) kotiopetusjaksoja
      WHERE
      """),
      oppijaOid.map(oid => sql"r_henkilo.oppija_oid = $oid AND"),
      Some(sql"""
        -- (1) oppija on potentiaalisesti oppivelvollinen, eli syntynyt 2004 tai myöhemmin
        EXTRACT(YEAR FROM r_henkilo.syntymaaika) >= 2004
        -- (2) oppijalla on peruskoulun opiskeluoikeus
        AND r_opiskeluoikeus.koulutusmuoto = 'perusopetus'
        AND r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
        -- (3) kyseisessä opiskeluoikeudessa on yhdeksännen luokan suoritus.
        AND r_paatason_suoritus.koulutusmoduuli_koodiarvo = '9'
        -- (4a) valvojalla on oppilaitostason oppilaitosoikeus ja opiskeluoikeuden lisätiedoista ei löydy kotiopetusjaksoa, joka osuu tälle hetkelle
        AND kotiopetusjaksoja.count = 0
        -- (5)  opiskeluoikeus ei ole eronnut tilassa tällä hetkellä
        AND r_opiskeluoikeus.viimeisin_tila <> 'eronnut'
        AND (
          -- (6a) opiskeluoikeus on läsnä tai väliaikaisesti keskeytynyt tällä hetkellä
          r_opiskeluoikeus.viimeisin_tila = any('{lasna, valiaikaisestikeskeytynyt}')
          -- TAI:
          OR (
            -- (6b.1 ) opiskeluoikeus on valmistunut-tilassa, ja siitä löytyy vahvistettu päättötodistus
            r_opiskeluoikeus.viimeisin_tila = 'valmistunut'
            -- (6b.2 ) ministeriön määrittelemä aikaraja ei ole kulunut umpeen
            -- TOISTAISEKSI VOIDAAN NÄYTTÄÄ AINA
          )
        )
      GROUP BY r_henkilo.oppija_oid
      ORDER BY r_henkilo.sukunimi, r_henkilo.etunimet
    """)).as[(ValpasOppija)])

  implicit private val getValpasOppijaResult: GetResult[ValpasOppija] = GetResult(r => {
    val rs: ResultSet = r.rs
    ValpasOppija(
      henkilö = ValpasHenkilö(
        oid = rs.getString("oppija_oid"),
        hetu = Option(rs.getString("hetu")),
        syntymäaika = Option(rs.getString("syntymaaika")),
        etunimet = rs.getString("etunimet"),
        sukunimi = rs.getString("sukunimi")
      ),
      opiskeluoikeudet = JsonSerializer.parse[List[ValpasOpiskeluoikeus]](rs.getString("opiskeluoikeudet"))
    )
  })
}
