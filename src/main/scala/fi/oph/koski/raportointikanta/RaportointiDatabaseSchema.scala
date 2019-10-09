package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.temporal.ChronoUnit

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.LukioRaporttiOppiaineTaiKurssi
import fi.oph.koski.schema.LocalizedString
import org.json4s.JValue
import slick.dbio.DBIO
import slick.sql.SqlProfile.ColumnOption.SqlType

object RaportointiDatabaseSchema {
  def moveSchema(oldSchema: Schema, newSchema: Schema) = DBIO.seq(
    sqlu"DROP SCHEMA IF EXISTS #${newSchema.name} CASCADE",
    sqlu"ALTER SCHEMA #${oldSchema.name} RENAME TO #${newSchema.name}"
  )

  def createSchemaIfNotExists(s: Schema) =
    sqlu"CREATE SCHEMA IF NOT EXISTS #${s.name}"

  def dropSchema(s: Schema) =
    sqlu"DROP SCHEMA #${s.name} CASCADE"

  def createOpiskeluoikeusIndexes(s: Schema) = DBIO.seq(
    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_opiskeluoikeus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus(oppija_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus(oppilaitos_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus(koulutusmuoto)",
    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus(sisaltyy_opiskeluoikeuteen_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus_aikajakso(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus_aikajakso(alku)",
    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_paatason_suoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${s.name}.r_paatason_suoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_paatason_suoritus(vahvistus_paiva)",
    sqlu"CREATE INDEX ON #${s.name}.r_paatason_suoritus(suorituksen_tyyppi)",
    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_osasuoritus(osasuoritus_id)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(vahvistus_paiva)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(suorituksen_tyyppi)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(ylempi_osasuoritus_id)"
  )

  def createOtherIndexes(s: Schema) = DBIO.seq(
    sqlu"CREATE INDEX ON #${s.name}.r_henkilo(hetu)",
    sqlu"CREATE INDEX ON #${s.name}.r_organisaatio(oppilaitosnumero)",
    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_koodisto_koodi(koodisto_uri, koodiarvo)"
  )

  def dropAllIfExists(s: Schema) = DBIO.seq(
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_opiskeluoikeus CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_opiskeluoikeus_aikajakso CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_paatason_suoritus CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_osasuoritus CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_henkilo CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_organisaatio CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_organisaatio_kieli CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_koodisto_koodi CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.raportointikanta_status CASCADE"
  )

  val createRolesIfNotExists = DBIO.seq(
    sqlu"do 'begin create role raportointikanta_katselija; exception when others then null; end'",
    sqlu"do 'begin create role raportointikanta_henkilo_katselija; exception when others then null; end'"
  )

  def grantPermissions(s: Schema) = DBIO.seq(actions =
    sqlu"GRANT USAGE ON SCHEMA #${s.name} TO raportointikanta_katselija, raportointikanta_henkilo_katselija",
    sqlu"""GRANT SELECT ON
      #${s.name}.r_opiskeluoikeus,
      #${s.name}.r_opiskeluoikeus_aikajakso,
      #${s.name}.r_paatason_suoritus,
      #${s.name}.r_osasuoritus,
      #${s.name}.r_organisaatio,
      #${s.name}.r_koodisto_koodi,
      #${s.name}.raportointikanta_status
      TO raportointikanta_katselija, raportointikanta_henkilo_katselija""",
    sqlu"GRANT SELECT ON #${s.name}.r_henkilo TO raportointikanta_henkilo_katselija"

  )

  private val StringIdentifierType = SqlType("character varying collate \"C\"")

  class ROpiskeluoikeusTable(tag: Tag, schema: Schema = Public) extends Table[ROpiskeluoikeusRow](tag, schema.nameOpt, "r_opiskeluoikeus") {
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
  class ROpiskeluoikeusTableTemp(tag: Tag) extends ROpiskeluoikeusTable(tag, Temp)

  class ROpiskeluoikeusAikajaksoTable(tag: Tag, schema: Schema = Public) extends Table[ROpiskeluoikeusAikajaksoRow](tag, schema.nameOpt, "r_opiskeluoikeus_aikajakso") {
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
  class ROpiskeluoikeusAikajaksoTableTemp(tag: Tag) extends ROpiskeluoikeusAikajaksoTable(tag, Temp)

  class RPäätasonSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RPäätasonSuoritusRow](tag, schema.nameOpt, "r_paatason_suoritus") {
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
  class RPäätasonSuoritusTableTemp(tag: Tag) extends RPäätasonSuoritusTable(tag, Temp)

  class ROsasuoritusTable(tag: Tag, schema: Schema = Public) extends Table[ROsasuoritusRow](tag, schema.nameOpt, "r_osasuoritus") {
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
  class ROsasuoritusTableTemp(tag: Tag) extends ROsasuoritusTable(tag, Temp)

  class RHenkilöTable(tag: Tag, schema: Schema = Public) extends Table[RHenkilöRow](tag, schema.nameOpt, "r_henkilo") {
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
    val kotikunta = column[Option[String]]("kotikunta")
    val yksiloity = column[Boolean]("yksiloity")
    def * = (oppijaOid, masterOid, hetu, sukupuoli, syntymäaika, sukunimi, etunimet, äidinkieli, kansalaisuus, turvakielto, kotikunta, yksiloity) <> (RHenkilöRow.tupled, RHenkilöRow.unapply)
  }
  class RHenkilöTableTemp(tag: Tag) extends RHenkilöTable(tag, Temp)

  class ROrganisaatioTable(tag: Tag, schema: Schema = Public) extends Table[ROrganisaatioRow](tag, schema.nameOpt, "r_organisaatio") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey, StringIdentifierType)
    val nimi = column[String]("nimi")
    val organisaatiotyypit = column[String]("organisaatiotyypit", StringIdentifierType)
    val oppilaitostyyppi = column[Option[String]]("oppilaitostyyppi", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)
    val kotipaikka = column[Option[String]]("kotipaikka", StringIdentifierType)
    val yTunnus = column[Option[String]]("y_tunnus", StringIdentifierType)
    def * = (organisaatioOid, nimi, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero, kotipaikka, yTunnus) <> (ROrganisaatioRow.tupled, ROrganisaatioRow.unapply)
  }

  class ROrganisaatioTableTemp(tag: Tag) extends ROrganisaatioTable(tag, Temp)

  class ROrganisaatioKieliTable(tag: Tag, schema: Schema = Public) extends Table[ROrganisaatioKieliRow](tag, schema.nameOpt, "r_organisaatio_kieli") {
    val organisaatioOid = column[String]("organisaatio_oid", StringIdentifierType)
    val kielikoodi = column[String]("kielikoodi", StringIdentifierType)
    def * = (organisaatioOid, kielikoodi) <> (ROrganisaatioKieliRow.tupled, ROrganisaatioKieliRow.unapply)
  }

  class ROrganisaatioKieliTableTemp(tag: Tag) extends ROrganisaatioKieliTable(tag, Temp)

  class RKoodistoKoodiTable(tag: Tag, schema: Schema = Public) extends Table[RKoodistoKoodiRow](tag, schema.nameOpt, "r_koodisto_koodi") {
    val koodistoUri = column[String]("koodisto_uri", StringIdentifierType)
    val koodiarvo = column[String]("koodiarvo", StringIdentifierType)
    val nimi = column[String]("nimi")
    def * = (koodistoUri, koodiarvo, nimi) <> (RKoodistoKoodiRow.tupled, RKoodistoKoodiRow.unapply)
  }
  class RKoodistoKoodiTableTemp(tag: Tag) extends RKoodistoKoodiTable(tag, Temp)

  class RaportointikantaStatusTable(tag: Tag, schema: Schema = Public) extends Table[RaportointikantaStatusRow](tag, schema.nameOpt, "raportointikanta_status") {
    val name = column[String]("name", O.PrimaryKey)
    val count = column[Int]("count", O.Default(0))
    val lastUpdate = column[Timestamp]("last_update", O.SqlType("timestamp default now()"))
    val loadStarted = column[Option[Timestamp]]("load_started", O.Default(None))
    val loadCompleted = column[Option[Timestamp]]("load_completed", O.Default(None))
    def * = (name, count, lastUpdate, loadStarted, loadCompleted) <> (RaportointikantaStatusRow.tupled, RaportointikantaStatusRow.unapply)
  }
  class RaportointikantaStatusTableTemp(tag: Tag) extends RaportointikantaStatusTable(tag, Temp)
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

  def suoritettu: Boolean = arviointiHyväksytty.exists(identity)
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
  turvakielto: Boolean,
  kotikunta: Option[String] = None,
  yksiloity: Boolean
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

case class ROrganisaatioKieliRow(
  organisaatioOid: String,
  kielikoodi: String
)

case class RKoodistoKoodiRow(
  koodistoUri: String,
  koodiarvo: String,
  nimi: String
)

case class RaportointikantaStatusRow(
  name: String,
  count: Int,
  lastUpdate: Timestamp,
  loadStarted: Option[Timestamp],
  loadCompleted: Option[Timestamp]
)

sealed trait Schema {
  def nameOpt: Option[String] = Some(name)
  def name: String
}

case object Public extends Schema {
  def name: String = "public"
}

case object Temp extends Schema {
  def name: String = "etl"
}
