package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.LocalDate
import java.time.temporal.ChronoUnit

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.YleissivistäväRaporttiOppiaineTaiKurssi
import fi.oph.koski.schema.LocalizedString
import org.json4s.JValue
import shapeless.{Generic, HNil}
import slickless._
import slick.dbio.DBIO
import slick.sql.SqlProfile.ColumnOption.SqlType

import scala.math.BigDecimal.decimal

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

    sqlu"CREATE INDEX ON #${s.name}.r_organisaatiohistoria(opiskeluoikeus_oid, alku, loppu DESC, oppilaitos_oid, koulutustoimija_oid)",

    sqlu"CREATE INDEX ON #${s.name}.r_opiskeluoikeus_aikajakso(opiskeluoikeus_oid)",

    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_paatason_suoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${s.name}.r_paatason_suoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_paatason_suoritus(vahvistus_paiva)",
    sqlu"CREATE INDEX ON #${s.name}.r_paatason_suoritus(suorituksen_tyyppi)",

    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_osasuoritus(osasuoritus_id)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${s.name}.r_osasuoritus(ylempi_osasuoritus_id)",

    sqlu"CREATE INDEX ON #${s.name}.esiopetus_opiskeluoik_aikajakso(opiskeluoikeus_oid)",
  )

  def createOtherIndexes(s: Schema) = DBIO.seq(
    sqlu"CREATE INDEX ON #${s.name}.r_henkilo(hetu)",
    sqlu"CREATE INDEX ON #${s.name}.r_organisaatio(oppilaitosnumero)",
    sqlu"CREATE UNIQUE INDEX ON #${s.name}.r_koodisto_koodi(koodisto_uri, koodiarvo)"
  )

  def dropAllIfExists(s: Schema) = DBIO.seq(
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_opiskeluoikeus CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_organisaatiohistoria CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_opiskeluoikeus_aikajakso CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.esiopetus_opiskeluoik_aikajakso CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_paatason_suoritus CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_osasuoritus CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_henkilo CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_organisaatio CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_organisaatio_kieli CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.r_koodisto_koodi CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.raportointikanta_status CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.muu_ammatillinen_raportointi CASCADE",
    sqlu"DROP TABLE IF EXISTS #${s.name}.topks_ammatillinen_raportointi CASCADE"
  )

  val createRolesIfNotExists = DBIO.seq(
    sqlu"do 'begin create role raportointikanta_katselija; exception when others then null; end'",
    sqlu"do 'begin create role raportointikanta_henkilo_katselija; exception when others then null; end'"
  )

  def grantPermissions(s: Schema) = DBIO.seq(actions =
    sqlu"GRANT USAGE ON SCHEMA #${s.name} TO raportointikanta_katselija, raportointikanta_henkilo_katselija",
    sqlu"""GRANT SELECT ON
      #${s.name}.r_opiskeluoikeus,
      #${s.name}.r_organisaatiohistoria,
      #${s.name}.r_opiskeluoikeus_aikajakso,
      #${s.name}.r_paatason_suoritus,
      #${s.name}.r_osasuoritus,
      #${s.name}.r_organisaatio,
      #${s.name}.r_organisaatio_kieli,
      #${s.name}.r_koodisto_koodi,
      #${s.name}.raportointikanta_status
      TO raportointikanta_katselija, raportointikanta_henkilo_katselija""",
    sqlu"""GRANT SELECT ON
      #${s.name}.r_henkilo,
      #${s.name}.esiopetus_opiskeluoik_aikajakso,
      #${s.name}.muu_ammatillinen_raportointi,
      #${s.name}.topks_ammatillinen_raportointi
      TO raportointikanta_henkilo_katselija"""
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
    val lähdejärjestelmäKoodiarvo = column[Option[String]]("lahdejarjestelma_koodiarvo")
    val lähdejärjestelmäId = column[Option[String]]("lahdejarjestelma_id")
    val data = column[JValue]("data")
    def * = (opiskeluoikeusOid, versionumero, aikaleima, sisältyyOpiskeluoikeuteenOid, oppijaOid,
      oppilaitosOid, oppilaitosNimi, oppilaitosKotipaikka, oppilaitosnumero, koulutustoimijaOid, koulutustoimijaNimi,
      koulutusmuoto, alkamispäivä, päättymispäivä, viimeisinTila,
      lisätiedotHenkilöstökoulutus, lisätiedotKoulutusvienti, lähdejärjestelmäKoodiarvo, lähdejärjestelmäId, data) <> (ROpiskeluoikeusRow.tupled, ROpiskeluoikeusRow.unapply)
  }
  class ROpiskeluoikeusTableTemp(tag: Tag) extends ROpiskeluoikeusTable(tag, Temp)

  class ROrganisaatioHistoriaTable(tag: Tag, schema: Schema = Public) extends Table[ROrganisaatioHistoriaRow](tag, schema.nameOpt, "r_organisaatiohistoria") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val oppilaitosOid = column[Option[String]]("oppilaitos_oid", StringIdentifierType)
    val koulutustoimijaOid = column[Option[String]]("koulutustoimija_oid", StringIdentifierType)
    val alku = column[LocalDate]("alku")
    val loppu = column[LocalDate]("loppu")

    def * = (opiskeluoikeusOid, oppilaitosOid, koulutustoimijaOid, alku, loppu) <>
      (ROrganisaatioHistoriaRow.tupled, ROrganisaatioHistoriaRow.unapply)
  }
  class ROrganisaatioHistoriaTableTemp(tag: Tag) extends ROrganisaatioHistoriaTable(tag, Temp)

  class ROpiskeluoikeusAikajaksoTable(tag: Tag, schema: Schema = Public) extends Table[ROpiskeluoikeusAikajaksoRow](tag, schema.nameOpt, "r_opiskeluoikeus_aikajakso") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val alku = column[Date]("alku")
    val loppu = column[Date]("loppu")
    val tila = column[String]("tila", StringIdentifierType)
    val tilaAlkanut = column[Date]("tila_alkanut")
    val osaAikaisuus = column[Byte]("osa_aikaisuus")
    val opintojenRahoitus = column[Option[String]]("opintojen_rahoitus", StringIdentifierType)
    val erityisenKoulutusTehtävänJaksoTehtäväKoodiarvo =  column[Option[String]]("erityisen_koulutus_tehtävän_jakso_tehtävä_koodiarvo", StringIdentifierType)
    val opiskeluoikeusPäättynyt = column[Boolean]("opiskeluoikeus_paattynyt")
    val ulkomainenVaihtoopiskelija = column[Boolean]("ulkomainen_vaihto_opiskelija")
    val majoitus = column[Boolean]("majoitus")
    val majoitusetu = column[Boolean]("majoitusetu")
    val kuljetusetu = column[Boolean]("kuljetusetu")
    val sisäoppilaitosmainenMajoitus = column[Boolean]("sisaoppilaitosmainen_majoitus")
    val vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = column[Boolean]("vaativan_erityisen_tuen_yhteydessa_jarjestettäva_majoitus")
    val erityinenTuki = column[Boolean]("erityinen_tuki")
    val vaativanErityisenTuenErityinenTehtävä = column[Boolean]("vaativan_erityisen_tuen_erityinen_tehtava")
    val hojks = column[Boolean]("hojks")
    val vammainen = column[Boolean]("vammainen")
    val vaikeastiVammainen = column[Boolean]("vaikeasti_vammainen")
    val vammainenJaAvustaja = column[Boolean]("vammainen_ja_avustaja")
    val opiskeluvalmiuksiaTukevatOpinnot = column[Boolean]("opiskeluvalmiuksia_tukevat_opinnot")
    val vankilaopetuksessa = column[Boolean]("vankilaopetuksessa")
    val oppisopimusJossainPäätasonSuorituksessa = column[Boolean]("oppisopimus_jossain_paatason_suorituksessa")
    val pidennettyOppivelvollisuus = column[Boolean]("pidennetty_oppivelvollisuus")
    val joustavaPerusopetus = column[Boolean]("joustava_perusopetus")
    val koulukoti = column[Boolean]("koulukoti")
    val oppimääränSuorittaja = column[Boolean]("oppimaaran_suorittaja")

    def * = (
      opiskeluoikeusOid ::
      alku ::
      loppu ::
      tila ::
      tilaAlkanut ::
      opiskeluoikeusPäättynyt ::
      opintojenRahoitus ::
      erityisenKoulutusTehtävänJaksoTehtäväKoodiarvo ::
      ulkomainenVaihtoopiskelija ::
      majoitus ::
      majoitusetu ::
      kuljetusetu ::
      sisäoppilaitosmainenMajoitus ::
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitus ::
      erityinenTuki ::
      vaativanErityisenTuenErityinenTehtävä ::
      hojks ::
      vammainen ::
      vaikeastiVammainen ::
      vammainenJaAvustaja ::
      osaAikaisuus ::
      opiskeluvalmiuksiaTukevatOpinnot ::
      vankilaopetuksessa ::
      oppisopimusJossainPäätasonSuorituksessa ::
      pidennettyOppivelvollisuus ::
      joustavaPerusopetus ::
      koulukoti ::
      oppimääränSuorittaja ::
      id ::
      HNil
    ).mappedWith(Generic[ROpiskeluoikeusAikajaksoRow])
  }
  class ROpiskeluoikeusAikajaksoTableTemp(tag: Tag) extends ROpiskeluoikeusAikajaksoTable(tag, Temp)

  class EsiopetusOpiskeluoikeusAikajaksoTable(tag: Tag, schema: Schema = Public) extends Table[EsiopetusOpiskeluoikeusAikajaksoRow](tag, schema.nameOpt, "esiopetus_opiskeluoik_aikajakso") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val alku = column[Date]("alku")
    val loppu = column[Date]("loppu")
    val tila = column[String]("tila", StringIdentifierType)
    val tilaAlkanut = column[Date]("tila_alkanut")
    val opiskeluoikeusPäättynyt = column[Boolean]("opiskeluoikeus_paattynyt")
    val pidennettyOppivelvollisuus = column[Boolean]("pidennetty_oppivelvollisuus")
    val tukimuodot = column[Option[String]]("tukimuodot")
    val erityisenTuenPäätös = column[Boolean]("erityisen_tuen_paatos")
    val erityisenTuenPäätösOpiskeleeToimintaAlueittain = column[Boolean]("erityisen_tuen_paatos_opiskelee_toiminta_alueittain")
    val erityisenTuenPäätösErityisryhmässä = column[Boolean]("erityisen_tuen_paatos_erityisryhmassa")
    val erityisenTuenPäätösToteutuspaikka = column[Option[String]]("erityisen_tuen_paatos_toteutuspaikka")
    val vammainen = column[Boolean]("vammainen")
    val vaikeastiVammainen = column[Boolean]("vaikeasti_vammainen")
    val majoitusetu = column[Boolean]("majoitusetu")
    val kuljetusetu = column[Boolean]("kuljetusetu")
    val sisäoppilaitosmainenMajoitus = column[Boolean]("sisaoppilaitosmainen_majoitus")
    val koulukoti = column[Boolean]("koulukoti")
    def * = (opiskeluoikeusOid, alku, loppu, tila, tilaAlkanut, opiskeluoikeusPäättynyt, pidennettyOppivelvollisuus, tukimuodot,
      erityisenTuenPäätös, erityisenTuenPäätösOpiskeleeToimintaAlueittain, erityisenTuenPäätösErityisryhmässä, erityisenTuenPäätösToteutuspaikka,
      vammainen, vaikeastiVammainen, majoitusetu, kuljetusetu,
      sisäoppilaitosmainenMajoitus, koulukoti) <> (EsiopetusOpiskeluoikeusAikajaksoRow.tupled, EsiopetusOpiskeluoikeusAikajaksoRow.unapply)
  }
  class EsiopetusOpiskeluoikeusAikajaksoTableTemp(tag: Tag) extends EsiopetusOpiskeluoikeusAikajaksoTable(tag, Temp)

  class RPäätasonSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RPäätasonSuoritusRow](tag, schema.nameOpt, "r_paatason_suoritus") {
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliKoulutustyyppi = column[Option[String]]("koulutusmoduuli_koulutustyyppi", StringIdentifierType)
    val koulutusmoduuliLaajuusArvo = column[Option[Double]]("koulutusmoduuli_laajuus_arvo", SqlType("numeric"))
    val koulutusmoduuliLaajuusYksikkö = column[Option[String]]("koulutusmoduuli_laajuus_yksikko", StringIdentifierType)
    val koulutusmoduuliNimi = column[Option[String]]("koulutusmoduuli_nimi", StringIdentifierType)
    val suorituskieliKoodiarvo = column[Option[String]]("suorituskieli_koodiarvo", StringIdentifierType)
    val oppimääräKoodiarvo = column[Option[String]]("oppimaara_koodiarvo", StringIdentifierType)
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val arviointiArvosanaKoodiarvo = column[Option[String]]("arviointi_arvosana_koodiarvo", StringIdentifierType)
    val arviointiArvosanaKoodisto = column[Option[String]]("arviointi_arvosana_koodisto", StringIdentifierType)
    val arviointiHyväksytty = column[Option[Boolean]]("arviointi_hyvaksytty")
    val arviointiPäivä = column[Option[Date]]("arviointi_paiva")
    val toimipisteOid = column[String]("toimipiste_oid", StringIdentifierType)
    val toimipisteNimi = column[String]("toimipiste_nimi")
    val data = column[JValue]("data")
    val sisältyyOpiskeluoikeuteenOid = column[Option[String]]("sisaltyy_opiskeluoikeuteen_oid", StringIdentifierType)
    def * = (päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliKoulutustyyppi,
      koulutusmoduuliLaajuusArvo, koulutusmoduuliLaajuusYksikkö, koulutusmoduuliNimi, suorituskieliKoodiarvo, oppimääräKoodiarvo, vahvistusPäivä,
      arviointiArvosanaKoodiarvo, arviointiArvosanaKoodisto, arviointiHyväksytty, arviointiPäivä,
      toimipisteOid, toimipisteNimi, data, sisältyyOpiskeluoikeuteenOid) <> (RPäätasonSuoritusRow.tupled, RPäätasonSuoritusRow.unapply)
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
    val koulutusmoduuliLaajuusArvo = column[Option[Double]]("koulutusmoduuli_laajuus_arvo", SqlType("numeric"))
    val koulutusmoduuliLaajuusYksikkö = column[Option[String]]("koulutusmoduuli_laajuus_yksikko", StringIdentifierType)
    val koulutusmoduuliPaikallinen = column[Boolean]("koulutusmoduuli_paikallinen")
    val koulutusmoduuliPakollinen = column[Option[Boolean]]("koulutusmoduuli_pakollinen")
    val koulutusmoduuliNimi = column[Option[String]]("koulutusmoduuli_nimi")
    val koulutusmoduuliOppimääräNimi = column[Option[String]]("koulutusmoduuli_oppimäärä_nimi")
    val koulutusmoduuliKieliaineNimi = column[Option[String]]("koulutusmoduuli_kieliaine_nimi")
    val koulutusmoduuliKurssinTyyppi = column[Option[String]]("koulutusmoduuli_kurssin_tyyppi")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val arviointiArvosanaKoodiarvo = column[Option[String]]("arviointi_arvosana_koodiarvo", StringIdentifierType)
    val arviointiArvosanaKoodisto = column[Option[String]]("arviointi_arvosana_koodisto", StringIdentifierType)
    val arviointiHyväksytty = column[Option[Boolean]]("arviointi_hyvaksytty")
    val arviointiPäivä = column[Option[Date]]("arviointi_paiva")
    val näytönArviointiPäivä = column[Option[Date]]("nayton_arviointi_paiva")
    val tunnustettu = column[Boolean]("tunnustettu")
    val tunnustettuRahoituksenPiirissä = column[Boolean]("tunnustettu_rahoituksen_piirissa")
    val data = column[JValue]("data")
    val sisältyyOpiskeluoikeuteenOid = column[Option[String]]("sisaltyy_opiskeluoikeuteen_oid", StringIdentifierType)
    def * = (
      osasuoritusId ::
      ylempiOsasuoritusId ::
      päätasonSuoritusId ::
      opiskeluoikeusOid ::
      suorituksenTyyppi ::
      koulutusmoduuliKoodisto ::
      koulutusmoduuliKoodiarvo ::
      koulutusmoduuliLaajuusArvo ::
      koulutusmoduuliLaajuusYksikkö ::
      koulutusmoduuliPaikallinen ::
      koulutusmoduuliPakollinen ::
      koulutusmoduuliNimi ::
      koulutusmoduuliOppimääräNimi ::
      koulutusmoduuliKieliaineNimi ::
      koulutusmoduuliKurssinTyyppi ::
      vahvistusPäivä ::
      arviointiArvosanaKoodiarvo ::
      arviointiArvosanaKoodisto ::
      arviointiHyväksytty ::
      arviointiPäivä ::
      näytönArviointiPäivä ::
      tunnustettu ::
      tunnustettuRahoituksenPiirissä ::
      data ::
      sisältyyOpiskeluoikeuteenOid ::
      HNil
    ).mappedWith(Generic[ROsasuoritusRow])
  }

  class ROsasuoritusTableTemp(tag: Tag) extends ROsasuoritusTable(tag, Temp)

  class MuuAmmatillinenOsasuoritusRaportointiTable(tag: Tag, schema: Schema = Public) extends Table[MuuAmmatillinenOsasuoritusRaportointiRow](tag, schema.nameOpt, "muu_ammatillinen_raportointi") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val toteuttavanLuokanNimi = column[String]("toteuttavan_luokan_nimi")
    val koulutusmoduuliLaajuusArvo = column[Option[Double]]("koulutusmoduuli_laajuus_arvo", SqlType("numeric"))
    val koulutusmoduuliLaajuusYksikkö = column[Option[String]]("koulutusmoduuli_laajuus_yksikko", StringIdentifierType)
    val arviointiHyväksytty = column[Boolean]("arviointi_hyvaksytty")
    def * = (opiskeluoikeusOid, päätasonSuoritusId, toteuttavanLuokanNimi, koulutusmoduuliLaajuusArvo, koulutusmoduuliLaajuusYksikkö, arviointiHyväksytty) <> (MuuAmmatillinenOsasuoritusRaportointiRow.tupled, MuuAmmatillinenOsasuoritusRaportointiRow.unapply)
  }

  class MuuAmmatillinenOsasuoritusRaportointiTableTemp(tag: Tag) extends MuuAmmatillinenOsasuoritusRaportointiTable(tag, Temp)

  class TOPKSAmmatillinenOsasuoritusRaportointiTable(tag: Tag, schema: Schema = Public) extends Table[TOPKSAmmatillinenRaportointiRow](tag, schema.nameOpt, "topks_ammatillinen_raportointi") {
    val opiskeluoikeudenOid =  column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val toteuttavanLuokanNimi = column[String]("toteuttavan_luokan_nimi")
    val rahoituksenPiirissä = column[Boolean]("rahoituksen_piirissa")
    val arviointiHyväksytty = column[Boolean]("arviointi_hyvaksytty")
    val tunnustettu = column[Boolean]("tunnustettu")
    val koulutusmoduuliLaajuusArvo = column[Option[Double]]("koulutusmoduuli_laajuus_arvo")
    val koulutusmoduuliLaajuusYksikkö =  column[Option[String]]("koulutusmoduuli_laajuus_yksikko")
    def * = (opiskeluoikeudenOid, päätasonSuoritusId, toteuttavanLuokanNimi, rahoituksenPiirissä, arviointiHyväksytty, tunnustettu, koulutusmoduuliLaajuusArvo, koulutusmoduuliLaajuusYksikkö) <> (TOPKSAmmatillinenRaportointiRow.tupled, TOPKSAmmatillinenRaportointiRow.unapply)
  }

  class TOPKSAmmatillinenOsasuoritusRaportointiTableTemp(tag: Tag) extends TOPKSAmmatillinenOsasuoritusRaportointiTable(tag, Temp)

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

trait AikajaksoRow[A] {
  def tila: String
  def tilaAlkanut: Date
  def alku: Date
  def loppu: Date
  def withTilaAlkanut(d: Date): A
  def withLoppu(d: Date): A
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
  lähdejärjestelmäKoodiarvo: Option[String],
  lähdejärjestelmäId: Option[String],
  data: JValue
)

case class ROrganisaatioHistoriaRow(
  opiskeluoikeusOid: String,
  oppilaitosOid: Option[String],
  koulutustoimijaOid: Option[String],
  alku: LocalDate,
  loppu: LocalDate
)

case class ROpiskeluoikeusAikajaksoRow(
  opiskeluoikeusOid: String,
  alku: Date,
  loppu: Date,
  tila: String,
  tilaAlkanut: Date,
  opiskeluoikeusPäättynyt: Boolean = false,
  opintojenRahoitus: Option[String] = None,
  erityisenKoulutusTehtävänJaksoTehtäväKoodiarvo: Option[String] = None,
  ulkomainenVaihtoopiskelija: Boolean = false,
  majoitus: Boolean = false,
  majoitusetu: Boolean = false,
  kuljetusetu: Boolean = false,
  sisäoppilaitosmainenMajoitus: Boolean = false,
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Boolean = false,
  erityinenTuki: Boolean = false,
  vaativanErityisenTuenErityinenTehtävä: Boolean = false,
  hojks: Boolean = false,
  vammainen: Boolean = false,
  vaikeastiVammainen: Boolean = false,
  vammainenJaAvustaja: Boolean = false,
  osaAikaisuus: Byte = 100,
  opiskeluvalmiuksiaTukevatOpinnot: Boolean = false,
  vankilaopetuksessa: Boolean = false,
  oppisopimusJossainPäätasonSuorituksessa: Boolean = false,
  pidennettyOppivelvollisuus: Boolean = false,
  joustavaPerusopetus: Boolean = false,
  koulukoti: Boolean = false,
  oppimääränSuorittaja: Boolean = false,
  id: Long = 0
) extends AikajaksoRow[ROpiskeluoikeusAikajaksoRow] {
  def truncateToDates(start: Date, end: Date): ROpiskeluoikeusAikajaksoRow = this.copy(
    alku = if (alku.after(start)) alku else start,
    loppu = if (loppu.before(end)) loppu else end
  )
  def truncateToDates(start: LocalDate, end: LocalDate): ROpiskeluoikeusAikajaksoRow =
    this.truncateToDates(Date.valueOf(start), Date.valueOf(end))
  lazy val lengthInDays: Int = ChronoUnit.DAYS.between(alku.toLocalDate, loppu.toLocalDate).toInt + 1

  def withLoppu(d: Date): ROpiskeluoikeusAikajaksoRow = this.copy(loppu = d)
  def withTilaAlkanut(d: Date): ROpiskeluoikeusAikajaksoRow = this.copy(tilaAlkanut = d)
}

case class EsiopetusOpiskeluoikeusAikajaksoRow(
  opiskeluoikeusOid: String,
  alku: Date,
  loppu: Date,
  tila: String,
  tilaAlkanut: Date,
  opiskeluoikeusPäättynyt: Boolean = false,
  pidennettyOppivelvollisuus: Boolean = false,
  tukimuodot: Option[String] = None,
  erityisenTuenPäätös: Boolean = false,
  erityisenTuenPäätösOpiskeleeToimintaAlueittain: Boolean = false,
  erityisenTuenPäätösErityisryhmässä: Boolean = false,
  erityisenTuenPäätösToteutuspaikka: Option[String] = None,
  vammainen: Boolean = false,
  vaikeastiVammainen: Boolean = false,
  majoitusetu: Boolean = false,
  kuljetusetu: Boolean = false,
  sisäoppilaitosmainenMajoitus: Boolean = false,
  koulukoti: Boolean = false
) extends AikajaksoRow[EsiopetusOpiskeluoikeusAikajaksoRow] {
  def withLoppu(d: Date): EsiopetusOpiskeluoikeusAikajaksoRow = this.copy(loppu = d)
  def withTilaAlkanut(d: Date): EsiopetusOpiskeluoikeusAikajaksoRow = this.copy(tilaAlkanut = d)
}

sealed trait RSuoritusRow {
  def arviointiArvosanaKoodiarvo: Option[String]
  def matchesWith(x: YleissivistäväRaporttiOppiaineTaiKurssi): Boolean
  def arviointiHyväksytty: Option[Boolean]
  def arvioituJaHyväksytty: Boolean = arviointiHyväksytty.getOrElse(false)
}

case class RPäätasonSuoritusRow(
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliKoulutustyyppi: Option[String],
  koulutusmoduuliLaajuusArvo: Option[Double],
  koulutusmoduuliLaajuusYksikkö: Option[String],
  koulutusmoduuliNimi: Option[String],
  suorituskieliKoodiarvo: Option[String],
  oppimääräKoodiarvo: Option[String],
  vahvistusPäivä: Option[Date],
  arviointiArvosanaKoodiarvo: Option[String],
  arviointiArvosanaKoodisto: Option[String],
  arviointiHyväksytty: Option[Boolean],
  arviointiPäivä: Option[Date],
  toimipisteOid: String,
  toimipisteNimi: String,
  data: JValue,
  sisältyyOpiskeluoikeuteenOid: Option[String]
) extends RSuoritusRow {
  override def matchesWith(x: YleissivistäväRaporttiOppiaineTaiKurssi): Boolean = {
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
  ylempiOsasuoritusId: Option[Long],
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliLaajuusArvo: Option[Double],
  koulutusmoduuliLaajuusYksikkö: Option[String],
  koulutusmoduuliPaikallinen: Boolean,
  koulutusmoduuliPakollinen: Option[Boolean],
  koulutusmoduuliNimi: Option[String],
  koulutusmoduuliOppimääräNimi: Option[String],
  koulutusmoduuliKieliaineNimi: Option[String],
  koulutusmoduuliKurssinTyyppi: Option[String],
  vahvistusPäivä: Option[Date],
  arviointiArvosanaKoodiarvo: Option[String],
  arviointiArvosanaKoodisto: Option[String],
  arviointiHyväksytty: Option[Boolean],
  arviointiPäivä: Option[Date],
  näytönArviointiPäivä: Option[Date],
  tunnustettu: Boolean,
  tunnustettuRahoituksenPiirissä: Boolean,
  data: JValue,
  sisältyyOpiskeluoikeuteenOid: Option[String]
) extends RSuoritusRow {
  override def matchesWith(x: YleissivistäväRaporttiOppiaineTaiKurssi): Boolean = {
    suorituksestaKäytettäväNimi.contains(x.nimi) &&
      koulutusmoduuliKoodiarvo == x.koulutusmoduuliKoodiarvo &&
      koulutusmoduuliPaikallinen == x.koulutusmoduuliPaikallinen
  }

  def suorituksestaKäytettäväNimi: Option[String] = {
    koulutusmoduuliKieliaineNimi
      .orElse(koulutusmoduuliOppimääräNimi)
      .orElse(koulutusmoduuliNimi)
  }

  def laajuus: BigDecimal = koulutusmoduuliLaajuusArvo.map(decimal).getOrElse(decimal(1.0))
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

case class MuuAmmatillinenOsasuoritusRaportointiRow(
  opiskeluoikeusOid: String,
  päätasonSuoritusId: Long,
  toteuttavanLuokanNimi: String,
  koulutusmoduuliLaajuusArvo: Option[Double] = None,
  koulutusmoduuliLaajuusYksikkö: Option[String] = None,
  arviointiHyväksytty: Boolean
)

case class TOPKSAmmatillinenRaportointiRow(
  opiskeluoikeudenOid: String,
  päätasonSuoritusId: Long,
  toteuttavanLuokanNimi: String,
  rahoituksenPiirissä: Boolean,
  arviointiHyväksytty: Boolean,
  tunnustettu: Boolean,
  koulutusmoduuliLaajuusArvo: Option[Double],
  koulutusmoduuliLaajuusYksikkö: Option[String]
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
