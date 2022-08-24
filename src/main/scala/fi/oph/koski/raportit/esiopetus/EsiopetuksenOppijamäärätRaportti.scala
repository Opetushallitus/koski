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

case class EsiopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
  implicit private val getResult: GetResult[EsiopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    EsiopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.<<,
      opetuskieli = r.<<,
      esiopetusoppilaidenMäärä = r.<<,
      vieraskielisiä = r.<<,
      koulunesiopetuksessa = r.<<,
      päiväkodinesiopetuksessa = r.<<,
      viisivuotiaita = r.<<,
      viisivuotiaitaEiPidennettyäOppivelvollisuutta = r.<<,
      pidOppivelvollisuusEritTukiJaVaikeastiVammainen = r.<<,
      pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = r.<<,
      virheellisestiSiirrettyjaTukitietoja = r.<<,
      erityiselläTuella = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä, t.language).as[EsiopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: LocalDate, lang: String)(implicit u: KoskiSpecificSession) = {
    val year = päivä.getYear
    val oppilaitosNimiSarake = if(lang == "sv") "oppilaitos_nimi_sv" else "oppilaitos_nimi"
    val koodistoNimiSarake = if(lang == "sv") "nimi_sv" else "nimi"

    sql"""
    select
      r_opiskeluoikeus.#$oppilaitosNimiSarake,
      r_koodisto_koodi.#$koodistoNimiSarake,
      count(*) as esiopetusoppilaidenMäärä,
      count(case when aidinkieli != 'fi' and aidinkieli != 'sv' and aidinkieli != 'se' and aidinkieli != 'ri' and aidinkieli != 'vk' then 1 end) as vieraskielisiä,
      count(case when koulutusmoduuli_koodiarvo = '001101' then 1 end) as koulunesiopetuksessa,
      count(case when koulutusmoduuli_koodiarvo = '001102' then 1 end) as päiväkodinesiopetuksessa,
      count(case when $year - extract(year from syntymaaika) = 5 then 1 end) as viisivuotiaita,
      count(case when $year - extract(year from syntymaaika) = 5 and pidennetty_oppivelvollisuus = false then 1 end) as viisivuotiaitaEiPidennettyäOppivelvollisuutta,
      count(case when erityisen_tuen_paatos and not vammainen and vaikeasti_vammainen and pidennetty_oppivelvollisuus then 1 end) as pidOppivelvollisuusEritTukiJaVaikeastiVammainen,
      count(case when erityisen_tuen_paatos and vammainen and not vaikeasti_vammainen and pidennetty_oppivelvollisuus then 1 end) as pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen,
      count(case when ((vaikeasti_vammainen and vammainen) or (pidennetty_oppivelvollisuus and (not erityisen_tuen_paatos or (not vaikeasti_vammainen and not vammainen))) or (not pidennetty_oppivelvollisuus and (vaikeasti_vammainen or vammainen))) then 1 end) as virheellisestiSiirrettyjaTukitietoja,
      count(case when erityisen_tuen_paatos = true then 1 end) as erityiselläTuella,
      count(case when majoitusetu = true then 1 end) as majoitusetu,
      count(case when kuljetusetu = true then 1 end) as kuljetusetu,
      count(case when sisaoppilaitosmainen_majoitus = true then 1 end) as sisäoppilaitosmainenMajoitus
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join esiopetus_opiskeluoik_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
    join r_koodisto_koodi
      on r_koodisto_koodi.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
      and r_koodisto_koodi.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
    join r_organisaatio on r_organisaatio.organisaatio_oid = oppilaitos_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    where (r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOidit) or r_opiskeluoikeus.koulutustoimija_oid = any($oppilaitosOidit))
      and r_opiskeluoikeus.koulutusmuoto = 'esiopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
      and aikajakso.tila = 'lasna'
    -- access check
      and (
        #${(if (u.hasGlobalReadAccess) "true" else "false")}
        or
        r_opiskeluoikeus.oppilaitos_oid = any($käyttäjänOrganisaatioOidit)
        or
        (r_opiskeluoikeus.koulutustoimija_oid = any($käyttäjänKoulutustoimijaOidit))
      )
    group by r_opiskeluoikeus.#$oppilaitosNimiSarake, r_koodisto_koodi.#$koodistoNimiSarake
  """
  }

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSpecificSession) = u.organisationOids(AccessType.read).toSeq

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSpecificSession) = u.varhaiskasvatusKäyttöoikeudet.toSeq
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "opetuskieli" -> Column(t.get("raportti-excel-kolumni-opetuskieli")),
    "esiopetusoppilaidenMäärä" -> Column(t.get("raportti-excel-kolumni-esiopetusoppilaidenMäärä"), comment = Some(t.get("raportti-excel-kolumni-esiopetusoppilaidenMäärä-comment"))),
    "vieraskielisiä" -> Column(t.get("raportti-excel-kolumni-esiopetusvieraskielisiä")),
    "koulunesiopetuksessa" -> Column(t.get("raportti-excel-kolumni-koulunesiopetuksessa")),
    "päiväkodinesiopetuksessa" -> Column(t.get("raportti-excel-kolumni-päiväkodinesiopetuksessa")),
    "viisivuotiaita" -> Column(t.get("raportti-excel-kolumni-viisivuotiaita"), comment = Some(t.get("raportti-excel-kolumni-viisivuotiaitaEiPidennettyäOppivelvollisuutta-comment"))),
    "viisivuotiaitaEiPidennettyäOppivelvollisuutta" -> Column(t.get("raportti-excel-kolumni-viisivuotiaitaEiPidennettyäOppivelvollisuutta")),
    "pidOppivelvollisuusEritTukiJaVaikeastiVammainen" -> Column(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaVaikeastiVammainen"), comment = Some(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaVaikeastiVammainen-comment"))),
    "pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen" -> Column(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen"), comment = Some(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen-comment"))),
    "virheellisestiSiirrettyjaTukitietoja" -> Column(t.get("raportti-excel-kolumni-virheellisestiSiirrettyjaTukitietoja"), comment = Some(t.get("raportti-excel-kolumni-virheellisestiSiirrettyjaTukitietoja-comment"))),
    "erityiselläTuella" -> Column(t.get("raportti-excel-kolumni-erityiselläTuella"), comment = Some(t.get("raportti-excel-kolumni-erityiselläTuella-comment"))),
    "majoitusetu" -> Column(t.get("raportti-excel-kolumni-majoitusetu"), comment = Some(t.get("raportti-excel-kolumni-majoitusetu-comment"))),
    "kuljetusetu" -> Column(t.get("raportti-excel-kolumni-kuljetusetu"), comment = Some(t.get("raportti-excel-kolumni-kuljetusetu-comment"))),
    "sisäoppilaitosmainenMajoitus" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-comment")))
  )
}

case class EsiopetuksenOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  opetuskieli: String,
  esiopetusoppilaidenMäärä: Int,
  vieraskielisiä: Int,
  koulunesiopetuksessa: Int,
  päiväkodinesiopetuksessa: Int,
  viisivuotiaita: Int,
  viisivuotiaitaEiPidennettyäOppivelvollisuutta: Int,
  pidOppivelvollisuusEritTukiJaVaikeastiVammainen: Int,
  pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen: Int,
  virheellisestiSiirrettyjaTukitietoja: Int,
  erityiselläTuella: Int,
  majoitusetu: Int,
  kuljetusetu: Int,
  sisäoppilaitosmainenMajoitus: Int
)
