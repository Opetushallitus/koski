package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportit.YleissivistäväUtils._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration._


sealed trait AikuistenPerusopetusRaporttiType {
  def typeName: String
  def päätasonSuoritusTyyppi: String
  def isOppiaineenOppimäärä(päätasonSuoritus: RPäätasonSuoritusRow): Boolean
  def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean
  def isKurssi(osasuoritus: ROsasuoritusRow): Boolean
}

object AikuistenPerusopetusAlkuvaiheRaportti extends AikuistenPerusopetusRaporttiType {
  val typeName = "alkuvaihe"
  val päätasonSuoritusTyyppi: String = "aikuistenperusopetuksenoppimaaranalkuvaihe"

  def isOppiaineenOppimäärä(päätasonSuoritus: RPäätasonSuoritusRow): Boolean = false

  def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean = {
    osasuoritus.suorituksenTyyppi == "aikuistenperusopetuksenalkuvaiheenoppiaine"
  }

  def isKurssi(osasuoritus: ROsasuoritusRow): Boolean = {
    osasuoritus.suorituksenTyyppi == "aikuistenperusopetuksenalkuvaiheenkurssi"
  }
}

object AikuistenPerusopetusPäättövaiheRaportti extends AikuistenPerusopetusRaporttiType {
  val typeName = "päättövaihe"
  val päätasonSuoritusTyyppi: String = "aikuistenperusopetuksenoppimaara"

  def isOppiaineenOppimäärä(päätasonSuoritus: RPäätasonSuoritusRow): Boolean = false

  def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean = {
    osasuoritus.suorituksenTyyppi == "aikuistenperusopetuksenoppiaine"
  }

  def isKurssi(osasuoritus: ROsasuoritusRow): Boolean = {
    osasuoritus.suorituksenTyyppi == "aikuistenperusopetuksenkurssi"
  }
}

object AikuistenPerusopetusOppiaineenOppimääräRaportti extends AikuistenPerusopetusRaporttiType {
  val typeName = "oppiaineenoppimäärä"
  val päätasonSuoritusTyyppi: String = "perusopetuksenoppiaineenoppimaara"

  def isOppiaineenOppimäärä(päätasonSuoritus: RPäätasonSuoritusRow): Boolean = {
    päätasonSuoritus.suorituksenTyyppi == päätasonSuoritusTyyppi
  }

  def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean = {
    List(
      "aikuistenperusopetuksenalkuvaiheenoppiaine",
      "aikuistenperusopetuksenoppiaine"
    ).contains(osasuoritus.suorituksenTyyppi)
  }

  def isKurssi(osasuoritus: ROsasuoritusRow): Boolean = {
    List(
      "aikuistenperusopetuksenalkuvaiheenkurssi",
      "aikuistenperusopetuksenkurssi"
    ).contains(osasuoritus.suorituksenTyyppi)
  }
}

object AikuistenPerusopetusRaportti {
  def makeRaporttiType(typeName: String): Either[String, AikuistenPerusopetusRaporttiType] = {
    typeName match {
      case "alkuvaihe" => Right(AikuistenPerusopetusAlkuvaiheRaportti)
      case "päättövaihe" => Right(AikuistenPerusopetusPäättövaiheRaportti)
      case "oppiaineenoppimäärä" => Right(AikuistenPerusopetusOppiaineenOppimääräRaportti)
      case _ => Left(s"${typeName} on epäkelpo aikuisten perusopetuksen raportin tyyppi")
    }
  }
}

case class AikuistenPerusopetusRaportti(
  repository: AikuistenPerusopetusRaporttiRepository,
  raporttiType: AikuistenPerusopetusRaporttiType,
  oppilaitosOid: Organisaatio.Oid,
  alku: LocalDate,
  loppu: LocalDate,
  osasuoritustenAikarajaus: Boolean
) extends GlobalExecutionContext {

  def build(): Seq[DynamicDataSheet] = {
    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu, osasuoritusMukanaAikarajauksessa, raporttiType.päätasonSuoritusTyyppi)
    val oppiaineetJaKurssit = opetettavatOppiaineetJaNiidenKurssit(raporttiType.isOppiaineenOppimäärä, raporttiType.isOppiaine, rows)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
      kurssit <- oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit)
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def osasuoritusMukanaAikarajauksessa(row: ROsasuoritusRow): Boolean = {
    !osasuoritustenAikarajaus || !raporttiType.isKurssi(row) || arvioituAikavälillä(alku, loppu)(row)
  }

  private def dropDataColumnByTitle(
    rows: Seq[Seq[Any]], columnSettings: Seq[Column], columnTitle: String
  ): (Seq[Seq[Any]], Seq[Column]) = {
    val columnIndex = columnSettings.indexWhere(_.title == columnTitle)
    if (columnIndex < 0) {
      throw new RuntimeException(s"Column '${columnTitle}' not found in report data")
    }
    (
      rows.map(_.patch(columnIndex, Nil, 1)),
      columnSettings.patch(columnIndex, Nil, 1)
    )
  }

  private def removeUnwantedColumns(rows: Seq[Seq[Any]], columnSettings: Seq[Column]): (Seq[Seq[Any]], Seq[Column]) = {
    raporttiType match {
      case AikuistenPerusopetusAlkuvaiheRaportti =>
        dropDataColumnByTitle(rows, columnSettings, "Opiskeluoikeudella alkuvaiheen suoritus")
      case AikuistenPerusopetusPäättövaiheRaportti =>
        dropDataColumnByTitle(rows, columnSettings, "Opiskeluoikeudella päättövaiheen suoritus")
      case AikuistenPerusopetusOppiaineenOppimääräRaportti =>
        (rows, columnSettings)
    }
  }

  private def oppiaineJaLisätiedotSheet(opiskeluoikeusData: Seq[AikuistenPerusopetusRaporttiRows], oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    Future {
      val (rows, columnSettings) = removeUnwantedColumns(
        opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu)),
        oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit)
      )

      DynamicDataSheet(
        title = "Oppiaineet ja lisätiedot",
        columnSettings = columnSettings,
        rows = rows
      )
    }
  }

  private def oppiaineKohtaisetSheetit(rows: Seq[AikuistenPerusopetusRaporttiRows], oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit]) = {
    Future {
      oppiaineetJaKurssit.map(oppiaineKohtainenSheet(_, rows))
    }
  }

  private def oppiaineKohtainenSheet(oppiaineJaKurssit: YleissivistäväRaporttiOppiaineJaKurssit, data: Seq[AikuistenPerusopetusRaporttiRows]) = {
    val oppiaine = oppiaineJaKurssit.oppiaine
    val kurssit = oppiaineJaKurssit.kurssit
    val filtered = data.filter(notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine))

    DynamicDataSheet(
      title = oppiaine.toSheetTitle,
      rows = filtered.map(oppiainekohtaisetKurssitiedot(_, kurssit)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(kurssit)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: YleissivistäväRaporttiOppiaine)(data: AikuistenPerusopetusRaporttiRows) = {
    !raporttiType.isOppiaineenOppimäärä(data.päätasonSuoritus) || data.päätasonSuoritus.matchesWith(oppiaine)
  }

  private def kaikkiOppiaineetVälilehtiRow(row: AikuistenPerusopetusRaporttiRows, oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val data = AikuistenPerusopetusRaporttiKaikkiOppiaineetVälilehtiRow(
      muut = AikuistenPerusopetusRaporttiOppiaineetVälilehtiMuut(
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
        opiskeluoikeuden_tilat_aikajakson_aikana = removeContinuousSameTila(row.aikajaksot).map(_.tila).mkString(", "),
        suorituksenTyyppi = row.päätasonSuoritus.suorituksenTyyppi,
        alkuvaiheenSuorituksia = row.hasAlkuvaihe,
        päättövaiheenSuorituksia = row.hasPäättövaihe,
        tutkintokoodi = row.päätasonSuoritus.koulutusmoduuliKoodiarvo,
        suorituksenNimi = row.päätasonSuoritus.koulutusmoduuliNimi,
        suorituksenTila = row.päätasonSuoritus.vahvistusPäivä.fold("kesken")(_ => "valmis"),
        suorituksenVahvistuspäivä = row.päätasonSuoritus.vahvistusPäivä.map(_.toLocalDate),
        läsnäolopäiviä_aikajakson_aikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
        rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(", "),
        rahoitusmuodotOk = rahoitusmuodotOk(row),
        ryhmä = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "ryhmä"),
        ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        majoitusetu = lisätiedot.flatMap(_.majoitusetu).map(lengthInDaysInDateRange(_, alku, loppu)),
        sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        yhteislaajuus = row.osasuoritukset
          .filter(raporttiType.isKurssi)
          .map(_.laajuus).sum,
        yhteislaajuusSuoritetut = row.osasuoritukset
          .filter(raporttiType.isKurssi)
          .filterNot(isTunnustettu)
          .map(_.laajuus).sum,
        yhteislaajuusHylätyt = row.osasuoritukset
          .filter(raporttiType.isKurssi)
          .filterNot(k => isTunnustettu(k) || k.suoritettu)
          .map(_.laajuus).sum,
        yhteislaajuusTunnustetut = row.osasuoritukset
          .filter(k => raporttiType.isKurssi(k) && k.suoritettu && isTunnustettu(k))
          .map(_.laajuus).sum
      ),
      oppiaineet = oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet, raporttiType.isOppiaineenOppimäärä)
    )
    data.toSeq
  }

  private def oppiainekohtaisetKurssitiedot(row: AikuistenPerusopetusRaporttiRows, kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    AikuistenPerusopetusRaporttiOppiaineRow(
      staticColumns = AikuistenPerusopetusRaporttiOppiaineTabStaticColumns(
        oppijanOid = row.opiskeluoikeus.oppijaOid,
        hetu = row.henkilo.hetu,
        sukinimi = row.henkilo.sukunimi,
        etunimet = row.henkilo.etunimet,
        toimipiste = row.päätasonSuoritus.toimipisteNimi,
        suorituksenTyyppi = row.päätasonSuoritus.suorituksenTyyppi
      ),
      kurssit = kurssienTiedot(row.osasuoritukset, kurssit)
    )
  }

  private def kurssienTiedot(osasuoritukset: Seq[ROsasuoritusRow], kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    val osasuorituksetMap = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
    kurssit.map { kurssi =>
      osasuorituksetMap.getOrElse(kurssi.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(kurssi)).map(kurssisuoritus =>
        AikuistenPerusopetusKurssinTiedot(
          arvosana = kurssisuoritus.arviointiArvosanaKoodiarvo,
          laajuus = kurssisuoritus.koulutusmoduuliLaajuusArvo,
          tunnustettu = isTunnustettu(kurssisuoritus)
        ).toString
      ).mkString(", ")
    }
  }

  private def isTunnustettu(kurssisuoritus: ROsasuoritusRow) = {
    JsonSerializer.extract[Option[OsaamisenTunnustaminen]](kurssisuoritus.data \ "tunnustettu").isDefined
  }

  private def alkuvaiheenSuorituksiaColumn() = {
    val comment = raporttiType match {
      case AikuistenPerusopetusOppiaineenOppimääräRaportti =>
        Some("Opiskeluoikeudelle siirretty myös alkuvaiheen suoritus. HUOM! Alkuvaiheen suorituksia ei tulisi olla samassa opiskeluoikeudessa aineopintojen kanssa.")
      case _ => None
    }
    CompactColumn("Opiskeluoikeudella alkuvaiheen suoritus", comment = comment)
  }

  private def päättövaiheenSuorituksiaColumn() = {
    val comment = raporttiType match {
      case AikuistenPerusopetusOppiaineenOppimääräRaportti =>
        Some("Opiskeluoikeudelle siirretty myös päättövaiheen suoritus. HUOM! Päättövaiheen suorituksia ei tulisi olla samassa opiskeluoikeudessa aineopintojen kanssa.")
      case _ => None
    }
    CompactColumn("Opiskeluoikeudella päättövaiheen suoritus", comment = comment)
  }

  private def oppiaineJaLisätiedotColumnSettings(oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit]) = {
    val yleisetColumns = Seq(
      CompactColumn("Opiskeluoikeuden oid"),
      CompactColumn("Lähdejärjestelmä"),
      CompactColumn("Koulutustoimija"),
      CompactColumn("Oppilaitoksen nimi"),
      CompactColumn("Toimipiste"),
      CompactColumn("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
      CompactColumn("Päivitetty", comment = Some("Viimeisin opiskeluoikeuden päivitys KOSKI-palveluun. HUOM. Raportilla näkyy vain edeltävän päivän tilanne.")),
      CompactColumn("Yksilöity", comment = Some("Jos sarakkeessa on arvo 'ei' eikä oppijalla ole henkilötunnusta, koulutuksen järjestäjän KOSKI-pääkäyttäjän on suoritettava oppijan yksilöinti oppijanumerorekisterissä. Jos yksilöimättömällä oppijalla on henkilötunnus, ole yhteydessä KOSKI-palveluosoitteeseen (koski@opintopolku.fi).")),
      Column("Oppijan oid"),
      Column("Hetu"),
      Column("Sukunimi"),
      Column("Etunimet"),
      CompactColumn("Opiskeluoikeuden alkamispäivä"),
      CompactColumn("Opiskeluoikeuden viimeisin tila", comment = Some("Se opiskeluoikeuden tila, joka opiskeluoikeudella on nyt.")),
      CompactColumn("Opiskeluoikeuden tilat aikajakson aikana", comment = Some("Kaikki opiskeluoikeuden tilat, joita opiskeluoikeudella on ollut raportin tulostusparametreissa määritellyn aikajakson aikana. Tilat näytetään pilkulla erotettuna aikajärjestyksessä.")),
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä aikuisten perusopetuksen alkuvaiheen, päättövaiheen vai aineopintojen suoritus.")),
      alkuvaiheenSuorituksiaColumn(),
      päättövaiheenSuorituksiaColumn(),
      CompactColumn("Tutkintokoodi/koulutusmoduulin koodi", comment = Some("Päätason suorituksen koulutusmoduulin koodiarvo")),
      CompactColumn("Suorituksen nimi", comment = Some("Päätason suorituksen koulutusmoduulin nimi")),
      CompactColumn("Suorituksen tila", comment = Some("Onko kyseinen päätason suoritus \"kesken\" vai \"valmis\".")),
      CompactColumn("Suorituksen vahvistuspäivä", comment = Some("Päätason suorituksen vahvistuspäivä. Vain \"valmis\"-tilaisilla suorituksilla on tässä kentässä jokin päivämäärä.")),
      CompactColumn("Läsnäolopäiviä aikajakson aikana", comment = Some("Kuinka monta kalenteripäivää oppija on ollut raportin tulostusparametreissa määriteltynä aikajaksona \"Läsnä\"-tilassa KOSKI-palvelussa.")),
      CompactColumn("Rahoitukset", comment = Some("Ne rahoituskoodit, jotka opiskeluoikeuden läsnäolojaksoille on siirretty. Rahoituskoodit erotettu pilkulla toisistaan ja järjestetty aikajärjestyksessä. 1 = Valtionosuusrahoitteinen koulutus, 6 = Muuta kautta rahoitettu.")),
      CompactColumn("Läsnä/valmistunut-rahoitusmuodot syötetty", comment = Some("Sarake kertoo, onko opiskeluoikeuden kaikille läsnä- ja valmistunut-jaksoille syötetty rahoitustieto")),
      CompactColumn("Ryhmä"),
      CompactColumn("Ulkomaanjaksot", comment = Some("Kuinka monta ulkomaanjaksopäivää oppijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Majoitusetu", comment = Some("Kuinka monta majoitusetupäivää oppijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Sisäoppilaitosmainen majoitus", comment = Some("Kuinka monta päivää oppija on ollut KOSKI-datan mukaan sisäoppilaitosmaisessa majoituksessa raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Yhteislaajuus (kaikki kurssit)", comment = Some("Opintojen yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien kurssien määrän tai tulostusparametreissa määriteltynä aikajaksona arvioiduiksi merkittyjen kurssien määrän riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (suoritetut kurssit)", comment = Some("Suoritettujen kurssien (eli sellaisten kurssien, jotka eivät ole tunnustettuja aikaisemman osaamisen pohjalta) yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien suoritettujen kurssien yhteislaajuuden tai tulostusparametreissa määriteltynä aikajaksona suoritettujen kurssien yhteislaajuuden riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (hylätyllä arvosanalla suoritetut kurssit)", comment = Some("Hylätyllä arvosanalla suoritettujen kurssien yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien suoritettujen kurssien yhteislaajuuden tai tulostusparametreissa määriteltynä aikajaksona suoritettujen kurssien yhteislaajuuden riippuen siitä, mitä tulostusparametreissa on valittu.")),
      CompactColumn("Yhteislaajuus (tunnustetut kurssit)", comment = Some("Tunnustettujen kurssien yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista. Sarake näyttää joko kaikkien opiskeluoikeudelta löytyvien tunnustettujen kurssien yhteislaajuuden tai tulostusparametreissa määriteltynä aikajaksona arvioiduiksi merkittyjen kurssien yhteislaajuuden riippuen siitä, mitä tulostusparametreissa on valittu."))
    )

    val oppiaineColumns = oppiaineet.map(x =>
      CompactColumn(title = x.oppiaine.toColumnTitle, comment = Some("Otsikon nimessä näytetään ensin oppiaineen koodi, sitten oppiaineen nimi ja viimeiseksi tieto, onko kyseessä valtakunnallinen vai paikallinen oppiaine (esim. BI Biologia valtakunnallinen). Sarakkeen arvossa näytetään pilkulla erotettuna oppiaineelle siirretty arvosana ja oppiaineessa suoritettujen kurssien määrä."))
    )

    yleisetColumns ++ oppiaineColumns
  }

  private def oppiaineKohtaisetColumnSettings(kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    Seq(
      Column("Oppijan oid"),
      Column("Hetu"),
      Column("Sukunimi"),
      Column("Etunimet"),
      CompactColumn("Toimipiste"),
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä aikuisten perusopetuksen alkuvaiheen, päättövaiheen vai aineopintojen suoritus."))
    ) ++ kurssit.map(k => CompactColumn(title = k.toColumnTitle, comment = Some("Otsikon nimessä näytetään ensin kurssin koodi, sitten kurssin nimi ja viimeiseksi tieto siitä, onko kurssi valtakunnallinen vai paikallinen. Kurssisarake sisältää aina seuraavat tiedot, jos opiskelijalla on kyseisen kurssi suoritettuna: arvosana, kurssin laajuus ja \"tunnustettu\" jos kyseinen kurssi on tunnustettu.")))
  }
}

case class AikuistenPerusopetusRaporttiKaikkiOppiaineetVälilehtiRow(
  muut: AikuistenPerusopetusRaporttiOppiaineetVälilehtiMuut,
  oppiaineet: Seq[String]
) {
  def toSeq: Seq[Any] = muut.productIterator.toList ++ oppiaineet
}

case class AikuistenPerusopetusRaporttiOppiaineetVälilehtiMuut(
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
  suorituksenTyyppi: String,
  alkuvaiheenSuorituksia: Boolean,
  päättövaiheenSuorituksia: Boolean,
  tutkintokoodi: String,
  suorituksenNimi: Option[String],
  suorituksenTila: String,
  suorituksenVahvistuspäivä: Option[LocalDate],
  läsnäolopäiviä_aikajakson_aikana: Int,
  rahoitukset: String,
  rahoitusmuodotOk: Boolean,
  ryhmä: Option[String],
  ulkomaanjaksot: Option[Int],
  majoitusetu: Option[Int],
  sisäoppilaitosmainenMajoitus: Option[Int],
  yhteislaajuus: BigDecimal,
  yhteislaajuusSuoritetut: BigDecimal,
  yhteislaajuusHylätyt: BigDecimal,
  yhteislaajuusTunnustetut: BigDecimal
)

case class AikuistenPerusopetusRaporttiOppiaineRow(
  staticColumns: AikuistenPerusopetusRaporttiOppiaineTabStaticColumns,
  kurssit: Seq[String]) {
  def toSeq: Seq[Any] = staticColumns.productIterator.toList ++ kurssit
}

case class AikuistenPerusopetusRaporttiOppiaineTabStaticColumns(
  oppijanOid: String,
  hetu: Option[String],
  sukinimi: String,
  etunimet: String,
  toimipiste: String,
  suorituksenTyyppi: String
)

case class AikuistenPerusopetusKurssinTiedot(arvosana: Option[String], laajuus: Option[Double], tunnustettu: Boolean) {
  override def toString: String = {
    List(
      Some(arvosana.map("Arvosana " + _).getOrElse("Ei arvosanaa")),
      Some(laajuus.map("Laajuus " + _).getOrElse("Ei laajuutta")),
      if (tunnustettu) Some("Tunnustettu") else None
    ).flatten.mkString(", ")
  }
}
