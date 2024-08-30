package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.Kotikuntahistoria
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.{YleissivistäväRaporttiKurssi, YleissivistäväRaporttiOppiaine, YleissivistäväRaporttiOppiaineTaiKurssi}
import fi.oph.koski.schema.LocalizedString
import org.json4s.JValue
import shapeless.{Generic, HNil}
import slickless._
import slick.dbio.DBIO
import slick.sql.SqlProfile.ColumnOption.SqlType

import scala.math.BigDecimal.decimal

object RaportointiDatabaseSchema {

  private val StringIdentifierType = SqlType("character varying collate \"C\"")

  val createRolesIfNotExists = DBIO.seq(
    sqlu"do 'begin create role raportointikanta_katselija; exception when others then null; end'",
    sqlu"do 'begin create role raportointikanta_henkilo_katselija; exception when others then null; end'"
  )

  class ROpiskeluoikeusTable(tag: Tag, schema: Schema = Public) extends Table[ROpiskeluoikeusRow](tag, schema.nameOpt, "r_opiskeluoikeus") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType, O.PrimaryKey)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima", SqlType("timestamptz"))
    val sisältyyOpiskeluoikeuteenOid = column[Option[String]]("sisaltyy_opiskeluoikeuteen_oid", StringIdentifierType)
    val oppijaOid = column[String]("oppija_oid", StringIdentifierType)
    val oppilaitosOid = column[String]("oppilaitos_oid", StringIdentifierType)
    val oppilaitosNimi = column[String]("oppilaitos_nimi")
    val oppilaitosNimiSv = column[String]("oppilaitos_nimi_sv")
    val oppilaitosKotipaikka = column[Option[String]]("oppilaitos_kotipaikka", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)
    val koulutustoimijaOid = column[String]("koulutustoimija_oid", StringIdentifierType)
    val koulutustoimijaNimi = column[String]("koulutustoimija_nimi")
    val koulutustoimijaNimiSv = column[String]("koulutustoimija_nimi_sv")
    val koulutusmuoto = column[String]("koulutusmuoto", StringIdentifierType)
    val alkamispäivä = column[Option[Date]]("alkamispaiva")
    val päättymispäivä = column[Option[Date]]("paattymispaiva")
    val viimeisinTila = column[Option[String]]("viimeisin_tila", StringIdentifierType)
    val lisätiedotHenkilöstökoulutus = column[Boolean]("lisatiedot_henkilostokoulutus")
    val lisätiedotKoulutusvienti = column[Boolean]("lisatiedot_koulutusvienti")
    val tuvaJärjestämislupa = column[Option[String]]("tuva_jarjestamislupa")
    val lähdejärjestelmäKoodiarvo = column[Option[String]]("lahdejarjestelma_koodiarvo")
    val lähdejärjestelmäId = column[Option[String]]("lahdejarjestelma_id")
    val oppivelvollisuudenSuorittamiseenKelpaava = column[Boolean]("oppivelvollisuuden_suorittamiseen_kelpaava")
    val data = column[JValue]("data")
    def * = (opiskeluoikeusOid :: versionumero :: aikaleima :: sisältyyOpiskeluoikeuteenOid :: oppijaOid ::
      oppilaitosOid :: oppilaitosNimi :: oppilaitosNimiSv :: oppilaitosKotipaikka :: oppilaitosnumero :: koulutustoimijaOid ::
      koulutustoimijaNimi :: koulutustoimijaNimiSv :: koulutusmuoto :: alkamispäivä :: päättymispäivä :: viimeisinTila ::
      lisätiedotHenkilöstökoulutus :: lisätiedotKoulutusvienti :: tuvaJärjestämislupa :: lähdejärjestelmäKoodiarvo :: lähdejärjestelmäId ::
      oppivelvollisuudenSuorittamiseenKelpaava :: data :: HNil).mappedWith(Generic[ROpiskeluoikeusRow])
  }
  class ROpiskeluoikeusTableTemp(tag: Tag) extends ROpiskeluoikeusTable(tag, Temp)
  class ROpiskeluoikeusConfidentialTable(tag: Tag) extends ROpiskeluoikeusTable(tag, Confidential)
  class ROpiskeluoikeusConfidentialTableTemp(tag: Tag) extends ROpiskeluoikeusTable(tag, TempConfidential)

  class RMitätöityOpiskeluoikeusTable(tag: Tag, schema: Schema = Public)
    extends Table[RMitätöityOpiskeluoikeusRow](tag, schema.nameOpt, "r_mitatoitu_opiskeluoikeus") {

    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType, O.PrimaryKey)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima", SqlType("timestamptz"))
    val oppijaOid = column[String]("oppija_oid", StringIdentifierType)
    val mitätöity = column[Option[LocalDate]]("mitatoitu")
    val suostumusPeruttu = column[Option[LocalDate]]("suostumus_peruttu")
    val tyyppi = column[String]("tyyppi", StringIdentifierType)
    val päätasonSuoritusTyypit = column[List[String]]("paatason_suoritus_tyypit")
    val oppilaitosOid = column[Option[String]]("oppilaitos_oid")
    val oppilaitoksenNimi = column[Option[String]]("oppilaitos_nimi")
    val koulutustoimijaOid = column[Option[String]]("koulutustoimija_oid")
    val koulutustoimijanNimi = column[Option[String]]("koulutustoimija_nimi")

    def * = (
      opiskeluoikeusOid,
      versionumero,
      aikaleima,
      oppijaOid,
      mitätöity,
      suostumusPeruttu,
      tyyppi,
      päätasonSuoritusTyypit,
      oppilaitosOid,
      oppilaitoksenNimi,
      koulutustoimijaOid,
      koulutustoimijanNimi,
    ) <> (RMitätöityOpiskeluoikeusRow.tupled, RMitätöityOpiskeluoikeusRow.unapply)
  }
  class RMitätöityOpiskeluoikeusTableTemp(tag: Tag) extends RMitätöityOpiskeluoikeusTable(tag, Temp)
  class RMitätöityOpiskeluoikeusConfidentialTable(tag: Tag) extends RMitätöityOpiskeluoikeusTable(tag, Confidential)
  class RMitätöityOpiskeluoikeusConfidentialTableTemp(tag: Tag) extends RMitätöityOpiskeluoikeusTable(tag, TempConfidential)

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
  class ROrganisaatioHistoriaConfidentialTable(tag: Tag) extends ROrganisaatioHistoriaTable(tag, Confidential)
  class ROrganisaatioHistoriaConfidentialTableTemp(tag: Tag) extends ROrganisaatioHistoriaTable(tag, TempConfidential)

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
    val maksuton = column[Boolean]("maksuton")
    val maksullinen = column[Boolean]("maksullinen")
    val oikeuttaMaksuttomuuteenPidennetty = column[Boolean]("oikeutta_maksuttomuuteen_pidennetty")
    val kotiopetus = column[Boolean]("kotiopetus")
    val ulkomaanjakso = column[Boolean]("ulkomaanjakso")

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
      maksuton ::
      maksullinen ::
      oikeuttaMaksuttomuuteenPidennetty ::
      kotiopetus ::
      ulkomaanjakso ::
      id ::
      HNil
    ).mappedWith(Generic[ROpiskeluoikeusAikajaksoRow])
  }
  class ROpiskeluoikeusAikajaksoTableTemp(tag: Tag) extends ROpiskeluoikeusAikajaksoTable(tag, Temp)
  class ROpiskeluoikeusAikajaksoConfidentialTable(tag: Tag) extends ROpiskeluoikeusAikajaksoTable(tag, Confidential)
  class ROpiskeluoikeusAikajaksoConfidentialTableTemp(tag: Tag) extends ROpiskeluoikeusAikajaksoTable(tag, TempConfidential)

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
  class EsiopetusOpiskeluoikeusAikajaksoConfidentialTable(tag: Tag) extends EsiopetusOpiskeluoikeusAikajaksoTable(tag, Confidential)
  class EsiopetusOpiskeluoikeusAikajaksoConfidentialTableTemp(tag: Tag) extends EsiopetusOpiskeluoikeusAikajaksoTable(tag, TempConfidential)

  class RPäätasonSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RPäätasonSuoritusRow](tag, schema.nameOpt, "r_paatason_suoritus") {
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id", O.PrimaryKey)
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliKoulutustyyppi = column[Option[String]]("koulutusmoduuli_koulutustyyppi", StringIdentifierType)
    val koulutusmoduuliLaajuusArvo = column[Option[Double]]("koulutusmoduuli_laajuus_arvo", SqlType("numeric"))
    val koulutusmoduuliLaajuusYksikkö = column[Option[String]]("koulutusmoduuli_laajuus_yksikko", StringIdentifierType)
    val koulutusmoduuliNimi = column[Option[String]]("koulutusmoduuli_nimi", StringIdentifierType)
    val tutkinnonNimiPerusteessa = column[Option[String]]("tutkinnon_nimi_perusteessa", StringIdentifierType)
    val suorituskieliKoodiarvo = column[Option[String]]("suorituskieli_koodiarvo", StringIdentifierType)
    val oppimääräKoodiarvo = column[Option[String]]("oppimaara_koodiarvo", StringIdentifierType)
    val alkamispäivä = column[Option[Date]]("alkamispaiva")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val arviointiArvosanaKoodiarvo = column[Option[String]]("arviointi_arvosana_koodiarvo", StringIdentifierType)
    val arviointiArvosanaKoodisto = column[Option[String]]("arviointi_arvosana_koodisto", StringIdentifierType)
    val arviointiHyväksytty = column[Option[Boolean]]("arviointi_hyvaksytty")
    val arviointiPäivä = column[Option[Date]]("arviointi_paiva")
    val toimipisteOid = column[String]("toimipiste_oid", StringIdentifierType)
    val toimipisteNimi = column[String]("toimipiste_nimi")
    val toimipisteNimiSv = column[String]("toimipiste_nimi_sv")
    val data = column[JValue]("data")
    val sisältyyOpiskeluoikeuteenOid = column[Option[String]]("sisaltyy_opiskeluoikeuteen_oid", StringIdentifierType)
    def * = (päätasonSuoritusId :: opiskeluoikeusOid :: suorituksenTyyppi ::
      koulutusmoduuliKoodisto :: koulutusmoduuliKoodiarvo :: koulutusmoduuliKoulutustyyppi ::
      koulutusmoduuliLaajuusArvo :: koulutusmoduuliLaajuusYksikkö :: koulutusmoduuliNimi ::
      tutkinnonNimiPerusteessa :: suorituskieliKoodiarvo :: oppimääräKoodiarvo :: alkamispäivä ::
      vahvistusPäivä :: arviointiArvosanaKoodiarvo :: arviointiArvosanaKoodisto :: arviointiHyväksytty ::
      arviointiPäivä :: toimipisteOid :: toimipisteNimi :: toimipisteNimiSv :: data :: sisältyyOpiskeluoikeuteenOid ::
      HNil).mappedWith(Generic[RPäätasonSuoritusRow])
  }
  class RPäätasonSuoritusTableTemp(tag: Tag) extends RPäätasonSuoritusTable(tag, Temp)
  class RPäätasonSuoritusConfidentialTable(tag: Tag) extends RPäätasonSuoritusTable(tag, Confidential)
  class RPäätasonSuoritusConfidentialTableTemp(tag: Tag) extends RPäätasonSuoritusTable(tag, TempConfidential)

  class ROsasuoritusTable(tag: Tag, schema: Schema = Public) extends Table[ROsasuoritusRow](tag, schema.nameOpt, "r_osasuoritus") {
    val osasuoritusId = column[Long]("osasuoritus_id", O.PrimaryKey)
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
    val ensimmäinenArviointiPäivä  = column[Option[Date]]("ensimmainen_arviointi_paiva")
    val korotettuEriVuonna  = column[Boolean]("korotettu_eri_vuonna")
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
      ensimmäinenArviointiPäivä ::
      korotettuEriVuonna ::
      näytönArviointiPäivä ::
      tunnustettu ::
      tunnustettuRahoituksenPiirissä ::
      data ::
      sisältyyOpiskeluoikeuteenOid ::
      HNil
    ).mappedWith(Generic[ROsasuoritusRow])
  }

  class ROsasuoritusTableTemp(tag: Tag) extends ROsasuoritusTable(tag, Temp)
  class ROsasuoritusConfidentialTable(tag: Tag) extends ROsasuoritusTable(tag, Confidential)
  class ROsasuoritusConfidentialTableTemp(tag: Tag) extends ROsasuoritusTable(tag, TempConfidential)

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
  class MuuAmmatillinenOsasuoritusRaportointiConfidentialTable(tag: Tag) extends MuuAmmatillinenOsasuoritusRaportointiTable(tag, Confidential)
  class MuuAmmatillinenOsasuoritusRaportointiConfidentialTableTemp(tag: Tag) extends MuuAmmatillinenOsasuoritusRaportointiTable(tag, TempConfidential)

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
  class TOPKSAmmatillinenOsasuoritusRaportointiConfidentialTable(tag: Tag) extends TOPKSAmmatillinenOsasuoritusRaportointiTable(tag, Confidential)
  class TOPKSAmmatillinenOsasuoritusRaportointiConfidentialTableTemp(tag: Tag) extends TOPKSAmmatillinenOsasuoritusRaportointiTable(tag, TempConfidential)

  class RHenkilöTable(tag: Tag, schema: Schema = Public) extends Table[RHenkilöRow](tag, schema.nameOpt, "r_henkilo") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey, StringIdentifierType)
    val masterOid = column[String]("master_oid", StringIdentifierType)
    val linkitetytOidit = column[List[String]]("linkitetyt_oidit")
    val hetu = column[Option[String]]("hetu", StringIdentifierType)
    val sukupuoli = column[Option[String]]("sukupuoli")
    val syntymäaika = column[Option[Date]]("syntymaaika")
    val sukunimi = column[String]("sukunimi")
    val etunimet = column[String]("etunimet")
    val äidinkieli = column[Option[String]]("aidinkieli", StringIdentifierType)
    val kansalaisuus = column[Option[String]]("kansalaisuus", StringIdentifierType)
    val turvakielto = column[Boolean]("turvakielto")
    val kotikunta = column[Option[String]]("kotikunta")
    val kotikuntaNimiFi = column[Option[String]]("kotikunta_nimi_fi")
    val kotikuntaNimiSv = column[Option[String]]("kotikunta_nimi_sv")
    val yksiloity = column[Boolean]("yksiloity")
    def * = (oppijaOid, masterOid, linkitetytOidit, hetu, sukupuoli, syntymäaika, sukunimi, etunimet, äidinkieli, kansalaisuus, turvakielto, kotikunta, kotikuntaNimiFi, kotikuntaNimiSv, yksiloity) <> (RHenkilöRow.tupled, RHenkilöRow.unapply)
  }
  class RHenkilöTableTemp(tag: Tag) extends RHenkilöTable(tag, Temp)
  class RHenkilöConfidentialTable(tag: Tag) extends RHenkilöTable(tag, Confidential)
  class RHenkilöConfidentialTableTemp(tag: Tag) extends RHenkilöTable(tag, TempConfidential)

  class ROrganisaatioTable(tag: Tag, schema: Schema = Public) extends Table[ROrganisaatioRow](tag, schema.nameOpt, "r_organisaatio") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey, StringIdentifierType)
    val nimi = column[String]("nimi")
    val nimiSv = column[String]("nimi_sv")
    val organisaatiotyypit = column[String]("organisaatiotyypit", StringIdentifierType)
    val oppilaitostyyppi = column[Option[String]]("oppilaitostyyppi", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)
    val kotipaikka = column[Option[String]]("kotipaikka", StringIdentifierType)
    val yTunnus = column[Option[String]]("y_tunnus", StringIdentifierType)
    val koulutustoimija = column[Option[String]]("koulutustoimija", StringIdentifierType)
    val oppilaitos = column[Option[String]]("oppilaitos", StringIdentifierType)
    def * = (organisaatioOid, nimi, nimiSv, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero, kotipaikka, yTunnus, koulutustoimija, oppilaitos) <> (ROrganisaatioRow.tupled, ROrganisaatioRow.unapply)
  }

  class ROrganisaatioTableTemp(tag: Tag) extends ROrganisaatioTable(tag, Temp)
  class ROrganisaatioConfidentialTable(tag: Tag) extends ROrganisaatioTable(tag, Confidential)
  class ROrganisaatioConfidentialTableTemp(tag: Tag) extends ROrganisaatioTable(tag, TempConfidential)

  class ROrganisaatioKieliTable(tag: Tag, schema: Schema = Public) extends Table[ROrganisaatioKieliRow](tag, schema.nameOpt, "r_organisaatio_kieli") {
    val organisaatioOid = column[String]("organisaatio_oid", StringIdentifierType)
    val kielikoodi = column[String]("kielikoodi", StringIdentifierType)
    def * = (organisaatioOid, kielikoodi) <> (ROrganisaatioKieliRow.tupled, ROrganisaatioKieliRow.unapply)
  }

  class ROrganisaatioKieliTableTemp(tag: Tag) extends ROrganisaatioKieliTable(tag, Temp)
  class ROrganisaatioKieliConfidentialTable(tag: Tag) extends ROrganisaatioKieliTable(tag, Confidential)
  class ROrganisaatioKieliConfidentialTableTemp(tag: Tag) extends ROrganisaatioKieliTable(tag, TempConfidential)

  class RKoodistoKoodiTable(tag: Tag, schema: Schema = Public) extends Table[RKoodistoKoodiRow](tag, schema.nameOpt, "r_koodisto_koodi") {
    val koodistoUri = column[String]("koodisto_uri", StringIdentifierType)
    val koodiarvo = column[String]("koodiarvo", StringIdentifierType)
    val nimi = column[String]("nimi")
    val nimiSv = column[String]("nimi_sv")
    def * = (koodistoUri, koodiarvo, nimi, nimiSv) <> (RKoodistoKoodiRow.tupled, RKoodistoKoodiRow.unapply)
  }
  class RKoodistoKoodiTableTemp(tag: Tag) extends RKoodistoKoodiTable(tag, Temp)
  class RKoodistoKoodiConfidentialTable(tag: Tag) extends RKoodistoKoodiTable(tag, Confidential)
  class RKoodistoKoodiConfidentialTableTemp(tag: Tag) extends RKoodistoKoodiTable(tag, TempConfidential)

  class RaportointikantaStatusTable(tag: Tag, schema: Schema = Public) extends Table[RaportointikantaStatusRow](tag, schema.nameOpt, "raportointikanta_status") {
    val name = column[String]("name", O.PrimaryKey)
    val count = column[Int]("count", O.Default(0))
    val lastUpdate = column[Timestamp]("last_update", O.SqlType("timestamp default now()"))
    val loadStarted = column[Option[Timestamp]]("load_started", O.Default(None))
    val loadCompleted = column[Option[Timestamp]]("load_completed", O.Default(None))
    val dueTime = column[Option[Timestamp]]("due_time", O.Default(None))
    def * = (name, count, lastUpdate, loadStarted, loadCompleted, dueTime) <> (RaportointikantaStatusRow.tupled, RaportointikantaStatusRow.unapply)
  }
  class RaportointikantaStatusTableTemp(tag: Tag) extends RaportointikantaStatusTable(tag, Temp)
  class RaportointikantaStatusConfidentialTable(tag: Tag) extends RaportointikantaStatusTable(tag, Confidential)
  class RaportointikantaStatusConfidentialTableTemp(tag: Tag) extends RaportointikantaStatusTable(tag, TempConfidential)

  class ROppivelvollisuudestaVapautusTable(tag: Tag, schema: Schema = Public) extends Table[ROppivelvollisuudestaVapautusRow](tag, schema.nameOpt, "r_oppivelvollisuudesta_vapautus") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey)
    val vapautettu = column[Timestamp]("vapautettu")
    def * = (oppijaOid, vapautettu) <> (ROppivelvollisuudestaVapautusRow.tupled, ROppivelvollisuudestaVapautusRow.unapply)
  }
  class ROppivelvollisuudestaVapautusTableTemp(tag: Tag) extends ROppivelvollisuudestaVapautusTable(tag, Temp)
  class ROppivelvollisuudestaVapautusConfidentialTable(tag: Tag) extends ROppivelvollisuudestaVapautusTable(tag, Confidential)
  class ROppivelvollisuudestaVapautusConfidentialTableTemp(tag: Tag) extends ROppivelvollisuudestaVapautusTable(tag, TempConfidential)

  class RYtrTutkintokokonaisuudenSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RYtrTutkintokokonaisuudenSuoritusRow](tag, schema.nameOpt, "r_ytr_tutkintokokonaisuuden_suoritus") {
    val ytrTutkintokokonaisuudenSuoritusId = column[Long]("ytr_tutkintokokonaisuuden_suoritus_id", O.PrimaryKey)

    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)

    val tyyppiKoodiarvo = column[Option[String]]("tyyppi_koodiarvo", StringIdentifierType)
    val tilaKoodiarvo = column[Option[String]]("tila_koodiarvo", StringIdentifierType)
    val suorituskieliKoodiarvo = column[Option[String]]("suorituskieli_koodiarvo", StringIdentifierType)

    val hyväksytystiValmistunutTutkinto = column[Option[Boolean]]("hyvaksytysti_valmistunut_tutkinto")

    val data = column[JValue]("data")

    def * = (
      ytrTutkintokokonaisuudenSuoritusId,
      päätasonSuoritusId,
      opiskeluoikeusOid,
      tyyppiKoodiarvo,
      tilaKoodiarvo,
      suorituskieliKoodiarvo,
      hyväksytystiValmistunutTutkinto,
      data
    ) <> (RYtrTutkintokokonaisuudenSuoritusRow.tupled, RYtrTutkintokokonaisuudenSuoritusRow.unapply)
  }
  class RYtrTutkintokokonaisuudenSuoritusTableTemp(tag: Tag) extends RYtrTutkintokokonaisuudenSuoritusTable(tag, Temp)
  class RYtrTutkintokokonaisuudenSuoritusConfidentialTable(tag: Tag) extends RYtrTutkintokokonaisuudenSuoritusTable(tag, Confidential)
  class RYtrTutkintokokonaisuudenSuoritusConfidentialTableTemp(tag: Tag) extends RYtrTutkintokokonaisuudenSuoritusTable(tag, TempConfidential)

  class RYtrTutkintokerranSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RYtrTutkintokerranSuoritusRow](tag, schema.nameOpt, "r_ytr_tutkintokerran_suoritus") {
    val ytrTutkintokerranSuoritusId = column[Long]("ytr_tutkintokerran_suoritus_id", O.PrimaryKey)

    val ytrTutkintokokonaisuudenSuoritusId = column[Long]("ytr_tutkintokokonaisuuden_suoritus_id")
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)

    val tutkintokertaKoodiarvo = column[String]("tutkintokerta_koodiarvo", StringIdentifierType)
    val vuosi = column[Int]("vuosi")
    val vuodenaikaKoodiarvo = column[String]("vuodenaika_koodiarvo", StringIdentifierType)
    val koulutustaustaKoodiarvo = column[Option[String]]("koulutustausta_koodiarvo", StringIdentifierType)
    val oppilaitosOid = column[Option[String]]("oppilaitos_oid", StringIdentifierType)
    val oppilaitosNimi = column[Option[String]]("oppilaitos_nimi")
    val oppilaitosNimiSv = column[Option[String]]("oppilaitos_nimi_sv")
    val oppilaitosKotipaikka = column[Option[String]]("oppilaitos_kotipaikka", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)

    val data = column[JValue]("data")

    def * = (
      ytrTutkintokerranSuoritusId,
      ytrTutkintokokonaisuudenSuoritusId,
      päätasonSuoritusId,
      opiskeluoikeusOid,
      tutkintokertaKoodiarvo,
      vuosi,
      vuodenaikaKoodiarvo,
      koulutustaustaKoodiarvo,
      oppilaitosOid,
      oppilaitosNimi,
      oppilaitosNimiSv,
      oppilaitosKotipaikka,
      oppilaitosnumero,
      data
    ) <> (RYtrTutkintokerranSuoritusRow.tupled, RYtrTutkintokerranSuoritusRow.unapply)
  }
  class RYtrTutkintokerranSuoritusTableTemp(tag: Tag) extends RYtrTutkintokerranSuoritusTable(tag, Temp)
  class RYtrTutkintokerranSuoritusConfidentialTable(tag: Tag) extends RYtrTutkintokerranSuoritusTable(tag, Confidential)
  class RYtrTutkintokerranSuoritusConfidentialTableTemp(tag: Tag) extends RYtrTutkintokerranSuoritusTable(tag, TempConfidential)

  class RYtrKokeenSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RYtrKokeenSuoritusRow](tag, schema.nameOpt, "r_ytr_kokeen_suoritus") {
    val ytrKokeenSuoritusId = column[Long]("ytr_kokeen_suoritus_id", O.PrimaryKey)

    val ytrTutkintokerranSuoritusId = column[Long]("ytr_tutkintokerran_suoritus_id")
    val ytrTutkintokokonaisuudenSuoritusId = column[Long]("ytr_tutkintokokonaisuuden_suoritus_id")
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)

    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliNimi = column[Option[String]]("koulutusmoduuli_nimi")
    val arviointiArvosanaKoodiarvo = column[Option[String]]("arviointi_arvosana_koodiarvo", StringIdentifierType)
    val arviointiArvosanaKoodisto = column[Option[String]]("arviointi_arvosana_koodisto", StringIdentifierType)
    val arviointiHyväksytty = column[Option[Boolean]]("arviointi_hyvaksytty")

    val arviointiPisteet = column[Option[Int]]("arviointi_pisteet")
    val keskeytynyt = column[Option[Boolean]]("keskeytynyt")
    val maksuton = column[Option[Boolean]]("maksuton")

    val data = column[JValue]("data")

    def * = (
      ytrKokeenSuoritusId,
      ytrTutkintokerranSuoritusId,
      ytrTutkintokokonaisuudenSuoritusId,
      päätasonSuoritusId,
      opiskeluoikeusOid,
      suorituksenTyyppi,
      koulutusmoduuliKoodisto,
      koulutusmoduuliKoodiarvo,
      koulutusmoduuliNimi,
      arviointiArvosanaKoodiarvo,
      arviointiArvosanaKoodisto,
      arviointiHyväksytty,
      arviointiPisteet,
      keskeytynyt,
      maksuton,
      data
    ) <> (RYtrKokeenSuoritusRow.tupled, RYtrKokeenSuoritusRow.unapply)
  }
  class RYtrKokeenSuoritusTableTemp(tag: Tag) extends RYtrKokeenSuoritusTable(tag, Temp)
  class RYtrKokeenSuoritusConfidentialTable(tag: Tag) extends RYtrKokeenSuoritusTable(tag, Confidential)
  class RYtrKokeenSuoritusConfidentialTableTemp(tag: Tag) extends RYtrKokeenSuoritusTable(tag, TempConfidential)

  class RYtrTutkintokokonaisuudenKokeenSuoritusTable(tag: Tag, schema: Schema = Public) extends Table[RYtrTutkintokokonaisuudenKokeenSuoritusRow](tag, schema.nameOpt, "r_ytr_tutkintokokonaisuuden_kokeen_suoritus") {
    val ytrTutkintokokonaisuudenSuoritusId = column[Long]("ytr_tutkintokokonaisuuden_suoritus_id")
    val ytrKokeenSuoritusId = column[Long]("ytr_kokeen_suoritus_id")

    val ytrTutkintokerranSuoritusId = column[Long]("ytr_tutkintokerran_suoritus_id")

    val sisällytetty = column[Boolean]("sisallytetty")

    def * = (
      ytrTutkintokokonaisuudenSuoritusId,
      ytrKokeenSuoritusId,
      ytrTutkintokerranSuoritusId,
      sisällytetty
    ) <> (RYtrTutkintokokonaisuudenKokeenSuoritusRow.tupled, RYtrTutkintokokonaisuudenKokeenSuoritusRow.unapply)
    def pk = primaryKey("r_ytr_tutkintokokonaisuuden_kokeen_suoritus_pk", (ytrTutkintokokonaisuudenSuoritusId, ytrKokeenSuoritusId))
  }
  class RYtrTutkintokokonaisuudenKokeenSuoritusTableTemp(tag: Tag) extends RYtrTutkintokokonaisuudenKokeenSuoritusTable(tag, Temp)
  class RYtrTutkintokokonaisuudenKokeenSuoritusConfidentialTable(tag: Tag) extends RYtrTutkintokokonaisuudenKokeenSuoritusTable(tag, Confidential)
  class RYtrTutkintokokonaisuudenKokeenSuoritusConfidentialTableTemp(tag: Tag) extends RYtrTutkintokokonaisuudenKokeenSuoritusTable(tag, TempConfidential)

  class RKotikuntahistoriaTable(tag: Tag, schema: Schema = Public) extends Table[RKotikuntahistoriaRow](tag, schema.nameOpt, "r_kotikuntahistoria") {
    val masterOppijaOid = column[String]("master_oid")
    val kotikunta = column[String]("kotikunta")
    val muuttoPvm = column[Option[Date]]("muutto_pvm")
    val poismuuttoPvm = column[Option[Date]]("poismuutto_pvm")
    val turvakielto = column[Boolean]("turvakielto")

    def * = (
      masterOppijaOid,
      kotikunta,
      muuttoPvm,
      poismuuttoPvm,
      turvakielto,
    ) <> (RKotikuntahistoriaRow.tupled, RKotikuntahistoriaRow.unapply)
  }
  class RKotikuntahistoriaTableTemp(tag: Tag) extends RKotikuntahistoriaTable(tag, Temp)
  class RKotikuntahistoriaConfidentialTable(tag: Tag) extends RKotikuntahistoriaTable(tag, Confidential)
  class RKotikuntahistoriaConfidentialTableTemp(tag: Tag) extends RKotikuntahistoriaTable(tag, TempConfidential)
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
  oppilaitosNimiSv: String,
  oppilaitosKotipaikka: Option[String],
  oppilaitosnumero: Option[String],
  koulutustoimijaOid: String,
  koulutustoimijaNimi: String,
  koulutustoimijaNimiSv: String,
  koulutusmuoto: String,
  alkamispäivä: Option[Date],
  päättymispäivä: Option[Date],
  viimeisinTila: Option[String],
  lisätiedotHenkilöstökoulutus: Boolean,
  lisätiedotKoulutusvienti: Boolean,
  tuvaJärjestämislupa: Option[String],
  lähdejärjestelmäKoodiarvo: Option[String],
  lähdejärjestelmäId: Option[String],
  oppivelvollisuudenSuorittamiseenKelpaava: Boolean,
  data: JValue
)

case class RMitätöityOpiskeluoikeusRow(
  opiskeluoikeusOid: String,
  versionumero: Int,
  aikaleima: Timestamp,
  oppijaOid: String,
  mitätöity: Option[LocalDate],
  suostumusPeruttu: Option[LocalDate],
  tyyppi: String,
  päätasonSuoritusTyypit: List[String],
  oppilaitosOid: Option[String],
  oppilaitoksenNimi: Option[String],
  koulutustoimijaOid: Option[String],
  koulutustoimijanNimi: Option[String],
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
  maksuton: Boolean = false,
  maksullinen: Boolean = false,
  oikeuttaMaksuttomuuteenPidennetty: Boolean = false,
  kotiopetus: Boolean = false,
  ulkomaanjakso: Boolean = false,
  id: Long = 0
) extends AikajaksoRow[ROpiskeluoikeusAikajaksoRow] {
  def truncateToDates(start: Date, end: Date): ROpiskeluoikeusAikajaksoRow = this.copy(
    alku = if (alku.after(start)) alku else start,
    loppu = if (loppu.before(end)) loppu else end
  )
  def truncateToDates(start: LocalDate, end: LocalDate): ROpiskeluoikeusAikajaksoRow =
    this.truncateToDates(Date.valueOf(start), Date.valueOf(end))
  def lengthInDays: Int = ChronoUnit.DAYS.between(alku.toLocalDate, loppu.toLocalDate).toInt + 1

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
  def matchesWith(x: YleissivistäväRaporttiOppiaineTaiKurssi, lang: String): Boolean
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
  tutkinnonNimiPerusteessa: Option[String],
  suorituskieliKoodiarvo: Option[String],
  oppimääräKoodiarvo: Option[String],
  alkamispäivä: Option[Date],
  vahvistusPäivä: Option[Date],
  arviointiArvosanaKoodiarvo: Option[String],
  arviointiArvosanaKoodisto: Option[String],
  arviointiHyväksytty: Option[Boolean],
  arviointiPäivä: Option[Date],
  toimipisteOid: String,
  toimipisteNimi: String,
  toimipisteNimiSv: String,
  data: JValue,
  sisältyyOpiskeluoikeuteenOid: Option[String]
) extends RSuoritusRow {
  override def matchesWith(x: YleissivistäväRaporttiOppiaineTaiKurssi, lang: String): Boolean = {
    val isPaikallinen = !koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava")

    suorituksestaKäytettäväNimi(lang).contains(x.nimi) &&
      koulutusmoduuliKoodiarvo == x.koulutusmoduuliKoodiarvo &&
      isPaikallinen == x.koulutusmoduuliPaikallinen
  }

  def oppimääräKoodiarvoDatasta: Option[String] =
    JsonSerializer.extract[Option[String]](data \ "koulutusmoduuli" \ "oppimäärä" \ "koodiarvo")
      .orElse(oppimääräKoodiarvo)

  def koulutusModuulistaKäytettäväNimi(lang: String): Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "tunniste" \ "nimi").map(_.get(lang))
      .orElse(koulutusmoduuliNimi)
  }

  def perusteestaKäytettäväNimi(lang: String): Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "perusteenNimi").map(_.get(lang))
      .orElse(tutkinnonNimiPerusteessa)
  }

  def suorituksestaKäytettäväNimi(lang: String): Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "kieli" \ "nimi").map(_.get(lang))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "oppimäärä" \ "nimi").map(_.get(lang)))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "uskonnonOppimäärä" \ "nimi").map(_.get(lang)))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "tunniste" \ "nimi").map(_.get(lang)))
      .orElse(koulutusmoduuliNimi)
  }

  def koulutusModuulinLaajuusYksikköNimi = JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "laajuus" \ "yksikkö" \ "nimi")
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
  ensimmäinenArviointiPäivä: Option[Date],
  korotettuEriVuonna: Boolean,
  näytönArviointiPäivä: Option[Date],
  tunnustettu: Boolean,
  tunnustettuRahoituksenPiirissä: Boolean,
  data: JValue,
  sisältyyOpiskeluoikeuteenOid: Option[String]
) extends RSuoritusRow {
  override def matchesWith(x: YleissivistäväRaporttiOppiaineTaiKurssi, lang: String): Boolean = {
    x match {
      case _: YleissivistäväRaporttiOppiaine => suorituksestaKäytettäväNimi(lang).contains(x.nimi) &&
        koulutusmoduuliKoodiarvo == x.koulutusmoduuliKoodiarvo &&
        koulutusmoduuliPaikallinen == x.koulutusmoduuliPaikallinen
      case _: YleissivistäväRaporttiKurssi => koulutusModuulistaKäytettäväNimi(lang).contains(x.nimi) &&
        koulutusmoduuliKoodiarvo == x.koulutusmoduuliKoodiarvo &&
        koulutusmoduuliPaikallinen == x.koulutusmoduuliPaikallinen
    }
  }

  def oppimääräKoodiarvo: Option[String] =
    JsonSerializer.extract[Option[String]](data \ "koulutusmoduuli" \ "oppimäärä" \ "koodiarvo")

  def koulutusModuulistaKäytettäväNimi(lang: String): Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "tunniste" \ "nimi").map(_.get(lang))
      .orElse(koulutusmoduuliNimi)
  }

  def suorituksestaKäytettäväNimi(lang: String): Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "kieli" \ "nimi").map(_.get(lang))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "oppimäärä" \ "nimi").map(_.get(lang)))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "uskonnonOppimäärä" \ "nimi").map(_.get(lang)))
      .orElse(JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "tunniste" \ "nimi").map(_.get(lang)))
      .orElse(koulutusmoduuliNimi)
  }

  def laajuus: BigDecimal = koulutusmoduuliLaajuusArvo.map(decimal).getOrElse(decimal(1.0))

  def koulutusModuulinLaajuusYksikköNimi = JsonSerializer.extract[Option[LocalizedString]](data \ "koulutusmoduuli" \ "laajuus" \ "yksikkö" \ "nimi")

  def luokkaAsteNimi: Option[LocalizedString] = JsonSerializer.extract[Option[LocalizedString]](data \ "luokkaAste" \ "nimi")
}

case class RHenkilöRow(
  oppijaOid: String,
  masterOid: String,
  linkitetytOidit: List[String],
  hetu: Option[String],
  sukupuoli: Option[String],
  syntymäaika: Option[Date],
  sukunimi: String,
  etunimet: String,
  aidinkieli: Option[String],
  kansalaisuus: Option[String],
  turvakielto: Boolean,
  kotikunta: Option[String] = None,
  kotikuntaNimiFi: Option[String] = None,
  kotikuntaNimiSv: Option[String] = None,
  yksiloity: Boolean
)

case class ROrganisaatioRow(
  organisaatioOid: String,
  nimi: String,
  nimiSv: String,
  organisaatiotyypit: String,
  oppilaitostyyppi: Option[String],
  oppilaitosnumero: Option[String],
  kotipaikka: Option[String],
  yTunnus: Option[String],
  koulutustoimija: Option[String],
  oppilaitos: Option[String],
)

case class ROrganisaatioKieliRow(
  organisaatioOid: String,
  kielikoodi: String
)

case class RKoodistoKoodiRow(
  koodistoUri: String,
  koodiarvo: String,
  nimi: String,
  nimiSv: String
)

case class RaportointikantaStatusRow(
  name: String,
  count: Int,
  lastUpdate: Timestamp,
  loadStarted: Option[Timestamp],
  loadCompleted: Option[Timestamp],
  dueTime: Option[Timestamp],
)

case class ROppivelvollisuudestaVapautusRow(
  oppijaOid: String,
  vapautettu: Timestamp,
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

case class RYtrTutkintokokonaisuudenSuoritusRow(
  ytrTutkintokokonaisuudenSuoritusId: Long,

  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,

  tyyppiKoodiarvo: Option[String],
  tilaKoodiarvo: Option[String],
  suorituskieliKoodiarvo: Option[String],

  // Jos tyyppi = candidate ja tila = graduated , tähän tallennetaan true.
  hyväksytystiValmistunutTutkinto: Option[Boolean],

  data: JValue
)

case class RYtrTutkintokerranSuoritusRow(
  ytrTutkintokerranSuoritusId: Long,

  ytrTutkintokokonaisuudenSuoritusId: Long,
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,

  tutkintokertaKoodiarvo: String,
  vuosi: Int,
  vuodenaikaKoodiarvo: String,
  koulutustaustaKoodiarvo: Option[String],
  oppilaitosOid: Option[String],
  oppilaitosNimi: Option[String],
  oppilaitosNimiSv: Option[String],
  oppilaitosKotipaikka: Option[String],
  oppilaitosnumero: Option[String],

  data: JValue
)

case class RYtrKokeenSuoritusRow(
  ytrKokeenSuoritusId: Long,

  ytrTutkintokerranSuoritusId: Long,
  ytrTutkintokokonaisuudenSuoritusId: Long,
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,

  // Jaetut kentät ROsasuoritusRow kanssa
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliNimi: Option[String],
  arviointiArvosanaKoodiarvo: Option[String],
  arviointiArvosanaKoodisto: Option[String],
  arviointiHyväksytty: Option[Boolean],

  // YTR-spesifit kentät
  arviointiPisteet: Option[Int],
  keskeytynyt: Option[Boolean],
  maksuton: Option[Boolean],

  data: JValue
)

case class RYtrTutkintokokonaisuudenKokeenSuoritusRow(
  ytrTutkintokokonaisuudenSuoritusId: Long,
  ytrKokeenSuoritusId: Long,

  ytrTutkintokerranSuoritusId: Long,

  // Tulevaisuutta varten: uuden lain myötä kokeita voi sisällyttää aiemmista tutkintokokonaisuuksista
  sisällytetty: Boolean
)

case class RKotikuntahistoriaRow(
  masterOppijaOid: String,
  kotikunta: String,
  muuttoPvm: Option[Date],
  poismuuttoPvm: Option[Date],
  turvakielto: Boolean,
)

sealed trait Schema {
  def nameOpt: Option[String] = Some(name)
  def name: String

  def moveSchema(newSchema: Schema) = DBIO.seq(
    sqlu"DROP SCHEMA IF EXISTS #${newSchema.name} CASCADE",
    sqlu"ALTER SCHEMA #${name} RENAME TO #${newSchema.name}"
  )

  def recreateSchema() =
    DBIO.seq(
      sqlu"DROP SCHEMA IF EXISTS #${name} CASCADE",
      sqlu"CREATE SCHEMA #${name}"
    )

  def dropSchema() =
    sqlu"DROP SCHEMA IF EXISTS #$name CASCADE"

  // Laita tähän vain ne indeksit, jotka tarvitaan inkrementaalisen generoinnin nopeuttamiseksi.
  def createIndexesForIncrementalUpdate() = DBIO.seq(
    sqlu"CREATE INDEX ON #${name}.r_osasuoritus(opiskeluoikeus_oid)",
  )

  def createOpiskeluoikeusIndexes() = DBIO.seq(
    sqlu"CREATE UNIQUE INDEX ON #${name}.r_opiskeluoikeus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus(oppija_oid)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus(oppilaitos_oid, koulutusmuoto)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus(koulutusmuoto)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus(sisaltyy_opiskeluoikeuteen_oid)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus(opiskeluoikeus_oid, koulutusmuoto, oppija_oid)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus USING HASH ((data->'järjestämismuoto'->>'koodiarvo'))",

    sqlu"CREATE INDEX ON #${name}.r_organisaatiohistoria(opiskeluoikeus_oid, loppu, alku, oppilaitos_oid, koulutustoimija_oid)",
    sqlu"CREATE INDEX ON #${name}.r_organisaatiohistoria(oppilaitos_oid, loppu, alku, opiskeluoikeus_oid)",

    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus_aikajakso(opiskeluoikeus_oid, loppu, alku, tila)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus_aikajakso(loppu, alku, opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON #${name}.r_opiskeluoikeus_aikajakso(oikeutta_maksuttomuuteen_pidennetty)",

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_paatason_suoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_paatason_suoritus(opiskeluoikeus_oid, suorituksen_tyyppi, koulutusmoduuli_koulutustyyppi)",
    sqlu"CREATE INDEX ON #${name}.r_paatason_suoritus(suorituksen_tyyppi)",
    sqlu"CREATE INDEX ON #${name}.r_paatason_suoritus(vahvistus_paiva)",

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_osasuoritus(osasuoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_osasuoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_osasuoritus(ylempi_osasuoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_osasuoritus(paatason_suoritus_id, suorituksen_tyyppi, arviointi_paiva)",
    sqlu"CREATE INDEX ON #${name}.r_osasuoritus(sisaltyy_opiskeluoikeuteen_oid)",

    sqlu"CREATE INDEX ON #${name}.esiopetus_opiskeluoik_aikajakso(opiskeluoikeus_oid)",

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_mitatoitu_opiskeluoikeus(opiskeluoikeus_oid)",

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_ytr_tutkintokokonaisuuden_suoritus(ytr_tutkintokokonaisuuden_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokokonaisuuden_suoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokokonaisuuden_suoritus(hyvaksytysti_valmistunut_tutkinto)", // TODO: Tarvitaanko tämä?

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_ytr_tutkintokerran_suoritus(ytr_tutkintokerran_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokerran_suoritus(ytr_tutkintokokonaisuuden_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokerran_suoritus(paatason_suoritus_id)",

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_ytr_kokeen_suoritus(ytr_kokeen_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_kokeen_suoritus(ytr_tutkintokerran_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_kokeen_suoritus(ytr_tutkintokokonaisuuden_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_kokeen_suoritus(paatason_suoritus_id)",

    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokokonaisuuden_kokeen_suoritus(ytr_tutkintokokonaisuuden_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokokonaisuuden_kokeen_suoritus(ytr_kokeen_suoritus_id)",
    sqlu"CREATE INDEX ON #${name}.r_ytr_tutkintokokonaisuuden_kokeen_suoritus(ytr_tutkintokerran_suoritus_id)",
  )

  def createOtherIndexes() = DBIO.seq(
    sqlu"CREATE INDEX ON #${name}.r_henkilo(hetu)",
    sqlu"CREATE INDEX ON #${name}.r_henkilo(oppija_oid, aidinkieli)",
    sqlu"CREATE INDEX ON #${name}.r_henkilo(linkitetyt_oidit)",
    sqlu"CREATE INDEX ON #${name}.r_henkilo(master_oid)",

    sqlu"CREATE INDEX ON #${name}.r_organisaatio(oppilaitosnumero)",

    sqlu"CREATE UNIQUE INDEX ON #${name}.r_koodisto_koodi(koodisto_uri, koodiarvo)",

    Kotikuntahistoria.createIndexes(this),
  )

  def grantPermissions() = DBIO.seq(actions =
    sqlu"GRANT USAGE ON SCHEMA #${name} TO raportointikanta_katselija, raportointikanta_henkilo_katselija",
    sqlu"""GRANT SELECT ON
      #${name}.r_opiskeluoikeus,
      #${name}.r_organisaatiohistoria,
      #${name}.r_opiskeluoikeus_aikajakso,
      #${name}.r_paatason_suoritus,
      #${name}.r_osasuoritus,
      #${name}.r_organisaatio,
      #${name}.r_organisaatio_kieli,
      #${name}.r_koodisto_koodi,
      #${name}.raportointikanta_status
      TO raportointikanta_katselija, raportointikanta_henkilo_katselija""",
    sqlu"""GRANT SELECT ON
      #${name}.r_henkilo,
      #${name}.esiopetus_opiskeluoik_aikajakso,
      #${name}.muu_ammatillinen_raportointi,
      #${name}.topks_ammatillinen_raportointi,
      #${name}.r_ytr_tutkintokokonaisuuden_suoritus,
      #${name}.r_ytr_tutkintokerran_suoritus,
      #${name}.r_ytr_kokeen_suoritus,
      #${name}.r_ytr_tutkintokokonaisuuden_kokeen_suoritus
      TO raportointikanta_henkilo_katselija"""
  )
}

case object Public extends Schema {
  def name: String = "public"
}

case object Temp extends Schema {
  def name: String = "etl"
}

sealed trait ConfidentialSchema extends Schema {
  override def createIndexesForIncrementalUpdate() = DBIO.seq()
  override def createOpiskeluoikeusIndexes() = DBIO.seq()
  override def createOtherIndexes() = DBIO.seq(
    Kotikuntahistoria.createIndexes(this),
  )
  override def grantPermissions() = DBIO.seq()
}

case object Confidential extends ConfidentialSchema {
  def name: String = "confidential"
}

case object TempConfidential extends ConfidentialSchema {
  def name: String = "etl_confidential"
}
