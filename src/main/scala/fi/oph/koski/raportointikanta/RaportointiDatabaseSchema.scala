package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.temporal.ChronoUnit

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.LukioRaporttiOppiaineTaiKurssi
import fi.oph.koski.schema.LocalizedString
import org.json4s.JValue
import org.json4s.jackson.Json
import slick.dbio.DBIO
import slick.sql.SqlProfile.ColumnOption.SqlType

object RaportointiDatabaseSchema {

  val createOpiskeluoikeusIndexes = DBIO.seq(
    sqlu"CREATE UNIQUE INDEX ON r_opiskeluoikeus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppija_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppilaitos_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(koulutusmuoto)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(sisaltyy_opiskeluoikeuteen_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus_aikajakso(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus_aikajakso(alku)",
    sqlu"CREATE UNIQUE INDEX ON r_paatason_suoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON r_paatason_suoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_paatason_suoritus(vahvistus_paiva)",
    sqlu"CREATE INDEX ON r_paatason_suoritus(suorituksen_tyyppi)",
    sqlu"CREATE UNIQUE INDEX ON r_osasuoritus(osasuoritus_id)",
    sqlu"CREATE INDEX ON r_osasuoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON r_osasuoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_osasuoritus(vahvistus_paiva)",
    sqlu"CREATE INDEX ON r_osasuoritus(suorituksen_tyyppi)",
    sqlu"CREATE INDEX ON r_osasuoritus(ylempi_osasuoritus_id)"
  )

  val createOtherIndexes = DBIO.seq(
    sqlu"CREATE INDEX ON r_henkilo(hetu)",
    sqlu"CREATE INDEX ON r_organisaatio(oppilaitosnumero)",
    sqlu"CREATE UNIQUE INDEX ON r_koodisto_koodi(koodisto_uri, koodiarvo)"
  )

  val dropAllIfExists = DBIO.seq(
    sqlu"DROP TABLE IF EXISTS r_opiskeluoikeus",
    sqlu"DROP TABLE IF EXISTS r_opiskeluoikeus_aikajakso",
    sqlu"DROP TABLE IF EXISTS r_paatason_suoritus",
    sqlu"DROP TABLE IF EXISTS r_osasuoritus",
    sqlu"DROP TABLE IF EXISTS r_henkilo",
    sqlu"DROP TABLE IF EXISTS r_organisaatio",
    sqlu"DROP TABLE IF EXISTS r_koodisto_koodi",
    sqlu"DROP TABLE IF EXISTS raportointikanta_status"
  )

  val createRolesIfNotExists = DBIO.seq(
    sqlu"do 'begin create role raportointikanta_katselija; exception when others then null; end'",
    sqlu"do 'begin create role raportointikanta_henkilo_katselija; exception when others then null; end'"
  )

  val grantPermissions = DBIO.seq(
    sqlu"""GRANT SELECT ON
          r_opiskeluoikeus, r_opiskeluoikeus_aikajakso, r_paatason_suoritus, r_osasuoritus, r_organisaatio, r_koodisto_koodi, raportointikanta_status
          TO raportointikanta_katselija, raportointikanta_henkilo_katselija""",
    sqlu"GRANT SELECT ON r_henkilo TO raportointikanta_henkilo_katselija"

  )

  private val StringIdentifierType = SqlType("character varying collate \"C\"")

  class ROpiskeluoikeusTable(tag: Tag) extends Table[ROpiskeluoikeusRow](tag, "r_opiskeluoikeus") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima", SqlType("timestamptz"))
    val sisältyyOpiskeluoikeuteenOid = column[Option[String]]("sisaltyy_opiskeluoikeuteen_oid", StringIdentifierType)
    val oppijaOid = column[String]("oppija_oid", StringIdentifierType)
    val oppilaitosOid = column[String]("oppilaitos_oid", StringIdentifierType)
    val oppilaitosNimi = column[String]("oppilaitos_nimi")
    val oppilaitosKotipaikka = column[Option[String]]("oppilaitos_kotipaikka", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)
    val koulutustoimijaOid = column[String]("koulutustoimija_oid", StringIdentifierType)
    val koulutustoimijaNimi = column[String]("koulutustoimija_nimi")
    val koulutusmuoto = column[String]("koulutusmuoto", StringIdentifierType)
    val alkamispäivä = column[Option[Date]]("alkamispaiva")
    val päättymispäivä = column[Option[Date]]("paattymispaiva")
    val viimeisinTila = column[Option[String]]("viimeisin_tila", StringIdentifierType)
    val lisätiedotHenkilöstökoulutus = column[Boolean]("lisatiedot_henkilostokoulutus")
    val lisätiedotKoulutusvienti = column[Boolean]("lisatiedot_koulutusvienti")
    val data = column[JValue]("data")
    def * = (opiskeluoikeusOid, versionumero, aikaleima, sisältyyOpiskeluoikeuteenOid, oppijaOid,
      oppilaitosOid, oppilaitosNimi, oppilaitosKotipaikka, oppilaitosnumero, koulutustoimijaOid, koulutustoimijaNimi,
      koulutusmuoto, alkamispäivä, päättymispäivä, viimeisinTila,
      lisätiedotHenkilöstökoulutus, lisätiedotKoulutusvienti, data) <> (ROpiskeluoikeusRow.tupled, ROpiskeluoikeusRow.unapply)
  }

  class ROpiskeluoikeusAikajaksoTable(tag: Tag) extends Table[ROpiskeluoikeusAikajaksoRow](tag, "r_opiskeluoikeus_aikajakso") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val alku = column[Date]("alku")
    val loppu = column[Date]("loppu")
    val tila = column[String]("tila", StringIdentifierType)
    val tilaAlkanut = column[Date]("tila_alkanut")
    val opiskeluoikeusPäättynyt = column[Boolean]("opiskeluoikeus_paattynyt")
    val opintojenRahoitus = column[Option[String]]("opintojen_rahoitus", StringIdentifierType)
    val majoitus = column[Byte]("majoitus")
    val sisäoppilaitosmainenMajoitus = column[Byte]("sisaoppilaitosmainen_majoitus")
    val vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = column[Byte]("vaativan_erityisen_tuen_yhteydessa_jarjestettäva_majoitus")
    val erityinenTuki = column[Byte]("erityinen_tuki")
    val vaativanErityisenTuenErityinenTehtävä = column[Byte]("vaativan_erityisen_tuen_erityinen_tehtava")
    val hojks = column[Byte]("hojks")
    val vaikeastiVammainen = column[Byte]("vaikeasti_vammainen")
    val vammainenJaAvustaja = column[Byte]("vammainen_ja_avustaja")
    val osaAikaisuus = column[Byte]("osa_aikaisuus")
    val opiskeluvalmiuksiaTukevatOpinnot = column[Byte]("opiskeluvalmiuksia_tukevat_opinnot")
    val vankilaopetuksessa = column[Byte]("vankilaopetuksessa")
    val oppisopimusJossainPäätasonSuorituksessa = column[Byte]("oppisopimus_jossain_paatason_suorituksessa")
    def * = (opiskeluoikeusOid, alku, loppu, tila, tilaAlkanut, opiskeluoikeusPäättynyt,
      opintojenRahoitus, majoitus, sisäoppilaitosmainenMajoitus, vaativanErityisenTuenYhteydessäJärjestettäväMajoitus,
      erityinenTuki, vaativanErityisenTuenErityinenTehtävä, hojks, vaikeastiVammainen, vammainenJaAvustaja,
      osaAikaisuus, opiskeluvalmiuksiaTukevatOpinnot, vankilaopetuksessa, oppisopimusJossainPäätasonSuorituksessa, id) <> (ROpiskeluoikeusAikajaksoRow.tupled, ROpiskeluoikeusAikajaksoRow.unapply)
  }

  class RPäätasonSuoritusTable(tag: Tag) extends Table[RPäätasonSuoritusRow](tag, "r_paatason_suoritus") {
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliKoulutustyyppi = column[Option[String]]("koulutusmoduuli_koulutustyyppi", StringIdentifierType)
    val koulutusmoduuliLaajuusArvo = column[Option[Float]]("koulutusmoduuli_laajuus_arvo", SqlType("numeric"))
    val koulutusmoduuliLaajuusYksikkö = column[Option[String]]("koulutusmoduuli_laajuus_yksikko", StringIdentifierType)
    val koulutusmoduuliNimi = column[Option[String]]("koulutusmoduuli_nimi", StringIdentifierType)
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val arviointiArvosanaKoodiarvo = column[Option[String]]("arviointi_arvosana_koodiarvo", StringIdentifierType)
    val arviointiArvosanaKoodisto = column[Option[String]]("arviointi_arvosana_koodisto", StringIdentifierType)
    val arviointiHyväksytty = column[Option[Boolean]]("arviointi_hyvaksytty")
    val arviointiPäivä = column[Option[Date]]("arviointi_paiva")
    val toimipisteOid = column[String]("toimipiste_oid", StringIdentifierType)
    val toimipisteNimi = column[String]("toimipiste_nimi")
    val data = column[JValue]("data")
    def * = (päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliKoulutustyyppi,
      koulutusmoduuliLaajuusArvo, koulutusmoduuliLaajuusYksikkö, koulutusmoduuliNimi,vahvistusPäivä,
      arviointiArvosanaKoodiarvo, arviointiArvosanaKoodisto, arviointiHyväksytty, arviointiPäivä,
      toimipisteOid, toimipisteNimi, data) <> (RPäätasonSuoritusRow.tupled, RPäätasonSuoritusRow.unapply)
  }

  class ROsasuoritusTable(tag: Tag) extends Table[ROsasuoritusRow](tag, "r_osasuoritus") {
    val osasuoritusId = column[Long]("osasuoritus_id")
    val ylempiOsasuoritusId = column[Option[Long]]("ylempi_osasuoritus_id")
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliLaajuusArvo = column[Option[Float]]("koulutusmoduuli_laajuus_arvo", SqlType("numeric"))
    val koulutusmoduuliLaajuusYksikkö = column[Option[String]]("koulutusmoduuli_laajuus_yksikko", StringIdentifierType)
    val koulutusmoduuliPaikallinen = column[Boolean]("koulutusmoduuli_paikallinen")
    val koulutusmoduuliPakollinen = column[Option[Boolean]]("koulutusmoduuli_pakollinen")
    val koulutusmoduuliNimi = column[Option[String]]("koulutusmoduuli_nimi")
    val koulutusmoduuliOppimääräNimi = column[Option[String]]("koulutusmoduuli_oppimäärä_nimi")
    val koulutusmoduuliKieliaineNimi = column[Option[String]]("koulutusmoduuli_kieliaine_nimi")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val arviointiArvosanaKoodiarvo = column[Option[String]]("arviointi_arvosana_koodiarvo", StringIdentifierType)
    val arviointiArvosanaKoodisto = column[Option[String]]("arviointi_arvosana_koodisto", StringIdentifierType)
    val arviointiHyväksytty = column[Option[Boolean]]("arviointi_hyvaksytty")
    val arviointiPäivä = column[Option[Date]]("arviointi_paiva")
    val näytönArviointiPäivä = column[Option[Date]]("nayton_arviointi_paiva")
    val data = column[JValue]("data")
    def * = (osasuoritusId, ylempiOsasuoritusId, päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliLaajuusArvo, koulutusmoduuliLaajuusYksikkö,
      koulutusmoduuliPaikallinen, koulutusmoduuliPakollinen, koulutusmoduuliNimi,
      koulutusmoduuliOppimääräNimi, koulutusmoduuliKieliaineNimi, vahvistusPäivä,
      arviointiArvosanaKoodiarvo, arviointiArvosanaKoodisto, arviointiHyväksytty, arviointiPäivä,
      näytönArviointiPäivä, data) <> (ROsasuoritusRow.tupled, ROsasuoritusRow.unapply)
  }

  class RHenkilöTable(tag: Tag) extends Table[RHenkilöRow](tag, "r_henkilo") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey, StringIdentifierType)
    val masterOid = column[String]("master_oid", StringIdentifierType)
    val hetu = column[Option[String]]("hetu", StringIdentifierType)
    val sukupuoli = column[Option[String]]("sukupuoli")
    val syntymäaika = column[Option[Date]]("syntymaaika")
    val sukunimi = column[String]("sukunimi")
    val etunimet = column[String]("etunimet")
    val äidinkieli = column[Option[String]]("aidinkieli", StringIdentifierType)
    val kansalaisuus = column[Option[String]]("kansalaisuus", StringIdentifierType)
    val turvakielto = column[Boolean]("turvakielto")
    def * = (oppijaOid, masterOid, hetu, sukupuoli, syntymäaika, sukunimi, etunimet, äidinkieli, kansalaisuus, turvakielto) <> (RHenkilöRow.tupled, RHenkilöRow.unapply)
  }

  class ROrganisaatioTable(tag: Tag) extends Table[ROrganisaatioRow](tag, "r_organisaatio") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey, StringIdentifierType)
    val nimi = column[String]("nimi")
    val organisaatiotyypit = column[String]("organisaatiotyypit", StringIdentifierType)
    val oppilaitostyyppi = column[Option[String]]("oppilaitostyyppi", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)
    val kotipaikka = column[Option[String]]("kotipaikka", StringIdentifierType)
    val yTunnus = column[Option[String]]("y_tunnus", StringIdentifierType)
    def * = (organisaatioOid, nimi, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero, kotipaikka, yTunnus) <> (ROrganisaatioRow.tupled, ROrganisaatioRow.unapply)
  }

  class RKoodistoKoodiTable(tag: Tag) extends Table[RKoodistoKoodiRow](tag, "r_koodisto_koodi") {
    val koodistoUri = column[String]("koodisto_uri", StringIdentifierType)
    val koodiarvo = column[String]("koodiarvo", StringIdentifierType)
    val nimi = column[String]("nimi")
    def * = (koodistoUri, koodiarvo, nimi) <> (RKoodistoKoodiRow.tupled, RKoodistoKoodiRow.unapply)
  }

  class RaportointikantaStatusTable(tag: Tag) extends Table[RaportointikantaStatusRow](tag, "raportointikanta_status") {
    val name = column[String]("name", O.PrimaryKey)
    val loadStarted = column[Option[Timestamp]]("load_started")
    val loadCompleted = column[Option[Timestamp]]("load_completed")
    def * = (name, loadStarted, loadCompleted) <> (RaportointikantaStatusRow.tupled, RaportointikantaStatusRow.unapply)
  }
}

case class ROpiskeluoikeusRow(
  opiskeluoikeusOid: String,
  versionumero: Int,
  aikaleima: Timestamp,
  sisältyyOpiskeluoikeuteenOid: Option[String],
  oppijaOid: String,
  oppilaitosOid: String,
  oppilaitosNimi: String,
  oppilaitosKotipaikka: Option[String],
  oppilaitosnumero: Option[String],
  koulutustoimijaOid: String,
  koulutustoimijaNimi: String,
  koulutusmuoto: String,
  alkamispäivä: Option[Date],
  päättymispäivä: Option[Date],
  viimeisinTila: Option[String],
  lisätiedotHenkilöstökoulutus: Boolean,
  lisätiedotKoulutusvienti: Boolean,
  data: JValue
)

case class ROpiskeluoikeusAikajaksoRow(
  opiskeluoikeusOid: String,
  alku: Date,
  loppu: Date,
  tila: String,
  tilaAlkanut: Date,
  opiskeluoikeusPäättynyt: Boolean = false,
  opintojenRahoitus: Option[String] = None,
  majoitus: Byte = 0,
  sisäoppilaitosmainenMajoitus: Byte = 0,
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Byte = 0,
  erityinenTuki: Byte = 0,
  vaativanErityisenTuenErityinenTehtävä: Byte = 0,
  hojks: Byte = 0,
  vaikeastiVammainen: Byte = 0,
  vammainenJaAvustaja: Byte = 0,
  osaAikaisuus: Byte = 100,
  opiskeluvalmiuksiaTukevatOpinnot: Byte = 0,
  vankilaopetuksessa: Byte = 0,
  oppisopimusJossainPäätasonSuorituksessa: Byte = 0,
  id: Long = 0
) {
  def truncateToDates(start: Date, end: Date): ROpiskeluoikeusAikajaksoRow = this.copy(
    alku = if (alku.after(start)) alku else start,
    loppu = if (loppu.before(end)) loppu else end
  )
  lazy val lengthInDays: Int = ChronoUnit.DAYS.between(alku.toLocalDate, loppu.toLocalDate).toInt + 1

}

sealed trait RSuoritusRow {
  def arviointiArvosanaKoodiarvo: Option[String]
  def matchesWith(x: LukioRaporttiOppiaineTaiKurssi): Boolean
}

case class RPäätasonSuoritusRow(
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliKoulutustyyppi: Option[String],
  koulutusmoduuliLaajuusArvo: Option[Float],
  koulutusmoduuliLaajuusYksikkö: Option[String],
  koulutusmoduuliNimi: Option[String],
  vahvistusPäivä: Option[Date],
  arviointiArvosanaKoodiarvo: Option[String],
  arviointiArvosanaKoodisto: Option[String],
  arviointiHyväksytty: Option[Boolean],
  arviointiPäivä: Option[Date],
  toimipisteOid: String,
  toimipisteNimi: String,
  data: JValue
) extends RSuoritusRow {
  override def matchesWith(x: LukioRaporttiOppiaineTaiKurssi): Boolean = {
    val isPaikallinen = !koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava")

    suorituksestaKäytettäväNimi.contains(x.nimi) &&
      koulutusmoduuliKoodiarvo == x.koulutusmoduuliKoodiarvo &&
      isPaikallinen == x.koulutusmoduuliPaikallinen
  }

  def suorituksestaKäytettäväNimi: Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "kieli" \ "nimi").map(_.get("fi"))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "oppimäärä" \ "nimi").map(_.get("fi")))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "uskonnonOppimäärä" \ "nimi").map(_.get("fi")))
      .orElse(koulutusmoduuliNimi)
  }
}

case class ROsasuoritusRow(
  osasuoritusId: Long,
  ylempiOsasuoritusId: Option[Long] = None,
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String] = None,
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliLaajuusArvo: Option[Float] = None,
  koulutusmoduuliLaajuusYksikkö: Option[String] = None,
  koulutusmoduuliPaikallinen: Boolean,
  koulutusmoduuliPakollinen: Option[Boolean] = None,
  koulutusmoduuliNimi: Option[String] = None,
  koulutusmoduuliOppimääräNimi: Option[String] = None,
  koulutusmoduuliKieliaineNimi: Option[String] = None,
  vahvistusPäivä: Option[Date] = None,
  arviointiArvosanaKoodiarvo: Option[String] = None,
  arviointiArvosanaKoodisto: Option[String] = None,
  arviointiHyväksytty: Option[Boolean] = None,
  arviointiPäivä: Option[Date] = None,
  näytönArviointiPäivä: Option[Date] = None,
  data: JValue
) extends RSuoritusRow {
  override def matchesWith(x: LukioRaporttiOppiaineTaiKurssi): Boolean = {
    suorituksestaKäytettäväNimi.contains(x.nimi) &&
      koulutusmoduuliKoodiarvo == x.koulutusmoduuliKoodiarvo &&
      koulutusmoduuliPaikallinen == x.koulutusmoduuliPaikallinen
  }

  def suorituksestaKäytettäväNimi: Option[String] = {
    koulutusmoduuliKieliaineNimi
      .orElse(koulutusmoduuliOppimääräNimi)
      .orElse(koulutusmoduuliNimi)
  }
}

case class RHenkilöRow(
  oppijaOid: String,
  masterOid: String,
  hetu: Option[String],
  sukupuoli: Option[String],
  syntymäaika: Option[Date],
  sukunimi: String,
  etunimet: String,
  aidinkieli: Option[String],
  kansalaisuus: Option[String],
  turvakielto: Boolean
)

case class ROrganisaatioRow(
  organisaatioOid: String,
  nimi: String,
  organisaatiotyypit: String,
  oppilaitostyyppi: Option[String],
  oppilaitosnumero: Option[String],
  kotipaikka: Option[String],
  yTunnus: Option[String]
)

case class RKoodistoKoodiRow(
  koodistoUri: String,
  koodiarvo: String,
  nimi: String
)

case class RaportointikantaStatusRow(
  name: String,
  loadStarted: Option[Timestamp],
  loadCompleted: Option[Timestamp]
)
