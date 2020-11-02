package fi.oph.koski.raportit

import java.sql.ResultSet
import java.time.LocalDate

import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult

object LukioOppiaineenOppimaaranKurssikertymat extends DatabaseConverters {
  val sheetTitle = "Aineopiskelijat"

  def datasheet(oppilaitosOids: List[String], jaksonAlku: LocalDate, jaksonLoppu: LocalDate, raportointiDatabase: RaportointiDatabase): DataSheet = {
    DataSheet(
      sheetTitle,
      rows = raportointiDatabase.runDbSync(queryAineopiskelija(oppilaitosOids, jaksonAlku, jaksonLoppu)),
      columnSettings
    )
  }

  private def queryAineopiskelija(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate) = {
    sql"""
      select
        r_opiskeluoikeus.oppilaitos_oid,
        r_opiskeluoikeus.oppilaitos_nimi oppilaitos,
        count(*) yhteensa,
        count(*) filter (where tunnustettu = false) suoritettuja,
        count(*) filter (where tunnustettu) tunnustettuja,
        count(*) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa,
        count(*) filter (where
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
           or
          (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false)
        ) pakollisia_tai_valtakunnallisia_syventavia,
        count(*) filter (where koulutusmoduuli_kurssin_tyyppi = 'pakollinen') pakollisia,
        count(*) filter (where
          koulutusmoduuli_paikallinen = false and
          koulutusmoduuli_kurssin_tyyppi = 'syventava'
        ) valtakunnallisia_syventavia,
        count(*) filter (where
          tunnustettu = false and (
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
            or
          (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false))
        ) suoritettuja_pakollisia_ja_valtakunnallisia_syventavia,
        count(*) filter (where
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen' and
          tunnustettu = false
        ) suoritettuja_pakollisia,
        count(*) filter (where
          koulutusmoduuli_paikallinen = false and
          koulutusmoduuli_kurssin_tyyppi = 'syventava' and
          tunnustettu = false
        ) suoritettuja_valtakunnallisia_syventavia,
        count(*) filter (where
          tunnustettu and (
            koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
            or (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false)
          )
        ) tunnustettuja_pakollisia_ja_valtakunnallisia_syventavia,
        count(*) filter (where
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen' and
          tunnustettu
        ) tunnustettuja_pakollisia,
        count(*) filter (where
          koulutusmoduuli_paikallinen = false and
          koulutusmoduuli_kurssin_tyyppi = 'syventava' and
          tunnustettu
        ) tunnustettuja_valtakunnallisia_syventavia,
        count(*) filter (where
          tunnustettu_rahoituksen_piirissa and (
           koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
            or
           (koulutusmoduuli_kurssin_tyyppi = 'syventava' and koulutusmoduuli_paikallinen = false))
        ) tunnustut_pakolliset_ja_valtakunnalliset_syventavat_rahoitus,
        count(*) filter (where
          tunnustettu_rahoituksen_piirissa and
          koulutusmoduuli_kurssin_tyyppi = 'pakollinen'
        ) pakollisia_tunnustettuja_rahoituksen_piirissa,
        count(*) filter (where
          koulutusmoduuli_paikallinen = false and
          koulutusmoduuli_kurssin_tyyppi = 'syventava' and
          tunnustettu_rahoituksen_piirissa
        ) "valtakunnallisia_syventavia_tunnustettuja_rahoituksen_piirissa",
        count(*) filter (where
          koulutusmoduuli_paikallinen = false
          and (tunnustettu = false or tunnustettu_rahoituksen_piirissa)
          and r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and opintojen_rahoitus = '6'
        ) suoritetut_tai_rahoitetut_muuta_kautta_rahoitetut,
        count(*) filter (where
          koulutusmoduuli_paikallinen = false
          and (tunnustettu = false or tunnustettu_rahoituksen_piirissa)
          and r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
          and opintojen_rahoitus is null
        ) suoritetut_tai_rahoitetut_rahoitusmuoto_ei_tiedossa
      from r_osasuoritus
      join r_paatason_suoritus
        on r_paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
      join r_opiskeluoikeus
        on r_opiskeluoikeus.opiskeluoikeus_oid = r_osasuoritus.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso
        on r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid = r_osasuoritus.opiskeluoikeus_oid
        and r_opiskeluoikeus_aikajakso.alku <= r_osasuoritus.arviointi_paiva
        and r_opiskeluoikeus_aikajakso.loppu >= r_osasuoritus.arviointi_paiva
      where r_paatason_suoritus.suorituksen_tyyppi = 'lukionoppiaineenoppimaara'
        and r_osasuoritus.suorituksen_tyyppi = 'lukionkurssi'
        and r_opiskeluoikeus_aikajakso.tila = 'lasna'
        and r_osasuoritus.arviointi_paiva >= $aikaisintaan
        and r_osasuoritus.arviointi_paiva <= $viimeistaan
        and r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOids)
      group by
        r_opiskeluoikeus.oppilaitos_oid,
        r_opiskeluoikeus.oppilaitos_nimi;
    """.as[LukioKurssikertymaAineopiskelijaRow]
  }

  implicit private val getResult: GetResult[LukioKurssikertymaAineopiskelijaRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    LukioKurssikertymaAineopiskelijaRow(
      oppilaitosOid = rs.getString("oppilaitos_oid"),
      oppilaitos = rs.getString("oppilaitos"),
      kurssejaYhteensa = rs.getInt("yhteensa"),
      suoritettujaKursseja = rs.getInt("suoritettuja"),
      tunnustettujaKursseja = rs.getInt("tunnustettuja"),
      tunnustettujaKursseja_rahoituksenPiirissa = rs.getInt("tunnustettuja_rahoituksen_piirissa"),
      pakollisia_tai_valtakunnallisiaSyventavia = rs.getInt("pakollisia_tai_valtakunnallisia_syventavia"),
      pakollisiaKursseja = rs.getInt("pakollisia"),
      valtakunnallisestiSyventaviaKursseja = rs.getInt("valtakunnallisia_syventavia"),
      suoritettujaPakollisia_ja_suoritettujaValtakunnallisiaSyventavia = rs.getInt("suoritettuja_pakollisia_ja_valtakunnallisia_syventavia"),
      suoritettujaPakollisiaKursseja = rs.getInt("suoritettuja_pakollisia"),
      suoritettujaValtakunnallisiaSyventaviaKursseja = rs.getInt("suoritettuja_valtakunnallisia_syventavia"),
      tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisiaSyventavia = rs.getInt("tunnustettuja_pakollisia_ja_valtakunnallisia_syventavia"),
      tunnustettujaPakollisiaKursseja = rs.getInt("tunnustettuja_pakollisia"),
      tunnustettujaValtakunnallisiaSyventaviaKursseja = rs.getInt("tunnustettuja_valtakunnallisia_syventavia"),
      tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisiaSyventavia = rs.getInt("tunnustut_pakolliset_ja_valtakunnalliset_syventavat_rahoitus"),
      tunnustettuja_rahoituksenPiirissa_pakollisia = rs.getInt("pakollisia_tunnustettuja_rahoituksen_piirissa"),
      tunnustettuja_rahoituksenPiirissa_valtakunnallisiaSyventaiva = rs.getInt("valtakunnallisia_syventavia_tunnustettuja_rahoituksen_piirissa"),
      suoritetutTaiRahoitetut_muutaKauttaRahoitetut = rs.getInt("suoritetut_tai_rahoitetut_muuta_kautta_rahoitetut"),
      suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa = rs.getInt("suoritetut_tai_rahoitetut_rahoitusmuoto_ei_tiedossa")
    )
  })

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column("Oppilaitoksen oid-tunniste"),
    "oppilaitos" -> Column("Oppilaitos"),
    "kurssejaYhteensa" -> Column("Kursseja yhteensä", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään")),
    "suoritettujaKursseja" -> Column("Suoritetut kurssit", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "tunnustettujaKursseja" -> Column("Tunnustetut kurssit", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "tunnustettujaKursseja_rahoituksenPiirissa" -> Column("Tunnustetut kurssit - rahoituksen piirissä", comment = Some("Kaikki sellaiset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
    "pakollisia_tai_valtakunnallisiaSyventavia" -> Column("Pakollisia ja valtakunnallisia syventäviä kursseja yhteensä", comment = Some("Kaikki pakolliset ja valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "pakollisiaKursseja" -> Column("Pakollisia kursseja yhteensä", comment = Some("Kaikki pakolliset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "valtakunnallisestiSyventaviaKursseja" -> Column("Valtakunnallisia syventäviä kursseja yhteensä", comment = Some("Kaikki valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "suoritettujaPakollisia_ja_suoritettujaValtakunnallisiaSyventavia" -> Column("Suoritettuja pakollisia ja valtakunnallisia syventäviä kursseja", comment = Some("Kaikki pakolliset ja valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "suoritettujaPakollisiaKursseja" -> Column("Suoritettuja pakollisia kursseja", comment = Some("Kaikki pakolliset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "suoritettujaValtakunnallisiaSyventaviaKursseja" -> Column("Suoritettuja valtakunnallisia syventäviä kursseja", comment = Some("Kaikki valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisiaSyventavia" -> Column("Tunnustettuja pakollisia ja valtakunnallisia syventäviä kursseja", comment = Some("Kaikki pakolliset ja valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "tunnustettujaPakollisiaKursseja" -> Column("Tunnustettuja pakollisia kursseja", comment = Some("Kaikki pakolliset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "tunnustettujaValtakunnallisiaSyventaviaKursseja" -> Column("Tunnustettuja valtakunnallisia syventäviä kursseja", comment = Some("Kaikki valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisiaSyventavia" -> Column("Tunnustetuista pakollisista ja valtakunnallisista syventävistä kursseista rahoituksen piirissä", comment = Some("Kaikki pakolliset ja valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty sekä tunnustetuiksi että rahoituksen piirissä oleviksi.")),
    "tunnustettuja_rahoituksenPiirissa_pakollisia" -> Column("Tunnustetuista pakollisista kursseista rahoituksen piirissä", comment = Some("Kaikki pakolliset kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty sekä tunnustetuiksi että rahoituksen piirissä oleviksi.")),
    "tunnustettuja_rahoituksenPiirissa_valtakunnallisiaSyventaiva" -> Column("Tunnustetuista valtakunnallisista syventävistä kursseista rahoituksen piirissä", comment = Some("Kaikki valtakunnalliset syventävät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty sekä tunnustetuiksi että rahoituksen piirissä oleviksi.")),
    "suoritetutTaiRahoitetut_muutaKauttaRahoitetut" -> Column("Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - muuta kautta rahoitetut", comment = Some("Aineopintojen suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut pakolliset tai valtakunnalliset syventävät kurssit, joiden arviointipäivä osuu muuta kautta rahoitetun läsnäolojakson sisälle. Kurssien tunnistetiedot löytyvät välilehdeltä ”Muuta kautta rah.”")),
    "suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa" -> Column("Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit, joilla ei rahoitustietoa", comment = Some("Aineopintojen suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut pakolliset tai valtakunnalliset syventävät kurssit, joiden arviointipäivä osuus sellaiselle tilajaksolle, jolta ei löydy tietoa rahoitusmuodosta. Kurssien tunnistetiedot löytyvät välilehdeltä ”Ei rahoitusmuotoa”."))
  )
}

case class LukioKurssikertymaAineopiskelijaRow(
  oppilaitosOid: String,
  oppilaitos: String,
  kurssejaYhteensa: Int,
  suoritettujaKursseja: Int,
  tunnustettujaKursseja: Int,
  tunnustettujaKursseja_rahoituksenPiirissa: Int,
  pakollisia_tai_valtakunnallisiaSyventavia: Int,
  pakollisiaKursseja: Int,
  valtakunnallisestiSyventaviaKursseja: Int,
  suoritettujaPakollisia_ja_suoritettujaValtakunnallisiaSyventavia: Int,
  suoritettujaPakollisiaKursseja: Int,
  suoritettujaValtakunnallisiaSyventaviaKursseja: Int,
  tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisiaSyventavia: Int,
  tunnustettujaPakollisiaKursseja: Int,
  tunnustettujaValtakunnallisiaSyventaviaKursseja: Int,
  tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisiaSyventavia: Int,
  tunnustettuja_rahoituksenPiirissa_pakollisia: Int,
  tunnustettuja_rahoituksenPiirissa_valtakunnallisiaSyventaiva: Int,
  suoritetutTaiRahoitetut_muutaKauttaRahoitetut: Int,
  suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa: Int
)
