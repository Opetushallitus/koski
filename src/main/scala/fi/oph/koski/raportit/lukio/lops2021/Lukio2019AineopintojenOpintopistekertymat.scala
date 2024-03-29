package fi.oph.koski.raportit.lukio.lops2021

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.lukio.LukioOppiaineenOppimaaranKurssikertymat.Oppimäärä
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import slick.jdbc.GetResult

import java.sql.ResultSet
import java.time.LocalDate

object Lukio2019AineopintojenOpintopistekertymat extends DatabaseConverters {

  def datasheet(
    oppilaitosOids: List[String],
    jaksonAlku: LocalDate,
    jaksonLoppu: LocalDate,
    raportointiDatabase: RaportointiDatabase,
    t: LocalizationReader,
    oppimäärä: Oppimäärä
  ): DataSheet = {
    DataSheet(
      oppimäärä.sheetTitle(t),
      rows = raportointiDatabase.runDbSync(queryAineopiskelija(oppilaitosOids, jaksonAlku, jaksonLoppu, oppimäärä)),
      columnSettings(t)
    )
  }

  def createPrecomputedTable(s: Schema) =
    sqlu"""
      create table #${s.name}.lukion_aineopintojen_opintopistekertyma as select
        oppilaitos_oid,
        arviointi_paiva,
        oppimaara_koodiarvo,
        sum(laajuus) yhteensa,
        sum(laajuus) filter (where suoritettu) suoritettuja,
        sum(laajuus) filter (where tunnustettu) tunnustettuja,
        sum(laajuus) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa,
        sum(laajuus) filter (where pakollinen or valtakunnallinen) pakollisia_tai_valtakunnallisia,
        sum(laajuus) filter (where pakollinen) pakollisia,
        sum(laajuus) filter (where valtakunnallinen) valtakunnallisia,
        sum(laajuus) filter (where suoritettu and (pakollinen or valtakunnallinen)) suoritettuja_pakollisia_ja_valtakunnallisia,
        sum(laajuus) filter (where pakollinen and suoritettu) suoritettuja_pakollisia,
        sum(laajuus) filter (where suoritettu and valtakunnallinen) suoritettuja_valtakunnallisia,
        sum(laajuus) filter (where tunnustettu and (pakollinen or valtakunnallinen)) tunnustettuja_pakollisia_ja_valtakunnallisia,
        sum(laajuus) filter (where tunnustettu and pakollinen) tunnustettuja_pakollisia,
        sum(laajuus) filter (where tunnustettu and valtakunnallinen) tunnustettuja_valtakunnallisia,
        sum(laajuus) filter (where tunnustettu_rahoituksen_piirissa and (pakollinen or valtakunnallinen)) tunnustut_pakolliset_ja_valtakunnalliset_rahoitus,
        sum(laajuus) filter (where tunnustettu_rahoituksen_piirissa and pakollinen) pakollisia_tunnustettuja_rahoituksen_piirissa,
        sum(laajuus) filter (where valtakunnallinen and tunnustettu_rahoituksen_piirissa) valtakunnallisia_tunnustettuja_rahoituksen_piirissa,
        sum(laajuus) filter (where korotettu_eri_vuonna) eri_vuonna_korotettuja
      from (
        select
          oppilaitos_oid,
          osasuoritus.arviointi_paiva,
          oppimaara_koodiarvo,
          tunnustettu,
          tunnustettu = false as suoritettu,
          tunnustettu_rahoituksen_piirissa,
          koulutusmoduuli_kurssin_tyyppi,
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen' as pakollinen,
          koulutusmoduuli_paikallinen = false as valtakunnallinen,
          osasuoritus.koulutusmoduuli_laajuus_arvo as laajuus,
          opintojen_rahoitus,
          korotettu_eri_vuonna
        from #${s.name}.r_paatason_suoritus paatason_suoritus
          join #${s.name}.r_opiskeluoikeus opiskeluoikeus
            on paatason_suoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
          join #${s.name}.r_osasuoritus osasuoritus
            on paatason_suoritus.paatason_suoritus_id = osasuoritus.paatason_suoritus_id
            and osasuoritus.arviointi_arvosana_koodiarvo != 'O'
          left join #${s.name}.r_opiskeluoikeus_aikajakso opiskeluoikeus_aikajakso
            on opiskeluoikeus_aikajakso.opiskeluoikeus_oid = osasuoritus.opiskeluoikeus_oid
              and (osasuoritus.arviointi_paiva between opiskeluoikeus_aikajakso.alku and opiskeluoikeus_aikajakso.loppu)
        where paatason_suoritus.suorituksen_tyyppi = 'lukionaineopinnot'
          and osasuoritus.suorituksen_tyyppi in ('lukionvaltakunnallinenmoduuli', 'lukionpaikallinenopintojakso')
      ) moduulit
    group by
      oppilaitos_oid,
      arviointi_paiva,
      oppimaara_koodiarvo
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.lukion_aineopintojen_opintopistekertyma(oppilaitos_oid, arviointi_paiva)"


  private def queryAineopiskelija(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, oppimäärä: Oppimäärä) = {
    sql"""
      select
        r_organisaatio.nimi oppilaitos,
        aineopintojen_opintopistekertymat.*,
        coalesce(muuta_kautta_rahoitetut.yhteensa, 0) as muuta_kautta_rahoitetut,
        coalesce(rahoitusmuoto_ei_tiedossa.yhteensa, 0) as rahoitusmuoto_ei_tiedossa,
        coalesce(opiskeluoikeuden_ulkopuoliset.yhteensa, 0) as opiskeluoikeuden_ulkopuoliset
      from (
        select
          oppilaitos_oid,
          sum(yhteensa) yhteensa,
          sum(suoritettuja) suoritettuja,
          sum(tunnustettuja) tunnustettuja,
          sum(tunnustettuja_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa,
          sum(pakollisia_tai_valtakunnallisia) pakollisia_tai_valtakunnallisia,
          sum(pakollisia) pakollisia,
          sum(valtakunnallisia) valtakunnallisia,
          sum(suoritettuja_pakollisia_ja_valtakunnallisia) suoritettuja_pakollisia_ja_valtakunnallisia,
          sum(suoritettuja_pakollisia) suoritettuja_pakollisia,
          sum(suoritettuja_valtakunnallisia) suoritettuja_valtakunnallisia,
          sum(tunnustettuja_pakollisia_ja_valtakunnallisia) tunnustettuja_pakollisia_ja_valtakunnallisia,
          sum(tunnustettuja_pakollisia) tunnustettuja_pakollisia,
          sum(tunnustettuja_valtakunnallisia) tunnustettuja_valtakunnallisia,
          sum(tunnustut_pakolliset_ja_valtakunnalliset_rahoitus) tunnustut_pakolliset_ja_valtakunnalliset_rahoitus,
          sum(pakollisia_tunnustettuja_rahoituksen_piirissa) pakollisia_tunnustettuja_rahoituksen_piirissa,
          sum(valtakunnallisia_tunnustettuja_rahoituksen_piirissa) valtakunnallisia_tunnustettuja_rahoituksen_piirissa,
          sum(eri_vuonna_korotettuja) eri_vuonna_korotettuja
        from lukion_aineopintojen_opintopistekertyma
          where oppilaitos_oid = any($oppilaitosOids)
            and arviointi_paiva between $aikaisintaan and $viimeistaan
            and (oppimaara_koodiarvo = ${oppimäärä.arvo} or (${oppimäärä.arvo} = 'nuortenops' and oppimaara_koodiarvo is null))
          group by oppilaitos_oid
      ) aineopintojen_opintopistekertymat
      left join (
        select
          oppilaitos_oid,
          sum(r_osasuoritus.koulutusmoduuli_laajuus_arvo) yhteensa
        from osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella
          join r_osasuoritus on r_osasuoritus.osasuoritus_id = osasuoritus_arvioitu_opiskeluoikeuden_ulkopuolella.osasuoritus_id
          where oppilaitos_oid = any($oppilaitosOids)
            and osasuorituksen_tyyppi in ('lukionvaltakunnallinenmoduuli', 'lukionpaikallinenopintojakso')
            and paatason_suorituksen_tyyppi = 'lukionaineopinnot'
            and (osasuorituksen_arviointi_paiva between $aikaisintaan and $viimeistaan)
            and koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
            and (
              tunnustettu = false
              or tunnustettu_rahoituksen_piirissa
            )
            and (oppimaara_koodiarvo = ${oppimäärä.arvo} or (${oppimäärä.arvo} = 'nuortenops' and oppimaara_koodiarvo is null))
        group by oppilaitos_oid
      ) opiskeluoikeuden_ulkopuoliset
          on opiskeluoikeuden_ulkopuoliset.oppilaitos_oid = aineopintojen_opintopistekertymat.oppilaitos_oid
      left join (
        select
          oppilaitos_oid,
          sum(koulutusmoduuli_laajuus_arvo) yhteensa
        from lukion_aineopintojen_moduulien_rahoitusmuodot
        where opintojen_rahoitus = '6'
          and oppilaitos_oid = any($oppilaitosOids)
          and (arviointi_paiva between $aikaisintaan and $viimeistaan)
          and (oppimaara_koodiarvo = ${oppimäärä.arvo} or (${oppimäärä.arvo} = 'nuortenops' and oppimaara_koodiarvo is null))
          and osasuorituksen_tyyppi = 'lukionvaltakunnallinenmoduuli'
        group by oppilaitos_oid
      ) muuta_kautta_rahoitetut
          on muuta_kautta_rahoitetut.oppilaitos_oid = aineopintojen_opintopistekertymat.oppilaitos_oid
      left join (
        select
          oppilaitos_oid,
          sum(koulutusmoduuli_laajuus_arvo) yhteensa
        from lukion_aineopintojen_moduulien_rahoitusmuodot
        where opintojen_rahoitus is null
          and oppilaitos_oid = any($oppilaitosOids)
          and (arviointi_paiva between $aikaisintaan and $viimeistaan)
          and (oppimaara_koodiarvo = ${oppimäärä.arvo} or (${oppimäärä.arvo} = 'nuortenops' and oppimaara_koodiarvo is null))
        group by oppilaitos_oid
      ) rahoitusmuoto_ei_tiedossa
          on rahoitusmuoto_ei_tiedossa.oppilaitos_oid = aineopintojen_opintopistekertymat.oppilaitos_oid
      join r_organisaatio on aineopintojen_opintopistekertymat.oppilaitos_oid = r_organisaatio.organisaatio_oid
    """.as[Lukio2019OpintopistekertymaAineopiskelijaRow]
  }

  implicit private val getResult: GetResult[Lukio2019OpintopistekertymaAineopiskelijaRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    Lukio2019OpintopistekertymaAineopiskelijaRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitos = rs.getString("oppilaitos"),
      opintopisteitaYhteensa = rs.getInt("yhteensa"),
      suoritettujaOpintopisteita = rs.getInt("suoritettuja"),
      tunnustettujaOpintopisteita = rs.getInt("tunnustettuja"),
      tunnustettujaOpintopisteita_rahoituksenPiirissa = rs.getInt("tunnustettuja_rahoituksen_piirissa"),
      pakollisia_tai_valtakunnallisia = rs.getInt("pakollisia_tai_valtakunnallisia"),
      pakollisiaOpintopisteita = rs.getInt("pakollisia"),
      valtakunnallisiaOpintopisteita = rs.getInt("valtakunnallisia"),
      suoritettujaPakollisia_ja_suoritettujaValtakunnallisia = rs.getInt("suoritettuja_pakollisia_ja_valtakunnallisia"),
      suoritettujaPakollisiaOpintopisteita = rs.getInt("suoritettuja_pakollisia"),
      suoritettujaValtakunnallisiaOpintopisteita = rs.getInt("suoritettuja_valtakunnallisia"),
      tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisia = rs.getInt("tunnustettuja_pakollisia_ja_valtakunnallisia"),
      tunnustettujaPakollisiaOpintopisteita = rs.getInt("tunnustettuja_pakollisia"),
      tunnustettujaValtakunnallisiaOpintopisteita = rs.getInt("tunnustettuja_valtakunnallisia"),
      tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisia = rs.getInt("tunnustut_pakolliset_ja_valtakunnalliset_rahoitus"),
      tunnustettuja_rahoituksenPiirissa_pakollisia = rs.getInt("pakollisia_tunnustettuja_rahoituksen_piirissa"),
      tunnustettuja_rahoituksenPiirissa_valtakunnallisia = rs.getInt("valtakunnallisia_tunnustettuja_rahoituksen_piirissa"),
      suoritetutTaiRahoitetut_muutaKauttaRahoitetut = rs.getInt("muuta_kautta_rahoitetut"),
      suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa = rs.getInt("rahoitusmuoto_ei_tiedossa"),
      suoritetutTaiRahoitetut_eiOpiskeluoikeudenSisalla = rs.getInt("opiskeluoikeuden_ulkopuoliset"),
      eriVuonnaKorotettujaOpintopisteita = rs.getInt("eri_vuonna_korotettuja"),
    )
  })

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column(t.get("raportti-excel-kolumni-oppilaitosOid")),
    "oppilaitos" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "opintopisteitaYhteensa" -> Column(t.get("raportti-excel-kolumni-opintopisteitaYhteensa"), comment = Some(t.get("raportti-excel-kolumni-opintopisteitaYhteensa-comment"))),
    "suoritettujaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-yhteensäSuoritettujaOpintopisteitä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäSuoritettujaOpintopisteitä-lukio-comment"))),
    "tunnustettujaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitä-lukio-comment"))),
    "tunnustettujaOpintopisteita_rahoituksenPiirissa" -> Column(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitäRahoituksenPiirissä"), comment = Some(t.get("raportti-excel-kolumni-yhteensäTunnistettujaOpintopisteitäRahoituksenPiirissä-lukio-comment"))),
    "pakollisia_tai_valtakunnallisia" -> Column(t.get("raportti-excel-kolumni-pakollisiaTaiValtakunnallisia"), comment = Some(t.get("raportti-excel-kolumni-pakollisiaTaiValtakunnallisia-comment"))),
    "pakollisiaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-pakollisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-pakollisiaOpintopisteita-comment"))),
    "valtakunnallisiaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-valtakunnallisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-valtakunnallisiaOpintopisteita-comment"))),
    "suoritettujaPakollisia_ja_suoritettujaValtakunnallisia" -> Column(t.get("raportti-excel-kolumni-suoritettujaPakollisiaJaSuoritettujaValtakunnallisia"), comment = Some(t.get("raportti-excel-kolumni-suoritettujaPakollisiaJaSuoritettujaValtakunnallisia-comment"))),
    "suoritettujaPakollisiaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-suoritettujaPakollisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-suoritettujaPakollisiaOpintopisteita-comment"))),
    "suoritettujaValtakunnallisiaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-suoritettujaValtakunnallisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-suoritettujaValtakunnallisiaOpintopisteita-comment"))),
    "tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisia" -> Column(t.get("raportti-excel-kolumni-tunnustettujaPakollisiaJaTunnustettujaValtakunnallisia"), comment = Some(t.get("raportti-excel-kolumni-tunnustettujaPakollisiaJaTunnustettujaValtakunnallisia-comment"))),
    "tunnustettujaPakollisiaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-tunnustettujaPakollisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-tunnustettujaPakollisiaOpintopisteita-comment"))),
    "tunnustettujaValtakunnallisiaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-tunnustettujaValtakunnallisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-tunnustettujaValtakunnallisiaOpintopisteita-comment"))),
    "tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisia" -> Column(t.get("raportti-excel-kolumni-tunnustettujaRahoituksenPiirissaPakollisiaJaValtakunnallisia"), comment = Some(t.get("raportti-excel-kolumni-tunnustettujaRahoituksenPiirissaPakollisiaJaValtakunnallisia-comment"))),
    "tunnustettuja_rahoituksenPiirissa_pakollisia" -> Column(t.get("raportti-excel-kolumni-tunnustettujaRahoituksenPiirissaPakollisiaOpintopisteita"), comment = Some(t.get("raportti-excel-kolumni-tunnustettujaRahoituksenPiirissaPakollisiaOpintopisteita-comment"))),
    "tunnustettuja_rahoituksenPiirissa_valtakunnallisia" -> Column(t.get("raportti-excel-kolumni-tunnustettujaRahoituksenPiirissaValtakunnallisia"), comment = Some(t.get("raportti-excel-kolumni-tunnustettujaRahoituksenPiirissaValtakunnallisia-comment"))),
    "suoritetutTaiRahoitetut_muutaKauttaRahoitetut" -> Column(t.get("raportti-excel-kolumni-suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetutOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetutOpintopisteet-lukio-comment"))),
    "suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa" -> Column(t.get("raportti-excel-kolumni-suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoaOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoaOpintopisteet-lukio-comment"))),
    "suoritetutTaiRahoitetut_eiOpiskeluoikeudenSisalla" -> Column(t.get("raportti-excel-kolumni-suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossaOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossaOpintopisteet-lukio-comment"))),
    "eriVuonnaKorotettujaOpintopisteita" -> Column(t.get("raportti-excel-kolumni-eriVuonnaKorotetutOpintopisteet"))
  )
}

case class Lukio2019OpintopistekertymaAineopiskelijaRow(
  oppilaitosOid: String,
  oppilaitos: String,
  opintopisteitaYhteensa: Int,
  suoritettujaOpintopisteita: Int,
  tunnustettujaOpintopisteita: Int,
  tunnustettujaOpintopisteita_rahoituksenPiirissa: Int,
  pakollisia_tai_valtakunnallisia: Int,
  pakollisiaOpintopisteita: Int,
  valtakunnallisiaOpintopisteita: Int,
  suoritettujaPakollisia_ja_suoritettujaValtakunnallisia: Int,
  suoritettujaPakollisiaOpintopisteita: Int,
  suoritettujaValtakunnallisiaOpintopisteita: Int,
  tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisia: Int,
  tunnustettujaPakollisiaOpintopisteita: Int,
  tunnustettujaValtakunnallisiaOpintopisteita: Int,
  tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisia: Int,
  tunnustettuja_rahoituksenPiirissa_pakollisia: Int,
  tunnustettuja_rahoituksenPiirissa_valtakunnallisia: Int,
  suoritetutTaiRahoitetut_muutaKauttaRahoitetut: Int,
  suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa: Int,
  suoritetutTaiRahoitetut_eiOpiskeluoikeudenSisalla: Int,
  eriVuonnaKorotettujaOpintopisteita: Int
)
