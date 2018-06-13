package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.json4s.JValue
import slick.dbio.DBIO
import slick.sql.SqlProfile.ColumnOption.SqlType

object RaportointiDatabaseSchema {

  val createOpiskeluoikeusIndexes = DBIO.seq(
    sqlu"CREATE UNIQUE INDEX ON r_opiskeluoikeus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppija_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppilaitos_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(koulutusmuoto)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus_aikajakso(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus_aikajakso(alku)",
    sqlu"CREATE UNIQUE INDEX ON r_paatason_suoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON r_paatason_suoritus(opiskeluoikeus_oid)",
    sqlu"CREATE UNIQUE INDEX ON r_osasuoritus(osasuoritus_id)",
    sqlu"CREATE INDEX ON r_osasuoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON r_osasuoritus(opiskeluoikeus_oid)"
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
    sqlu"DROP TABLE IF EXISTS r_koodisto_koodi"
  )

  private val StringIdentifierType = SqlType("character varying collate \"C\"")

  class ROpiskeluoikeusTable(tag: Tag) extends Table[ROpiskeluoikeusRow](tag, "r_opiskeluoikeus") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
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
    def * = (opiskeluoikeusOid, versionumero, aikaleima, sisältyyOpiskeluoikeuteenOid, oppijaOid,
      oppilaitosOid, oppilaitosNimi, oppilaitosKotipaikka, oppilaitosnumero, koulutustoimijaOid, koulutustoimijaNimi,
      koulutusmuoto, alkamispäivä, päättymispäivä, viimeisinTila,
      lisätiedotHenkilöstökoulutus, lisätiedotKoulutusvienti) <> (ROpiskeluoikeusRow.tupled, ROpiskeluoikeusRow.unapply)
  }

  class ROpiskeluoikeusAikajaksoTable(tag: Tag) extends Table[ROpiskeluoikeusAikajaksoRow](tag, "r_opiskeluoikeus_aikajakso") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val alku = column[Date]("alku")
    val loppu = column[Date]("loppu")
    val tila = column[String]("tila", StringIdentifierType)
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
    def * = (opiskeluoikeusOid, alku, loppu, tila, opiskeluoikeusPäättynyt,
      opintojenRahoitus, majoitus, sisäoppilaitosmainenMajoitus, vaativanErityisenTuenYhteydessäJärjestettäväMajoitus,
      erityinenTuki, vaativanErityisenTuenErityinenTehtävä, hojks, vaikeastiVammainen, vammainenJaAvustaja,
      osaAikaisuus, opiskeluvalmiuksiaTukevatOpinnot, vankilaopetuksessa, oppisopimusJossainPäätasonSuorituksessa) <> (ROpiskeluoikeusAikajaksoRow.tupled, ROpiskeluoikeusAikajaksoRow.unapply)
  }

  class RPäätasonSuoritusTable(tag: Tag) extends Table[RPäätasonSuoritusRow](tag, "r_paatason_suoritus") {
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliKoulutustyyppi = column[Option[String]]("koulutusmoduuli_koulutustyyppi", StringIdentifierType)
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val toimipisteOid = column[String]("toimipiste_oid", StringIdentifierType)
    val toimipisteNimi = column[String]("toimipiste_nimi")
    val data = column[JValue]("data")
    def * = (päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliKoulutustyyppi,
      vahvistusPäivä, toimipisteOid, toimipisteNimi, data) <> (RPäätasonSuoritusRow.tupled, RPäätasonSuoritusRow.unapply)
  }

  class ROsasuoritusTable(tag: Tag) extends Table[ROsasuoritusRow](tag, "r_osasuoritus") {
    val osasuoritusId = column[Long]("osasuoritus_id")
    val ylempiOsasuoritusId = column[Option[Long]]("ylempi_osasuoritus_id")
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", StringIdentifierType)
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi", StringIdentifierType)
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto", StringIdentifierType)
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo", StringIdentifierType)
    val koulutusmoduuliPaikallinen = column[Boolean]("koulutusmoduuli_paikallinen")
    val koulutusmoduuliPakollinen = column[Option[Boolean]]("koulutusmoduuli_pakollinen")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val data = column[JValue]("data")
    def * = (osasuoritusId, ylempiOsasuoritusId, päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliPaikallinen, koulutusmoduuliPakollinen,
      vahvistusPäivä, data) <> (ROsasuoritusRow.tupled, ROsasuoritusRow.unapply)
  }

  class RHenkilöTable(tag: Tag) extends Table[RHenkilöRow](tag, "r_henkilo") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey, StringIdentifierType)
    val hetu = column[Option[String]]("hetu", StringIdentifierType)
    val syntymäaika = column[Option[Date]]("syntymaaika")
    val sukunimi = column[String]("sukunimi")
    val etunimet = column[String]("etunimet")
    val äidinkieli = column[Option[String]]("aidinkieli", StringIdentifierType)
    val kansalaisuus = column[Option[String]]("kansalaisuus", StringIdentifierType)
    val turvakielto = column[Boolean]("turvakielto")
    def * = (oppijaOid, hetu, syntymäaika, sukunimi, etunimet, äidinkieli, kansalaisuus, turvakielto) <> (RHenkilöRow.tupled, RHenkilöRow.unapply)
  }

  class ROrganisaatioTable(tag: Tag) extends Table[ROrganisaatioRow](tag, "r_organisaatio") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey, StringIdentifierType)
    val nimi = column[String]("nimi")
    val organisaatiotyypit = column[String]("organisaatiotyypit", StringIdentifierType)
    val oppilaitostyyppi = column[Option[String]]("oppilaitostyyppi", StringIdentifierType)
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero", StringIdentifierType)
    val kotipaikka = column[Option[String]]("kotipaikka", StringIdentifierType)
    def * = (organisaatioOid, nimi, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero, kotipaikka) <> (ROrganisaatioRow.tupled, ROrganisaatioRow.unapply)
  }

  class RKoodistoKoodiTable(tag: Tag) extends Table[RKoodistoKoodiRow](tag, "r_koodisto_koodi") {
    val koodistoUri = column[String]("koodisto_uri", StringIdentifierType)
    val koodiarvo = column[String]("koodiarvo", StringIdentifierType)
    val nimi = column[String]("nimi")
    def * = (koodistoUri, koodiarvo, nimi) <> (RKoodistoKoodiRow.tupled, RKoodistoKoodiRow.unapply)
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
  lisätiedotKoulutusvienti: Boolean
)

case class ROpiskeluoikeusAikajaksoRow(
  opiskeluoikeusOid: String,
  alku: Date,
  loppu: Date,
  tila: String,
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
  oppisopimusJossainPäätasonSuorituksessa: Byte = 0
)

case class RPäätasonSuoritusRow(
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliKoulutustyyppi: Option[String],
  vahvistusPäivä: Option[Date],
  toimipisteOid: String,
  toimipisteNimi: String,
  data: JValue
)

case class ROsasuoritusRow(
  osasuoritusId: Long,
  ylempiOsasuoritusId: Option[Long],
  päätasonSuoritusId: Long,
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutusmoduuliPaikallinen: Boolean,
  koulutusmoduuliPakollinen: Option[Boolean],
  vahvistusPäivä: Option[Date],
  data: JValue
)

case class RHenkilöRow(
  oppijaOid: String,
  hetu: Option[String],
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
  kotipaikka: Option[String]
)

case class RKoodistoKoodiRow(
  koodistoUri: String,
  koodiarvo: String,
  nimi: String
)
