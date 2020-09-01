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

case class EsiopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
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
      pidennettyOppivelvollisuusJaVaikeastiVammainen = r.<<,
      pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = r.<<,
      virheellisestiSiirretytVaikeastiVammaiset = r.<<,
      virheellisestiSiirretytMuutKuinVaikeimminVammaiset = r.<<,
      erityiselläTuella = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: Date): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä).as[EsiopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: Date) = {
    sql"""
    select
      r_opiskeluoikeus.oppilaitos_nimi,
      r_koodisto_koodi.nimi,
      count(*) as esiopetusoppilaidenMäärä,
      count(case when aidinkieli != 'fi' and aidinkieli != 'sv' and aidinkieli != 'se' and aidinkieli != 'ri' and aidinkieli != 'vk' then 1 end) as vieraskielisiä,
      count(case when koulutusmoduuli_koodiarvo = '001101' then 1 end) as koulunesiopetuksessa,
      count(case when koulutusmoduuli_koodiarvo = '001102' then 1 end) as päiväkodinesiopetuksessa,
      count(case when extract(year from syntymaaika) = 5 then 1 end) as viisivuotiaita,
      count(case when extract(year from syntymaaika) = 5 and pidennetty_oppivelvollisuus = true then 1 end) as viisivuotiaitaEiPidennettyäOppivelvollisuutta,
      count(case when pidennetty_oppivelvollisuus = true and vaikeasti_vammainen = true then 1 end) as pidennettyOppivelvollisuusJaVaikeastiVammainen,
      count(case when pidennetty_oppivelvollisuus = true and vaikeasti_vammainen = false and vammainen = true then 1 end) as pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen,
      count(case when pidennetty_oppivelvollisuus = false and vaikeasti_vammainen = true then 1 end) as virheellisestiSiirretytVaikeastiVammaiset,
      count(case when pidennetty_oppivelvollisuus = false and vaikeasti_vammainen = false and vammainen = true then 1 end) as virheellisestiSiirretytMuutKuinVaikeimminVammaiset,
      count(case when erityisen_tuen_paatos = true then 1 end) as erityiselläTuella,
      count(case when majoitusetu = true then 1 end) as majoitusetu,
      count(case when kuljetusetu = true then 1 end) as kuljetusetu,
      count(case when sisaoppilaitosmainen_majoitus = true then 1 end) as sisäoppilaitosmainenMajoitus
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join esiopetus_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
    join r_koodisto_koodi on r_koodisto_koodi.koodisto_uri = split_part(split_part(kielikoodi, '#', 1), '_', 1) and r_koodisto_koodi.koodiarvo = split_part(kielikoodi, '#', 2)
    join r_organisaatio on r_organisaatio.organisaatio_oid = oppilaitos_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    where r_opiskeluoikeus.oppilaitos_oid in (#${toSqlList(oppilaitosOidit)})
      and r_opiskeluoikeus.koulutusmuoto = 'esiopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
    group by r_opiskeluoikeus.oppilaitos_nimi, r_koodisto_koodi.nimi
  """
  }

  private def toSqlList[T](xs: Iterable[T]) = xs.mkString("'", "','","'")

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column("Oppilaitos"),
    "opetuskieli" -> Column("Opetuskieli"),
    "esiopetusoppilaidenMäärä" -> Column("Esiopetusoppilaiden määrä"),
    "vieraskielisiä" -> Column("Vieraskielisiä"),
    "koulunesiopetuksessa" -> Column("Koulun esiopetuksessa"),
    "päiväkodinesiopetuksessa" -> Column("Päiväkodin esiopetuksessa"),
    "viisivuotiaita" -> Column("Viisivuotiaita"),
    "viisivuotiaitaEiPidennettyäOppivelvollisuutta" -> Column("Viisivuotiaita, ei pidennettyä oppivelvollisuutta"),
    "pidennettyOppivelvollisuusJaVaikeastiVammainen" -> Column("Pidennetty oppivelvollisuus ja vaikeasti vammainen"),
    "pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen" -> Column("Pidennetty oppivelvollisuus ja muu kuin vaikeimmin vammainen"),
    "virheellisestiSiirretytVaikeastiVammaiset" -> Column("Virheellisesti siirretyt vaikeasti vammaiset"),
    "virheellisestiSiirretytMuutKuinVaikeimminVammaiset" -> Column("Virheellisesti siirretyt muut kuin vaikeimmin vammaiset"),
    "erityiselläTuella" -> Column("Erityisellä tuella"),
    "majoitusetu" -> Column("Majoitusetu"),
    "kuljetusetu" -> Column("Kuljetusetu"),
    "sisäoppilaitosmainenMajoitus" -> Column("Sisäoppilaitosmainen majoitus")
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
  pidennettyOppivelvollisuusJaVaikeastiVammainen: Int,
  pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen: Int,
  virheellisestiSiirretytVaikeastiVammaiset: Int,
  virheellisestiSiirretytMuutKuinVaikeimminVammaiset: Int,
  erityiselläTuella: Int,
  majoitusetu: Int,
  kuljetusetu: Int,
  sisäoppilaitosmainenMajoitus: Int
)
