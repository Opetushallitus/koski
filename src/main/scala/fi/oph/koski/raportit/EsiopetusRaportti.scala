package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods

import scala.concurrent.duration._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import slick.jdbc.GetResult

case class EsiopetusRaportti(db: DB) extends KoskiDatabaseMethods {

  implicit val getResult = GetResult(r =>
    EsiopetusRaporttiRow(
      opiskeluoikeusOid = r.<<,
      lähdejärjestelmäKoodiarvo = r.<<,
      lähdejärjestelmäId = r.<<,
      aikaleima = r.nextTimestamp.toLocalDateTime.toLocalDate,
      koulutustoimijaNimi = r.<<,
      oppilaitosNimi = r.<<,
      toimipisteNimi = r.<<,
      opiskeluoikeudenAlkamispäivä = r.nextDate.toLocalDate,
      opiskeluoikeudenViimeisinTila = r.<<,
      yksilöity = r.<<,
      oppijaOid = r.<<,
      hetu = r.<<,
      etunimet = r.<<,
      sukunimi = r.<<,
      pidennettyOppivelvollisuus = r.<<,
      tukimuodot = r.<<,
      erityisenTuenPäätös = r.<<,
      erityisenTuenPäätösOpiskeleeToimintaAlueittain = r.<<,
      erityisenTuenPäätösErityisryhmässä = r.<<,
      erityisenTuenPäätösToteutuspaikka = r.<<,
      vammainen = r.<<,
      vaikeastiVammainen = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<,
      koulukoti = r.<<
    )
  )

  def build(oppilaitosOid: String, päivä: Date) = {
    val rows = runDbSync(query(oppilaitosOid, päivä).as[EsiopetusRaporttiRow], timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows,
      columnSettings
    )
  }

  private def query(oppilaitosOid: String, päivä: Date) = sql"""
    select
      r_opiskeluoikeus.opiskeluoikeus_oid,
      lahdejarjestelma_koodiarvo,
      lahdejarjestelma_id,
      aikaleima,
      koulutustoimija_nimi,
      oppilaitos_nimi,
      toimipiste_nimi,
      alkamispaiva,
      viimeisin_tila,
      yksiloity,
      r_opiskeluoikeus.oppija_oid,
      hetu,
      etunimet,
      sukunimi,
      pidennetty_oppivelvollisuus,
      tukimuodot,
      erityisen_tuen_paatos,
      erityisen_tuen_paatos_opiskelee_toiminta_alueittain,
      erityisen_tuen_paatos_erityisryhmassa,
      erityisen_tuen_paatos_toteutuspaikka,
      vammainen,
      vaikeasti_vammainen,
      majoitusetu,
      kuljetusetu,
      sisaoppilaitosmainen_majoitus,
      koulukoti
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join esiopetus_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    where r_opiskeluoikeus.oppilaitos_oid = $oppilaitosOid
      and r_opiskeluoikeus.koulutusmuoto = 'esiopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
  """

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmäKoodiarvo" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    "lähdejärjestelmäId" -> Column("Lähdejärjestelmä"),
    "aikaleima" -> Column("Opiskeluoikeus päivitetty"),
    "koulutustoimijaNimi" -> Column("Koulutustoimijan nimi"),
    "oppilaitosNimi" -> Column("Oppilaitoksen nimi"),
    "toimipisteNimi" -> Column("Toimipisteen nimi"),
    "opiskeluoikeudenAlkamispäivä" -> Column("Opiskeluoikeuden alkamispäivä"),
    "opiskeluoikeudenViimeisinTila" -> Column("Opiskeluoikeuden viimeisin tila"),
    "yksilöity" -> Column("Oppija yksilöity"),
    "oppijaOid" -> Column("Oppija oid"),
    "hetu" -> Column("Hetu"),
    "etunimet" -> Column("Etunimet"),
    "sukunimi" -> Column("Sukunimi"),
    "pidennettyOppivelvollisuus" -> Column("Pidennetty oppivelvollisuus"),
    "tukimuodot" -> Column("Tukimuodot"),
    "erityisenTuenPäätös" -> Column("Erityisen tuen päätös"),
    "erityisenTuenPäätösOpiskeleeToimintaAlueittain" -> Column("Erityisen tuen päätös: Opiskelee toiminta-alueittain"),
    "erityisenTuenPäätösErityisryhmässä" -> Column("Erityisen tuen päätös: Erityisryhmässä"),
    "erityisenTuenPäätösToteutuspaikka" -> Column("Erityisen tuen päätös: Toteutuspaikka"),
    "vammainen" -> Column("Vammainen"),
    "vaikeastiVammainen" -> Column("Vaikeasti vammainen"),
    "majoitusetu" -> Column("Majoitusetu"),
    "kuljetusetu" -> Column("Kuljetusetu"),
    "sisäoppilaitosmainenMajoitus" -> Column("Sisäoppilaitosmainen majoitus"),
    "koulukoti" -> Column("Koulukoti")
  )
}

case class EsiopetusRaporttiRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmäKoodiarvo: Option[String],
  lähdejärjestelmäId: Option[String],
  aikaleima: LocalDate,
  koulutustoimijaNimi: Option[String],
  oppilaitosNimi: Option[String],
  toimipisteNimi: Option[String],
  opiskeluoikeudenAlkamispäivä: LocalDate,
  opiskeluoikeudenViimeisinTila: String,
  yksilöity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
  pidennettyOppivelvollisuus: Boolean,
  tukimuodot: Option[String],
  erityisenTuenPäätös: Boolean,
  erityisenTuenPäätösOpiskeleeToimintaAlueittain: Boolean,
  erityisenTuenPäätösErityisryhmässä: Boolean,
  erityisenTuenPäätösToteutuspaikka: Option[String],
  vammainen: Boolean,
  vaikeastiVammainen: Boolean,
  majoitusetu: Boolean,
  kuljetusetu: Boolean,
  sisäoppilaitosmainenMajoitus: Boolean,
  koulukoti: Boolean
)
