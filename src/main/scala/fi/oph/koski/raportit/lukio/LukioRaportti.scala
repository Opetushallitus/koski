package fi.oph.koski.raportit.lukio

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.YleissivistäväUtils._
import fi.oph.koski.raportit._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class LukioRaportti(repository: LukioRaportitRepository, t: LocalizationReader) extends GlobalExecutionContext {

  private def isOppiaineenOppimäärä(päätasonSuoritus: RPäätasonSuoritusRow) = {
    päätasonSuoritus.suorituksenTyyppi == "lukionoppiaineenoppimaara"
  }

  private def isOppiaine(osasuoritus: ROsasuoritusRow) = {
    List(
      "lukionoppiaine",
      "lukionmuuopinto"
    ).contains(osasuoritus.suorituksenTyyppi)
  }

  def buildRaportti(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean
  ): Seq[DynamicDataSheet] = {
    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu, osasuoritustenAikarajaus)
    val oppiaineetJaKurssit = opetettavatOppiaineetJaNiidenKurssit(isOppiaineenOppimäärä, isOppiaine, rows, t)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
      kurssit <- oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit)
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def oppiaineJaLisätiedotSheet(opiskeluoikeusData: Seq[LukioRaporttiRows], oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    Future {
      DynamicDataSheet(
        title = "Oppiaineet ja lisätiedot",
        rows = opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu)).map(_.toSeq),
        columnSettings = oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit)
      )
    }
  }

  private def oppiaineKohtaisetSheetit(rows: Seq[LukioRaporttiRows], oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit]) = {
    Future {
      oppiaineetJaKurssit.map(oppiaineKohtainenSheet(_, rows))
    }
  }

  private def oppiaineKohtainenSheet(oppiaineJaKurssit: YleissivistäväRaporttiOppiaineJaKurssit, data: Seq[LukioRaporttiRows]) = {
    val oppiaine = oppiaineJaKurssit.oppiaine
    val kurssit = oppiaineJaKurssit.kurssit
    val filtered = data.filter(notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine))

    DynamicDataSheet(
      title = oppiaine.toSheetTitle(t),
      rows = filtered.map(oppiainekohtaisetKurssitiedot(_, kurssit)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(kurssit)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: YleissivistäväRaporttiOppiaine)(data: LukioRaporttiRows) = {
    !isOppiaineenOppimäärä(data.päätasonSuoritus) || data.päätasonSuoritus.matchesWith(oppiaine, t.language)
  }

  private def kaikkiOppiaineetVälilehtiRow(row: LukioRaporttiRows, oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val lukionKurssit = row.osasuoritukset.filter(_.suorituksenTyyppi == "lukionkurssi")
    val oppimääräSuoritettu = JsonSerializer.extract[Option[Boolean]](row.opiskeluoikeus.data \ "oppimääräSuoritettu")

    LukioRaporttiKaikkiOppiaineetVälilehtiRow(
     muut = LukioRaporttiOppiaineetVälilehtiMuut(
       opiskeluoikeudenOid = row.opiskeluoikeus.opiskeluoikeusOid,
       lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
       koulutustoimija = row.opiskeluoikeus.koulutustoimijaNimi,
       oppilaitoksenNimi = row.opiskeluoikeus.oppilaitosNimi,
       toimipiste = row.päätasonSuoritus.toimipisteNimi,
       opiskeluoikeuden_tunniste_lähdejärjestelmässä = lähdejärjestelmänId.flatMap(_.id),
       aikaleima = row.opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
       yksiloity = row.henkilo.yksiloity,
       oppijanOid = row.opiskeluoikeus.oppijaOid,
       hetu = row.henkilo.hetu,
       sukunimi = row.henkilo.sukunimi,
       etunimet = row.henkilo.etunimet,
       opiskeluoikeuden_alkamispäivä = row.opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
       opiskeluoikeuden_viimeisin_tila = row.opiskeluoikeus.viimeisinTila,
       opiskeluoikeuden_tilat_aikajakson_aikana = removeContinuousSameTila(row.aikajaksot).map(_.tila).mkString(","),
       opetussuunnitelma = opetussuunnitelma(row.päätasonSuoritus),
       suorituksenTyyppi = row.päätasonSuoritus.suorituksenTyyppi,
       suorituksenTila = row.päätasonSuoritus.vahvistusPäivä.fold("kesken")(_ => "valmis"),
       suorituksenVahvistuspäivä = row.päätasonSuoritus.vahvistusPäivä.map(_.toLocalDate),
       oppimääräSuoritettu = oppimääräSuoritettu.getOrElse(false),
       läsnäolopäiviä_aikajakson_aikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
       rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(","),
       rahoitusmuodotOk = rahoitusmuodotOk(row),
       ryhmä = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "ryhmä"),
       pidennettyPäättymispäivä = lisätiedot.exists(_.pidennettyPäättymispäivä),
       ulkomainenVaihtoOpiskelija = lisätiedot.exists(_.ulkomainenVaihtoopiskelija),
       ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       erityisen_koulutustehtävän_tehtävät = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.flatMap(_.tehtävä.nimi.map(_.get("fi"))).mkString(","))),
       erityisen_koulutustehtävän_jaksot = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       yhteislaajuus = lukionKurssit.map(_.laajuus).sum,
       yhteislaajuusSuoritetut = lukionKurssit
         .filterNot(k => k.tunnustettu)
         .map(_.laajuus).sum,
       yhteislaajuusHylätyt = lukionKurssit
         .filterNot(k => k.tunnustettu || k.arvioituJaHyväksytty)
         .map(_.laajuus).sum,
       yhteislaajuusTunnustetut = lukionKurssit
         .filter(k => k.arvioituJaHyväksytty && k.tunnustettu)
         .map(_.laajuus).sum,
       yhteislaajuusKorotettuEriVuonna = lukionKurssit
         .filter(_.korotettuEriVuonna)
         .map(_.laajuus).sum
     ),
      oppiaineet = oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet, isOppiaineenOppimäärä, t)
    )
  }

  private def oppiainekohtaisetKurssitiedot(row:LukioRaporttiRows, kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    LukioRaportinOppiaineenKurssitRow(
      stattisetKolumnit = LukioOppiaineenKurssienVälilehtiStaattisetKolumnit(
        oppijanOid = row.opiskeluoikeus.oppijaOid,
        hetu = row.henkilo.hetu,
        sukinimi =  row.henkilo.sukunimi,
        etunimet = row.henkilo.etunimet,
        toimipiste = row.päätasonSuoritus.toimipisteNimi,
        opetussuunnitelma = opetussuunnitelma(row.päätasonSuoritus),
        suorituksenTyyppi = row.päätasonSuoritus.suorituksenTyyppi
      ),
      kurssit = kurssienTiedot(row.osasuoritukset, kurssit)
    )
  }

  private def kurssienTiedot(osasuoritukset: Seq[ROsasuoritusRow], kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    val osasuorituksetMap = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
    kurssit.map { kurssi =>
      osasuorituksetMap.getOrElse(kurssi.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(kurssi, t.language)).map(kurssisuoritus =>
        LukioKurssinTiedot(
          kurssintyyppi = JsonSerializer.extract[Option[Koodistokoodiviite]](kurssisuoritus.data \ "koulutusmoduuli" \ "kurssinTyyppi").map(_.koodiarvo),
          arvosana = kurssisuoritus.arviointiArvosanaKoodiarvo,
          laajuus = kurssisuoritus.koulutusmoduuliLaajuusArvo,
          tunnustettu = kurssisuoritus.tunnustettu,
          korotettuEriVuonna = kurssisuoritus.korotettuEriVuonna
        ).toString
      ).mkString(",")
    }
  }

  private def opetussuunnitelma(suoritus: RPäätasonSuoritusRow) = {
    if (isOppiaineenOppimäärä(suoritus)) {
      JsonSerializer.extract[Option[String]](suoritus.data \ "koulutusmoduuli" \ "perusteenDiaarinumero").map {
        case "60/011/2015" | "33/011/2003" => "Lukio suoritetaan nuorten opetussuunnitelman mukaan"
        case "70/011/2015" | "4/011/2004" => "Lukio suoritetaan aikuisten opetussuunnitelman mukaan"
        case diaarinumero => diaarinumero
      }
    } else {
      JsonSerializer.extract[Option[Koodistokoodiviite]](suoritus.data \ "oppimäärä").flatMap(_.nimi.map(_.get("fi")))
    }
  }

  private def oppiaineJaLisätiedotColumnSettings(oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit]) = {
    Seq(
      CompactColumn("Opiskeluoikeuden oid"),
      CompactColumn("Lähdejärjestelmä"),
      CompactColumn("Koulutustoimija"),
      CompactColumn("Oppilaitoksen nimi"),
      CompactColumn("Toimipiste"),
      CompactColumn("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
      CompactColumn("Päivitetty", comment = Some("Viimeisin opiskeluoikeuden päivitys KOSKI-palveluun. HUOM. Raportilla näkyy vain edeltävän päivän tilanne.")),
      CompactColumn("Yksilöity", comment = Some("Jos tässä on arvo 'ei', tulee oppija yksilöidä oppijanumerorekisterissä")),
      Column("Oppijan oid"),
      Column("Hetu"),
      Column("Sukunimi"),
      Column("Etunimet"),
      CompactColumn("Opiskeluoikeuden alkamispäivä"),
      CompactColumn("Opiskeluoikeuden viimeisin tila", comment = Some("Se opiskeluoikeuden tila, joka opiskeluoikeudella on nyt.")),
      CompactColumn("Opiskeluoikeuden tilat aikajakson aikana", comment = Some("Kaikki opiskeluoikeuden tilat, joita opiskeluoikeudella on ollut aikajaksona aikana. Tilat näyteään pilkuilla erotettuna aikajärjestyksessä.")),
      CompactColumn("Opetussuunnitelma", comment = Some("Suoritetaanko lukio oppimäärää tai oppiaineen oppimäärää nuorten vai aikuisten opetussuunnitelman mukaan.")),
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä koko lukion oppimäärän suoritus (\"lukionoppimaara\") vai aineopintosuoritus (\"lukionoppiaineenoppimaara\").")),
      CompactColumn("Suorituksen tila", comment = Some("Onko kyseinen päätason suoritus (lukion oppimäärä tai oppiaineen oppimäärä) \"kesken\" vai \"valmis\".")),
      CompactColumn("Suorituksen vahvistuspäivä", comment = Some("Päätason suorituksen (lukion oppimäärä tai oppiaineen oppimäärä) vahvistuspäivä. Vain \"valmis\"-tilaisilla suorituksilla on tässä kentässä jokin päivämäärä.")),
      CompactColumn("Oppimäärä suoritettu", comment = Some("Tieto siitä, onko lukion oppimäärän suorittamiseen vaadittavat opinnot suoritettu. Jos lukion oppimäärän suoritus on vahvistettu, lukion oppimäärän suorittamiseen vaadittavat opinnot tulkitaan automaattisesti suoritetuksi, jolloin tähän kenttään tallennetaan automaattisesti arvo 'true'.")),
      CompactColumn("Läsnäolopäiviä aikajakson aikana", comment = Some("Kuinka monta kalenteripäivää opiskelija on ollut raportin tulostusparametreissa määriteltynä aikajaksona \"Läsnä\"-tilassa KOSKI-palvelussa.")),
      CompactColumn("Rahoitukset", comment = Some("Rahoituskoodit aikajärjestyksessä, joita opiskeluoikeuden läsnäolojaksoille on siirretty. Rahoituskoodien nimiarvot koodistossa https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
      CompactColumn("Läsnä/valmistunut-rahoitusmuodot syötetty", comment = Some("Sarake kertoo, onko opiskeluoikeuden kaikille läsnä- ja valmistunut-jaksoille syötetty rahoitustieto")),
      CompactColumn("Ryhmä"),
      CompactColumn("Pidennetty päättymispäivä", comment = Some("Onko opiskelijalle merkitty opiskeluoikeuden lisätietoihin, että hänellä on pidennetty päättymispäivä (kyllä/ei).")),
      CompactColumn("Ulkomainen vaihto-opiskelija", comment = Some("Onko kyseesä ulkomainen vaihto-opiskelija (kyllä/ei).")),
      CompactColumn("Ulkomaanjaksot", comment = Some("Kuinka monta ulkomaanjaksopäivää opiskelijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Erityisen koulutustehtävän tehtävät", comment = Some("Erityiset koulutustehtävät, jotka opiskelijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Erityisen koulutustehtävän jaksot", comment = Some("Kuinka monta erityisen koulutustehtävän jaksopäivää opiskelijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Sisäoppilaitosmainen majoitus", comment = Some("Kuinka monta päivää opiskelija on ollut KOSKI-datan mukaan sisäoppilaitosmaisessa majoituksessa raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Yhteislaajuus (kaikki kurssit)", comment = Some("Opintojen yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien kurssien määrän tai tulostusparametreissa määriteltynä aikajaksona arvioiduiksi merkittyjen kurssien määrän riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (suoritetut kurssit)", comment = Some("Suoritettujen kurssien (eli sellaisten kurssien, jotka eivät ole tunnustettuja aikaisemman osaamisen pohjalta) yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien suoritettujen kurssien yhteislaajuuden tai tulostusparametreissa määriteltynä aikajaksona suoritettujen kurssien yhteislaajuuden riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)", comment = Some("Hylätyllä arvosanalla suoritettujen kurssien yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien suoritettujen kurssien yhteislaajuuden tai tulostusparametreissa määriteltynä aikajaksona suoritettujen kurssien yhteislaajuuden riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (tunnustetut kurssit)", comment = Some("Tunnustettujen kurssien yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien tunnustettujen kurssien yhteislaajuuden tai tulostusparametreissa määriteltynä aikajaksona arvioiduiksi merkittyjen kurssien yhteislaajuuden riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (eri vuonna korotetut kurssit)", comment = Some("Kurssit, joiden arviointia on korotettuu eri vuonna, kuin jona kurssin ensimmäinen arviointi on annettu."))
    ) ++ oppiaineet.map(x => CompactColumn(title = x.oppiaine.toColumnTitle(t), comment = Some("Otsikon nimessä näytetään ensin oppiaineen koodi, sitten oppiaineen nimi ja viimeiseksi tieto, onko kyseessä valtakunnallinen vai paikallinen oppiaine (esim. BI Biologia valtakunnallinen). Sarakkeen arvossa näytetään pilkulla erotettuna oppiaineelle siirretty arvosana ja oppiaineessa suoritettujen kurssien määrä.")))
  }

  private def oppiaineKohtaisetColumnSettings(kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    Seq(
      Column("Oppijan oid"),
      Column("Hetu"),
      Column("Sukunimi"),
      Column("Etunimet"),
      CompactColumn("Toimipiste"),
      CompactColumn("Opetussuunnitelma", comment = Some("Suoritetaanko lukio oppimäärää tai oppiaineen oppimäärää nuorten vai aikuisten opetussuunnitelman mukaan.")),
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä koko lukion oppimäärän suoritus (\"lukionoppimaara\") vai aineopintosuoritus (\"lukionoppiaineenoppimaara\")."))
    ) ++ kurssit.map(k => CompactColumn(title = k.toColumnTitle(t), comment = Some("Otsikon nimessä näytetään ensin kurssin koodi, sitten kurssin nimi ja viimeiseksi tieto siitä, onko kurssi valtakunnallinen vai paikallinen. Kurssisarake sisältää aina seuraavat tiedot, jos opiskelijalla on kyseisen kurssi suoritettuna: kurssityyppi (pakollinen, syventävä, soveltava), arvosana, kurssin laajuus ja \"tunnustettu\" jos kyseinen kurssi on tunnustettu.")))
  }
}

case class LukioRaporttiOppiaineetVälilehtiMuut(
  opiskeluoikeudenOid: String,
  lähdejärjestelmä: Option[String],
  koulutustoimija: String,
  oppilaitoksenNimi: String,
  toimipiste: String,
  opiskeluoikeuden_tunniste_lähdejärjestelmässä: Option[String],
  aikaleima: LocalDate,
  yksiloity: Boolean,
  oppijanOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  opiskeluoikeuden_alkamispäivä: Option[LocalDate],
  opiskeluoikeuden_viimeisin_tila: Option[String],
  opiskeluoikeuden_tilat_aikajakson_aikana: String,
  opetussuunnitelma: Option[String],
  suorituksenTyyppi: String,
  suorituksenTila: String,
  suorituksenVahvistuspäivä: Option[LocalDate],
  oppimääräSuoritettu: Boolean,
  läsnäolopäiviä_aikajakson_aikana: Int,
  rahoitukset: String,
  rahoitusmuodotOk: Boolean,
  ryhmä: Option[String],
  pidennettyPäättymispäivä: Boolean,
  ulkomainenVaihtoOpiskelija: Boolean,
  ulkomaanjaksot: Option[Int],
  erityisen_koulutustehtävän_tehtävät: Option[String],
  erityisen_koulutustehtävän_jaksot: Option[Int],
  sisäoppilaitosmainenMajoitus: Option[Int],
  yhteislaajuus: BigDecimal,
  yhteislaajuusSuoritetut: BigDecimal,
  yhteislaajuusHylätyt: BigDecimal,
  yhteislaajuusTunnustetut: BigDecimal,
  yhteislaajuusKorotettuEriVuonna: BigDecimal
)

case class LukioRaporttiKaikkiOppiaineetVälilehtiRow(muut: LukioRaporttiOppiaineetVälilehtiMuut, oppiaineet: Seq[String]) {
 def toSeq: Seq[Any] = muut.productIterator.toList ++ oppiaineet
}

case class LukioOppiaineenKurssienVälilehtiStaattisetKolumnit(
  oppijanOid: String,
  hetu: Option[String],
  sukinimi: String,
  etunimet: String,
  toimipiste: String,
  opetussuunnitelma: Option[String],
  suorituksenTyyppi: String
)

case class LukioKurssinTiedot(kurssintyyppi: Option[String], arvosana: Option[String], laajuus: Option[Double], tunnustettu: Boolean, korotettuEriVuonna: Boolean) {
  override def toString: String = {
    List(
      Some(kurssintyyppi.getOrElse("Ei tyyppiä").capitalize),
      Some(arvosana.map("Arvosana " + _).getOrElse("Ei arvosanaa")),
      Some(laajuus.map("Laajuus " + _).getOrElse("Ei laajuutta")),
      if (tunnustettu) Some("Tunnustettu") else None,
      if (korotettuEriVuonna) Some("Korotettu eri vuonna") else None
    ).flatten.mkString(", ")
  }
}

case class LukioRaportinOppiaineenKurssitRow(stattisetKolumnit: LukioOppiaineenKurssienVälilehtiStaattisetKolumnit, kurssit: Seq[String]) {
  def toSeq: Seq[Any] = stattisetKolumnit.productIterator.toList ++ kurssit
}
