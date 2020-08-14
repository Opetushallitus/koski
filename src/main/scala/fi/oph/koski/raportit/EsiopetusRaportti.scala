package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.{Oid, isValidOrganisaatioOid}
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class EsiopetusRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[EsiopetusRaporttiRow] = GetResult(r =>
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
      opiskeluoikeudenTilaRaportinTarkasteluajankohtana = r.<<,
      koulutus = r.<<,
      suorituksenVahvistuspäivä = r.nextDateOption.map(_.toLocalDate),
      yksilöity = r.<<,
      oppijaOid = r.<<,
      hetu = r.<<,
      etunimet = r.<<,
      sukunimi = r.<<,
      pidennettyOppivelvollisuus = r.<<,
      tukimuodot = r.<<,
      erityisenTuenPäätös = r.<<,
      erityisenTuenPäätösOpiskeleeToimintaAlueittain = r.<<,
      erityisenTuenPäätösToteutuspaikka = r.<<,
      vammainen = r.<<,
      vaikeastiVammainen = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<,
      koulukoti = r.<<,
      ostopalveluTaiPalveluseteli = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: Date)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä).as[EsiopetusRaporttiRow]
    DataSheet(
      title = "Suoritukset",
      rows = runDbSync(raporttiQuery, timeout = 5.minutes),
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: Date)(implicit u: KoskiSession) =
    sql"""
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
      aikajakso.tila,
      r_paatason_suoritus.koulutusmoduuli_nimi,
      r_paatason_suoritus.vahvistus_paiva,
      yksiloity,
      r_opiskeluoikeus.oppija_oid,
      hetu,
      etunimet,
      sukunimi,
      pidennetty_oppivelvollisuus,
      tukimuodot,
      erityisen_tuen_paatos,
      erityisen_tuen_paatos_opiskelee_toiminta_alueittain,
      erityisen_tuen_paatos_toteutuspaikka,
      vammainen,
      vaikeasti_vammainen,
      majoitusetu,
      kuljetusetu,
      sisaoppilaitosmainen_majoitus,
      koulukoti,
      r_opiskeluoikeus.data -> 'järjestämismuoto' ->> 'koodiarvo'
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join esiopetus_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    where r_opiskeluoikeus.oppilaitos_oid in (#${toSqlList(oppilaitosOidit)})
      and r_opiskeluoikeus.koulutusmuoto = 'esiopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
      -- access check
      and (
        #${(if (u.hasGlobalReadAccess) "true" else "false")}
        or
        r_opiskeluoikeus.oppilaitos_oid in (#${toSqlList(käyttäjänOrganisaatioOidit)})
        or
        (r_opiskeluoikeus.koulutustoimija_oid in (#${toSqlList(käyttäjänKoulutustoimijaOidit)}) and r_opiskeluoikeus.oppilaitos_oid in (#${toSqlList(käyttäjänOstopalveluOidit)}))
      )
  """

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSession) = u.organisationOids(AccessType.read)

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSession) = u.varhaiskasvatusKäyttöoikeudet
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  private def käyttäjänOstopalveluOidit(implicit u: KoskiSession) =
    organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)

  private def toSqlList[T](xs: Iterable[T]) = xs.mkString("'", "','","'")

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmäKoodiarvo" -> Column("Lähdejärjestelmä"),
    "lähdejärjestelmäId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    "aikaleima" -> Column("Opiskeluoikeus päivitetty"),
    "koulutustoimijaNimi" -> Column("Koulutustoimijan nimi"),
    "oppilaitosNimi" -> Column("Oppilaitoksen nimi"),
    "toimipisteNimi" -> Column("Toimipisteen nimi"),
    "opiskeluoikeudenAlkamispäivä" -> Column("Opiskeluoikeuden alkamispäivä"),
    "opiskeluoikeudenViimeisinTila" -> Column("Opiskeluoikeuden viimeisin tila"),
    "opiskeluoikeudenTilaRaportinTarkasteluajankohtana" -> Column("Opiskeluoikeuden tila raportin tarkasteluajankohtana", comment = Some("Opiskeluoikeuden tila raportin tulostusparametreissa valittuna päivänä")),
    "koulutus" -> Column("Koulutus"),
    "suorituksenVahvistuspäivä" -> Column("Suorituksen vahvistuspäivä"),
    "yksilöity" -> Column("Oppija yksilöity"),
    "oppijaOid" -> Column("Oppija oid"),
    "hetu" -> Column("Hetu"),
    "etunimet" -> Column("Etunimet"),
    "sukunimi" -> Column("Sukunimi"),
    "pidennettyOppivelvollisuus" -> Column("Pidennetty oppivelvollisuus"),
    "tukimuodot" -> Column("Tukimuodot"),
    "erityisenTuenPäätös" -> Column("Erityisen tuen päätös"),
    "erityisenTuenPäätösOpiskeleeToimintaAlueittain" -> Column("Erityisen tuen päätös: Opiskelee toiminta-alueittain"),
    "erityisenTuenPäätösToteutuspaikka" -> Column("Erityisen tuen päätös: Toteutuspaikka"),
    "vammainen" -> Column("Vammainen"),
    "vaikeastiVammainen" -> Column("Vaikeimmin kehitysvammainen"),
    "majoitusetu" -> Column("Majoitusetu"),
    "kuljetusetu" -> Column("Kuljetusetu"),
    "sisäoppilaitosmainenMajoitus" -> Column("Sisäoppilaitosmainen majoitus"),
    "koulukoti" -> Column("Koulukoti"),
    "ostopalveluTaiPalveluseteli" -> Column("Ostopalvelu/palveluseteli", comment = Some("'JM02': Ostopalvelu, 'JM03': Palveluseteli"))
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
  opiskeluoikeudenTilaRaportinTarkasteluajankohtana: String,
  koulutus: String,
  suorituksenVahvistuspäivä: Option[LocalDate],
  yksilöity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
  pidennettyOppivelvollisuus: Boolean,
  tukimuodot: Option[String],
  erityisenTuenPäätös: Boolean,
  erityisenTuenPäätösOpiskeleeToimintaAlueittain: Boolean,
  erityisenTuenPäätösToteutuspaikka: Option[String],
  vammainen: Boolean,
  vaikeastiVammainen: Boolean,
  majoitusetu: Boolean,
  kuljetusetu: Boolean,
  sisäoppilaitosmainenMajoitus: Boolean,
  koulukoti: Boolean,
  ostopalveluTaiPalveluseteli: Option[String]
)
