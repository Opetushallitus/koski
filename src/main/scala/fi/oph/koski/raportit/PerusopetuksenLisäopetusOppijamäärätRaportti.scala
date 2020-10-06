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

case class PerusopetuksenLisäopetusOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[PerusopetuksenLisäopetusOppijamäärätRaporttiRow] = GetResult(r =>
    PerusopetuksenLisäopetusOppijamäärätRaporttiRow(
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      organisaatioOid = r.rs.getString("oppilaitos_oid"),
      opetuskieli = r.rs.getString("opetuskieli"),
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
      koulukoti = r.rs.getInt("koulukoti")
    )
  )

  def build(oppilaitosOids: Set[String], date: Date)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, date).as[PerusopetuksenLisäopetusOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOids: Set[String], date: Date)(implicit u: KoskiSession) = {
    sql"""
    select
      oo.oppilaitos_nimi,
      oo.oppilaitos_oid,
      opetuskieli_koodisto.nimi as opetuskieli,
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
      count(distinct (case when koulukoti then oo.opiskeluoikeus_oid end)) as koulukoti
    from r_opiskeluoikeus oo
    join r_henkilo on r_henkilo.oppija_oid = oo.oppija_oid
    join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oo.oppilaitos_oid
    left join r_koodisto_koodi opetuskieli_koodisto
      on opetuskieli_koodisto.koodisto_uri = split_part(split_part(r_organisaatio_kieli.kielikoodi, '#', 1), '_', 1)
      and opetuskieli_koodisto.koodiarvo = split_part(r_organisaatio_kieli.kielikoodi, '#', 2)
    where oo.oppilaitos_oid in (#${toSqlListUnsafe(oppilaitosOids)})
      and oo.koulutusmuoto = 'perusopetuksenlisaopetus'
      and aikajakso.alku <= $date
      and aikajakso.loppu >= $date
      and aikajakso.tila = 'lasna'
    group by oo.oppilaitos_nimi, oo.oppilaitos_oid, opetuskieli_koodisto.nimi, r_paatason_suoritus.koulutusmoduuli_koodiarvo
  """
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column("Oppilaitos"),
    "organisaatioOid" -> Column("Organisaation oid"),
    "opetuskieli" -> Column("Opetuskieli", comment = Some("Oppilaitoksen opetuskieli Opintopolun organisaatiopalvelussa")),
    "oppilaita" -> Column("Oppilaiden määrä", comment = Some("\"Läsnä\"-tilaiset perusopetuksen lisäopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "vieraskielisiä" -> Column("Oppilaista vieraskielisiä", comment = Some("Oppilaat, joiden äidinkieli on muu kuin suomi, ruotsi, saame, romani tai viittomakieli")),
    "pidennettyOppivelvollisuusJaVaikeastiVammainen" -> Column("Pidennetty oppivelvollisuus ja vaikeasti vammainen", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva pidennetyn oppivelvollisuuden ja vaikean vammaisuuden jakso")),
    "pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen" -> Column("Pidennetty oppivelvollisuus ja muu kuin vaikeimmin vammainen", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva pidennetyn oppivelvollisuuden ja muun kuin vaikean vammaisuuden jakso")),
    "virheellisestiSiirretytVaikeastiVammaiset" -> Column("Virheellisesti siirretyt vaikeasti vammaiset", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva \"Vaikeasti vammainen\"-jakso, mutta joille ei löydy kyseiselle päivälle osuvaa pidennetyn oppivelvollisuuden ja erityisen tuen jaksoja.")),
    "virheellisestiSiirretytMuutKuinVaikeimminVammaiset" -> Column("Virheellisesti siirretyt muut kuin vaikeimmin vammaiset", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva \"Muu kuin vaikeasti vammainen\"-jakso, mutta joille ei löydy kyseiselle päivälle osuvaa pidennetyn oppivelvollisuuden ja erityisen tuen jaksoja.")),
    "erityiselläTuella" -> Column("Oppilaat, joilla erityinen tuki", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva erityisen tuen jakso.")),
    "majoitusetu" -> Column("Majoitusetu", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva majoitusetujakso. ")),
    "kuljetusetu" -> Column("Kuljetusetu", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva kuljetusetujakso.")),
    "sisäoppilaitosmainenMajoitus" -> Column("Sisäoppilaitosmainen majoitus", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva sisäoppilaitosmaisen majoituksen jakso.")),
    "koulukoti" -> Column("Koulukotioppilas", comment = Some("Perusopetuksen lisäopetuksen oppilaat, joille löytyy opiskeluoikeuden lisätiedoista raportin tulostusparametreissa määritellylle päivälle osuva koulukotijakso."))
  )
}

case class PerusopetuksenLisäopetusOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  organisaatioOid: String,
  opetuskieli: String,
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
  koulukoti: Int
)
