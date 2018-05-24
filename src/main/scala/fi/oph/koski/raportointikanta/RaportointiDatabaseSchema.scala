package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.json4s.JValue
import slick.dbio.DBIO

object RaportointiDatabaseSchema {

  val createIndexes = DBIO.seq(
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppija_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppilaitos_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(koulutusmuoto)",
    sqlu"CREATE INDEX ON r_opiskeluoikeusjakso(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_paatason_suoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_osasuoritus(paatason_suoritus_id)",
    sqlu"CREATE INDEX ON r_osasuoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_henkilo(hetu)",
    sqlu"CREATE INDEX ON r_organisaatio(oppilaitosnumero)",
    sqlu"CREATE UNIQUE INDEX ON r_koodisto_koodi(koodisto_uri, koodiarvo)"
  )

  val dropAllIfExists = DBIO.seq(
    sqlu"DROP TABLE IF EXISTS r_opiskeluoikeus",
    sqlu"DROP TABLE IF EXISTS r_opiskeluoikeusjakso",
    sqlu"DROP TABLE IF EXISTS r_paatason_suoritus",
    sqlu"DROP TABLE IF EXISTS r_osasuoritus",
    sqlu"DROP TABLE IF EXISTS r_henkilo",
    sqlu"DROP TABLE IF EXISTS r_organisaatio",
    sqlu"DROP TABLE IF EXISTS r_koodisto_koodi"
  )

  class ROpiskeluoikeusTable(tag: Tag) extends Table[ROpiskeluoikeusRow](tag, "r_opiskeluoikeus") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", O.PrimaryKey)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val sisältyyOpiskeluoikeuteenOid = column[Option[String]]("sisaltyy_opiskeluoikeuteen_oid")
    val oppijaOid = column[String]("oppija_oid")
    val oppilaitosOid = column[String]("oppilaitos_oid")
    val oppilaitosNimi = column[String]("oppilaitos_nimi")
    val oppilaitosKotipaikka = column[Option[String]]("oppilaitos_kotipaikka")
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero")
    val koulutustoimijaOid = column[String]("koulutustoimija_oid")
    val koulutustoimijaNimi = column[String]("koulutustoimija_nimi")
    val koulutusmuoto = column[String]("koulutusmuoto")
    val alkamispäivä = column[Option[Date]]("alkamispaiva")
    val päättymispäivä = column[Option[Date]]("paattymispaiva")
    val viimeisinTila = column[Option[String]]("viimeisin_tila")
    val lisätiedotHenkilöstökoulutus = column[Boolean]("lisatiedot_henkilostokoulutus")
    val lisätiedotKoulutusvienti = column[Boolean]("lisatiedot_koulutusvienti")
    def * = (opiskeluoikeusOid, versionumero, aikaleima, sisältyyOpiskeluoikeuteenOid, oppijaOid,
      oppilaitosOid, oppilaitosNimi, oppilaitosKotipaikka, oppilaitosnumero, koulutustoimijaOid, koulutustoimijaNimi,
      koulutusmuoto, alkamispäivä, päättymispäivä, viimeisinTila,
      lisätiedotHenkilöstökoulutus, lisätiedotKoulutusvienti) <> (ROpiskeluoikeusRow.tupled, ROpiskeluoikeusRow.unapply)
  }

  class ROpiskeluoikeusjaksoTable(tag: Tag) extends Table[ROpiskeluoikeusjaksoRow](tag, "r_opiskeluoikeusjakso") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val alku = column[Date]("alku")
    val loppu = column[Option[Date]]("loppu")
    val tila = column[String]("tila")
    val opiskeluoikeusPäättynyt = column[Boolean]("opiskeluoikeus_paattynyt")
    val opintojenRahoitus = column[Option[String]]("opintojen_rahoitus")
    def * = (opiskeluoikeusOid, alku, loppu, tila, opiskeluoikeusPäättynyt, opintojenRahoitus) <> (ROpiskeluoikeusjaksoRow.tupled, ROpiskeluoikeusjaksoRow.unapply)
  }

  class RPäätasonSuoritusTable(tag: Tag) extends Table[RPäätasonSuoritusRow](tag, "r_paatason_suoritus") {
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id", O.PrimaryKey)
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi")
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto")
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo")
    val koulutusmoduuliKoulutustyyppi = column[Option[String]]("koulutusmoduuli_koulutustyyppi")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val toimipisteOid = column[String]("toimipiste_oid")
    val toimipisteNimi = column[String]("toimipiste_nimi")
    val data = column[JValue]("data")
    def * = (päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliKoulutustyyppi,
      vahvistusPäivä, toimipisteOid, toimipisteNimi, data) <> (RPäätasonSuoritusRow.tupled, RPäätasonSuoritusRow.unapply)
  }

  class ROsasuoritusTable(tag: Tag) extends Table[ROsasuoritusRow](tag, "r_osasuoritus") {
    val osasuoritusId = column[Long]("osasuoritus_id", O.PrimaryKey)
    val ylempiOsasuoritusId = column[Option[Long]]("ylempi_osasuoritus_id")
    val päätasonSuoritusId = column[Long]("paatason_suoritus_id")
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi")
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto")
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo")
    val koulutusmoduuliPaikallinen = column[Boolean]("koulutusmoduuli_paikallinen")
    val koulutusmoduuliPakollinen = column[Option[Boolean]]("koulutusmoduuli_pakollinen")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    val data = column[JValue]("data")
    def * = (osasuoritusId, ylempiOsasuoritusId, päätasonSuoritusId, opiskeluoikeusOid, suorituksenTyyppi,
      koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutusmoduuliPaikallinen, koulutusmoduuliPakollinen,
      vahvistusPäivä, data) <> (ROsasuoritusRow.tupled, ROsasuoritusRow.unapply)
  }

  class RHenkilöTable(tag: Tag) extends Table[RHenkilöRow](tag, "r_henkilo") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey)
    val hetu = column[Option[String]]("hetu")
    val syntymäaika = column[Option[Date]]("syntymaaika")
    val sukunimi = column[String]("sukunimi")
    val etunimet = column[String]("etunimet")
    val äidinkieli = column[Option[String]]("aidinkieli")
    val kansalaisuus = column[Option[String]]("kansalaisuus")
    val turvakielto = column[Boolean]("turvakielto")
    def * = (oppijaOid, hetu, syntymäaika, sukunimi, etunimet, äidinkieli, kansalaisuus, turvakielto) <> (RHenkilöRow.tupled, RHenkilöRow.unapply)
  }

  class ROrganisaatioTable(tag: Tag) extends Table[ROrganisaatioRow](tag, "r_organisaatio") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey)
    val nimi = column[String]("nimi")
    val organisaatiotyypit = column[String]("organisaatiotyypit")
    val oppilaitostyyppi = column[Option[String]]("oppilaitostyyppi")
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero")
    val kotipaikka = column[Option[String]]("kotipaikka")
    def * = (organisaatioOid, nimi, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero, kotipaikka) <> (ROrganisaatioRow.tupled, ROrganisaatioRow.unapply)
  }

  class RKoodistoKoodiTable(tag: Tag) extends Table[RKoodistoKoodiRow](tag, "r_koodisto_koodi") {
    val koodistoUri = column[String]("koodisto_uri")
    val koodiarvo = column[String]("koodiarvo")
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

case class ROpiskeluoikeusjaksoRow(
  opiskeluoikeusOid: String,
  alku: Date,
  loppu: Option[Date],
  tila: String,
  opiskeluoikeusPäättynyt: Boolean,
  opintojenRahoitus: Option[String]
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
