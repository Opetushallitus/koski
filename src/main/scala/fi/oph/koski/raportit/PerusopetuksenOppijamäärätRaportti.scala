package fi.oph.koski.raportit

import java.sql.Date

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class PerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[PerusopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    PerusopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.<<,
      opetuskieli = r.<<,
      vuosiluokka = r.<<,
      oppilaita = r.<<,
      vieraskielisiä = r.<<,
      pidennettyOppivelvollisuusJaVaikeastiVammainen = r.<<,
      pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = r.<<,
      virheellisestiSiirretytVaikeastiVammaiset = r.<<,
      virheellisestiSiirretytMuutKuinVaikeimminVammaiset = r.<<,
      erityiselläTuella = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<,
      koulukoti = r.<<,
      joustavaPerusopetus = r.<<
    )
  )

  def build(oppilaitosOids: List[String], date: Date)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), date).as[PerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOids: List[String], date: Date)(implicit u: KoskiSession) = {
    sql"""
    with q as (
      select
        r_opiskeluoikeus.oppilaitos_nimi,
        r_koodisto_koodi.nimi as opetuskieli,
        r_paatason_suoritus.koulutusmoduuli_koodiarvo as vuosiluokka,
        count(*) as oppilaita,
        count(case when aidinkieli != 'fi' and aidinkieli != 'sv' and aidinkieli != 'se' and aidinkieli != 'ri' and aidinkieli != 'vk' then 1 end) as vieraskielisiä,
        count(case when pidennetty_oppivelvollisuus and vaikeasti_vammainen then 1 end) as pidennettyOppivelvollisuusJaVaikeastiVammainen,
        count(case when pidennetty_oppivelvollisuus and not vaikeasti_vammainen and vammainen then 1 end) as pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen,
        count(case when not pidennetty_oppivelvollisuus and vaikeasti_vammainen and erityinen_tuki then 1 end) as virheellisestiSiirretytVaikeastiVammaiset,
        count(case when not pidennetty_oppivelvollisuus and not vaikeasti_vammainen and vammainen and erityinen_tuki then 1 end) as virheellisestiSiirretytMuutKuinVaikeimminVammaiset,
        count(case when erityinen_tuki then 1 end) as erityiselläTuella,
        count(case when majoitusetu then 1 end) as majoitusetu,
        count(case when kuljetusetu then 1 end) as kuljetusetu,
        count(case when sisaoppilaitosmainen_majoitus then 1 end) as sisäoppilaitosmainenMajoitus,
        count(case when koulukoti then 1 end) as koulukoti,
        count(case when joustava_perusopetus then 1 end) as joustava_perusopetus
      from r_opiskeluoikeus
      join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
      join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
      join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
      join r_koodisto_koodi on r_koodisto_koodi.koodisto_uri = split_part(split_part(kielikoodi, '#', 1), '_', 1) and r_koodisto_koodi.koodiarvo = split_part(kielikoodi, '#', 2)
      join r_organisaatio on r_organisaatio.organisaatio_oid = oppilaitos_oid
      left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
      where r_opiskeluoikeus.oppilaitos_oid in (#${toSqlList(oppilaitosOids)})
        and r_opiskeluoikeus.koulutusmuoto = 'perusopetus'
        and r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenvuosiluokka'
        and aikajakso.alku <= $date
        and aikajakso.loppu >= $date
        and aikajakso.tila = 'lasna'
      group by r_opiskeluoikeus.oppilaitos_nimi, r_koodisto_koodi.nimi, r_paatason_suoritus.koulutusmoduuli_koodiarvo
    ), with_sums as (
      select * from q
      union all
      select
        oppilaitos_nimi,
        opetuskieli,
        'yhteensä' as vuosiluokka,
        sum(oppilaita),
        sum(vieraskielisiä),
        sum(pidennettyOppivelvollisuusJaVaikeastiVammainen),
        sum(pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen),
        sum(virheellisestiSiirretytVaikeastiVammaiset),
        sum(virheellisestiSiirretytMuutKuinVaikeimminVammaiset),
        sum(erityiselläTuella),
        sum(majoitusetu),
        sum(kuljetusetu),
        sum(sisäoppilaitosmainenMajoitus),
        sum(koulukoti),
        sum(joustava_perusopetus)
      from q
      group by oppilaitos_nimi, opetuskieli
    ) select *
    from with_sums
    order by oppilaitos_nimi, vuosiluokka
  """
  }

  private def toSqlList[T](xs: Iterable[T]) = xs.mkString("'", "','", "'")

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
    "vuosiluokka" -> Column("Vuosiluokka"),
    "oppilaita" -> Column("Perusopetusoppilaiden määrä", comment = Some("\"Läsnä\"-tilaiset perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "vieraskielisiä" -> Column("Perusopetusoppilaista vieraskielisiä"),
    "pidennettyOppivelvollisuusJaVaikeastiVammainen" -> Column("Pidennetty oppivelvollisuus ja vaikeasti vammainen"),
    "pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen" -> Column("Pidennetty oppivelvollisuus ja muu kuin vaikeimmin vammainen"),
    "virheellisestiSiirretytVaikeastiVammaiset" -> Column("Virheellisesti siirretyt vaikeasti vammaiset", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva \"Vaikeasti vammainen\"-jakso, mutta joille ei löydy kyseiselle päivälle osuvaa pidennetyn oppivelvollisuuden ja erityisen tuen jaksoja.")),
    "virheellisestiSiirretytMuutKuinVaikeimminVammaiset" -> Column("Virheellisesti siirretyt muut kuin vaikeimmin vammaiset", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva \"Muu kuin vaikeasti vammainen\"-jakso, mutta joille ei löydy kyseiselle päivälle osuvaa pidennetyn oppivelvollisuuden ja erityisen tuen jaksoja.")),
    "erityiselläTuella" -> Column("Perusopetusoppilaat, joilla erityinen tuki", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva erityisen tuen jakso.")),
    "majoitusetu" -> Column("Majoitusetu", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva majoitusetujakso. ")),
    "kuljetusetu" -> Column("Kuljetusetu", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva kuljetusetujakso.")),
    "sisäoppilaitosmainenMajoitus" -> Column("Sisäoppilaitosmainen majoitus", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva sisäoppilaitosmaisen majoituksen jakso.")),
    "koulukoti" -> Column("Koulukotioppilas", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva koulukotijakso.")),
    "joustavaPerusopetus" -> Column("Joustava perusopetus", comment = Some("Perusopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva joustavan perusopetuksen jakso."))
  )
}

case class PerusopetuksenOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  opetuskieli: String,
  vuosiluokka: String,
  oppilaita: Int,
  vieraskielisiä: Int,
  pidennettyOppivelvollisuusJaVaikeastiVammainen: Int,
  pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen: Int,
  virheellisestiSiirretytVaikeastiVammaiset: Int,
  virheellisestiSiirretytMuutKuinVaikeimminVammaiset: Int,
  erityiselläTuella: Int,
  majoitusetu: Int,
  kuljetusetu: Int,
  sisäoppilaitosmainenMajoitus: Int,
  koulukoti: Int,
  joustavaPerusopetus: Int
)
