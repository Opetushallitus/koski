package fi.oph.koski.raportit

import java.sql.Date

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.util.SQL.toSqlListUnsafe
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class PerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[PerusopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    PerusopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      opetuskieli = r.rs.getString("opetuskieli"),
      vuosiluokka = r.rs.getString("vuosiluokka"),
      oppilaita = r.rs.getInt("oppilaita"),
      vieraskielisiä = r.rs.getInt("vieraskielisiä"),
      pidennettyOppivelvollisuusJaVaikeastiVammainen = r.rs.getInt("pidennettyOppivelvollisuusJaVaikeastiVammainen"),
      pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = r.rs.getInt("pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen"),
      virheellisestiSiirretytVaikeastiVammaiset = r.rs.getInt("virheellisestiSiirretytVaikeastiVammaiset"),
      virheellisestiSiirretytMuutKuinVaikeimminVammaiset = r.rs.getInt("virheellisestiSiirretytMuutKuinVaikeimminVammaiset"),
      erityiselläTuella = r.rs.getInt("erityiselläTuella"),
      majoitusetu = r.rs.getInt("majoitusetu"),
      kuljetusetu = r.rs.getInt("kuljetusetu"),
      sisäoppilaitosmainenMajoitus = r.rs.getInt("sisäoppilaitosmainenMajoitus"),
      koulukoti = r.rs.getInt("koulukoti"),
      joustavaPerusopetus = r.rs.getInt("joustava_perusopetus")
    )
  )

  def build(oppilaitosOids: Set[String], date: Date)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, date).as[PerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOids: Set[String], date: Date)(implicit u: KoskiSession) = {
    sql"""
    with q as (
      select
        oo.oppilaitos_nimi,
        opetuskieli_koodisto.nimi as opetuskieli,
        r_paatason_suoritus.koulutusmoduuli_koodiarvo as vuosiluokka,
        count(distinct oo.opiskeluoikeus_oid) as oppilaita,
        count(distinct (case when r_henkilo.aidinkieli not in ('fi', 'sv', 'se', 'ri', 'vk') then oo.opiskeluoikeus_oid end)) as vieraskielisiä,
        count(distinct (case when                   vaikeasti_vammainen and pidennetty_oppivelvollisuus then oo.opiskeluoikeus_oid end)) as pidennettyOppivelvollisuusJaVaikeastiVammainen,
        count(distinct (case when vammainen and not vaikeasti_vammainen and pidennetty_oppivelvollisuus then oo.opiskeluoikeus_oid end)) as pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen,
        count(distinct (case when                   vaikeasti_vammainen and not (pidennetty_oppivelvollisuus and erityinen_tuki) then oo.opiskeluoikeus_oid end)) as virheellisestiSiirretytVaikeastiVammaiset,
        count(distinct (case when vammainen and not vaikeasti_vammainen and not (pidennetty_oppivelvollisuus and erityinen_tuki) then oo.opiskeluoikeus_oid end)) as virheellisestiSiirretytMuutKuinVaikeimminVammaiset,
        count(distinct (case when erityinen_tuki then oo.opiskeluoikeus_oid end)) as erityiselläTuella,
        count(distinct (case when majoitusetu then oo.opiskeluoikeus_oid end)) as majoitusetu,
        count(distinct (case when kuljetusetu then oo.opiskeluoikeus_oid end)) as kuljetusetu,
        count(distinct (case when sisaoppilaitosmainen_majoitus then oo.opiskeluoikeus_oid end)) as sisäoppilaitosmainenMajoitus,
        count(distinct (case when koulukoti then oo.opiskeluoikeus_oid end)) as koulukoti,
        count(distinct (case when joustava_perusopetus then oo.opiskeluoikeus_oid end)) as joustava_perusopetus
      from r_opiskeluoikeus oo
      join r_henkilo on r_henkilo.oppija_oid = oo.oppija_oid
      join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      left join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oo.oppilaitos_oid
      left join r_koodisto_koodi opetuskieli_koodisto
        on opetuskieli_koodisto.koodisto_uri = split_part(split_part(r_organisaatio_kieli.kielikoodi, '#', 1), '_', 1)
        and opetuskieli_koodisto.koodiarvo = split_part(r_organisaatio_kieli.kielikoodi, '#', 2)
      where oo.oppilaitos_oid in (#${toSqlListUnsafe(oppilaitosOids)})
        and oo.koulutusmuoto = 'perusopetus'
        and r_paatason_suoritus.vahvistus_paiva is null
        and r_paatason_suoritus.koulutusmoduuli_koodiarvo in ('1', '2', '3', '4', '5', '6', '7', '8', '9')
        and aikajakso.alku <= $date
        and aikajakso.loppu >= $date
        and aikajakso.tila = 'lasna'
      group by oo.oppilaitos_nimi, opetuskieli_koodisto.nimi, r_paatason_suoritus.koulutusmoduuli_koodiarvo
    ), totals as (
      select * from q
      union all
      select
        oppilaitos_nimi,
        opetuskieli,
        'Kaikki vuosiluokat yhteensä' as vuosiluokka,
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
    from totals
    order by oppilaitos_nimi, vuosiluokka
  """
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
