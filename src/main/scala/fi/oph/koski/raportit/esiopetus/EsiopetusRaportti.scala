package fi.oph.koski.raportit.esiopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import slick.jdbc.GetResult

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class EsiopetusRaportti(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
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
      opiskeluoikeudenPäättymispäivä = r.nextDateOption.map(_.toLocalDate),
      opiskeluoikeudenViimeisinTila = r.<<,
      opiskeluoikeudenTilaRaportinTarkasteluajankohtana = r.<<,
      koulutuskoodi = r.<<,
      koulutus = r.<<,
      perusteenDiaarinumero = r.<<,
      suorituskieli = r.<<,
      suorituksenVahvistuspäivä = r.nextDateOption.map(_.toLocalDate),
      yksilöity = r.<<,
      oppijaOid = r.<<,
      hetu = r.<<,
      etunimet = r.<<,
      sukunimi = r.<<,
      kotikunta = r.<<,
      pidennettyOppivelvollisuus = r.<<,
      tukimuodot = r.<<,
      erityisenTuenPäätös = r.<<,
      vammainen = r.<<,
      vaikeastiVammainen = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<,
      koulukoti = r.<<,
      ostopalveluTaiPalveluseteli = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä, t.language).as[EsiopetusRaporttiRow]
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = runDbSync(raporttiQuery, timeout = 5.minutes),
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: LocalDate, lang: String)(implicit u: KoskiSpecificSession) = {
    val koulutustoimijaNimiSarake = if(lang == "sv") "koulutustoimija_nimi_sv" else "koulutustoimija_nimi"
    val oppilaitosNimiSarake = if(lang == "sv") "oppilaitos_nimi_sv" else "oppilaitos_nimi"
    val toimipisteNimiSarake = if(lang == "sv") "toimipiste_nimi_sv" else "toimipiste_nimi"
    val kotikuntaSarake = if(lang == "sv") "kotikunta_nimi_sv" else "kotikunta_nimi_fi"
    val koodistoSarake = if(lang == "sv") "nimi_sv" else "nimi"
    sql"""
    select
      r_opiskeluoikeus.opiskeluoikeus_oid,
      lahdejarjestelma_koodiarvo,
      lahdejarjestelma_id,
      aikaleima,
      #$koulutustoimijaNimiSarake as koulutustoimija_nimi,
      #$oppilaitosNimiSarake as oppilaitos_nimi,
      #$toimipisteNimiSarake toimipiste_nimi,
      r_opiskeluoikeus.alkamispaiva,
      r_opiskeluoikeus.paattymispaiva,
      viimeisin_tila,
      aikajakso.tila,
      r_paatason_suoritus.koulutusmoduuli_koodiarvo,
      COALESCE(r_paatason_suoritus.data -> 'koulutusmoduuli' -> 'tunniste' -> 'nimi' ->> $lang, r_paatason_suoritus.koulutusmoduuli_nimi) as koulutusmoduuli_nimi,
      r_paatason_suoritus.data -> 'koulutusmoduuli' ->> 'perusteenDiaarinumero',
      kielikoodi.#$koodistoSarake,
      r_paatason_suoritus.vahvistus_paiva,
      yksiloity,
      r_opiskeluoikeus.oppija_oid,
      hetu,
      etunimet,
      sukunimi,
      #$kotikuntaSarake as kotikunta_nimi,
      pidennetty_oppivelvollisuus,
      tukimuodot,
      erityisen_tuen_paatos,
      vammainen,
      vaikeasti_vammainen,
      majoitusetu,
      kuljetusetu,
      sisaoppilaitosmainen_majoitus,
      koulukoti,
      r_opiskeluoikeus.data -> 'järjestämismuoto' ->> 'koodiarvo'
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join esiopetus_opiskeluoik_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_koodisto_koodi as kielikoodi
      on kielikoodi.koodisto_uri = 'kieli'
      and kielikoodi.koodiarvo = r_paatason_suoritus.suorituskieli_koodiarvo
    where (r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOidit) or r_opiskeluoikeus.koulutustoimija_oid = any($oppilaitosOidit))
      and r_opiskeluoikeus.koulutusmuoto = 'esiopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
      -- access check
      and (
        #${(if (u.hasGlobalReadAccess) "true" else "false")}
        or
        r_opiskeluoikeus.oppilaitos_oid = any($käyttäjänOrganisaatioOidit)
        or
        (r_opiskeluoikeus.koulutustoimija_oid = any($käyttäjänKoulutustoimijaOidit) and r_opiskeluoikeus.oppilaitos_oid = any($käyttäjänOstopalveluOidit))
      )
  """
  }

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSpecificSession) = u.organisationOids(AccessType.read).toSeq

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSpecificSession) = u.varhaiskasvatusKäyttöoikeudet.toSeq
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  private def käyttäjänOstopalveluOidit(implicit u: KoskiSpecificSession) =
    organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "kotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta"), comment = Some(t.get("raportti-excel-kolumni-kotikunta-comment"))),
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "lähdejärjestelmäKoodiarvo" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    "lähdejärjestelmäId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    "aikaleima" -> Column(t.get("raportti-excel-kolumni-aikaleima")),
    "koulutustoimijaNimi" -> Column(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "toimipisteNimi" -> Column(t.get("raportti-excel-kolumni-toimipisteNimi")),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "opiskeluoikeudenPäättymispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
    "opiskeluoikeudenViimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
    "opiskeluoikeudenTilaRaportinTarkasteluajankohtana" -> Column(t.get("raportti-excel-kolumni-tilaHakupaivalla"), comment = Some(t.get("raportti-excel-kolumni-tilaHakupaivalla-comment"))),
    "koulutuskoodi" -> Column(t.get("raportti-excel-kolumni-koulutuskoodi")),
    "koulutus" -> Column(t.get("raportti-excel-kolumni-koulutus")),
    "perusteenDiaarinumero" -> Column(t.get("raportti-excel-kolumni-perusteenDiaarinumero")),
    "suorituskieli" -> Column(t.get("raportti-excel-kolumni-suorituskieli")),
    "suorituksenVahvistuspäivä" -> Column(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
    "yksilöity" -> Column(t.get("raportti-excel-kolumni-yksiloity")),
    "pidennettyOppivelvollisuus" -> Column(t.get("raportti-excel-kolumni-pidennettyOppivelvollisuus")),
    "tukimuodot" -> Column(t.get("raportti-excel-kolumni-tukimuodot")),
    "erityisenTuenPäätös" -> Column(t.get("raportti-excel-kolumni-erityisenTuenPaatosVoimassa")),
    "vammainen" -> Column(t.get("raportti-excel-kolumni-vammainen")),
    "vaikeastiVammainen" -> Column(t.get("raportti-excel-kolumni-vaikeastiVammainen")),
    "majoitusetu" -> Column(t.get("raportti-excel-kolumni-majoitusetu")),
    "kuljetusetu" -> Column(t.get("raportti-excel-kolumni-kuljetusetu")),
    "sisäoppilaitosmainenMajoitus" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus")),
    "koulukoti" -> Column(t.get("raportti-excel-kolumni-koulukoti")),
    "ostopalveluTaiPalveluseteli" -> Column(t.get("raportti-excel-kolumni-ostopalveluTaiPalveluseteli"), comment = Some(t.get("raportti-excel-kolumni-ostopalveluTaiPalveluseteli-comment")))
  )
}

case class EsiopetusRaporttiRow(
  oppijaOid: String,
  hetu: Option[String],
  etunimet: String,
  sukunimi: String,
  kotikunta: Option[String],
  opiskeluoikeusOid: String,
  lähdejärjestelmäKoodiarvo: Option[String],
  lähdejärjestelmäId: Option[String],
  aikaleima: LocalDate,
  koulutustoimijaNimi: Option[String],
  oppilaitosNimi: Option[String],
  toimipisteNimi: Option[String],
  opiskeluoikeudenAlkamispäivä: LocalDate,
  opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  opiskeluoikeudenViimeisinTila: String,
  opiskeluoikeudenTilaRaportinTarkasteluajankohtana: String,
  koulutuskoodi: String,
  koulutus: String,
  perusteenDiaarinumero: Option[String],
  suorituskieli: Option[String],
  suorituksenVahvistuspäivä: Option[LocalDate],
  yksilöity: Boolean,
  pidennettyOppivelvollisuus: Boolean,
  tukimuodot: Option[String],
  erityisenTuenPäätös: Boolean,
  vammainen: Boolean,
  vaikeastiVammainen: Boolean,
  majoitusetu: Boolean,
  kuljetusetu: Boolean,
  sisäoppilaitosmainenMajoitus: Boolean,
  koulukoti: Boolean,
  ostopalveluTaiPalveluseteli: Option[String]
)
