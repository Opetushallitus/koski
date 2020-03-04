package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.YleissivistäväUtils._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration._


case class AikuistenPerusopetusRaportti(repository: AikuistenPerusopetusRaporttiRepository) extends GlobalExecutionContext {
  def build(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean
  ): Seq[DynamicDataSheet] = {
    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu, osasuoritustenAikarajaus)
    val oppiaineetJaKurssit = opetettavatOppiaineetJaNiidenKurssit(isOppiaineenOppimäärä, isOppiaine, rows)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
      kurssit <- oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit)
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def isOppiaineenOppimäärä(päätasonSuoritus: RPäätasonSuoritusRow): Boolean = {
    päätasonSuoritus.suorituksenTyyppi == "perusopetuksenoppiaineenoppimaara"
  }

  private def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean = {
    List(
      "aikuistenperusopetuksenoppiaine",
      "aikuistenperusopetuksenalkuvaiheenoppiaine"
    ).contains(osasuoritus.suorituksenTyyppi)
  }

  private def oppiaineJaLisätiedotSheet(opiskeluoikeusData: Seq[AikuistenPerusopetusRaporttiRows], oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    Future {
      DynamicDataSheet(
        title = "Oppiaineet ja lisätiedot",
        rows = opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu)).map(_.toSeq),
        columnSettings = oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit)
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
    !isOppiaineenOppimäärä(data.päätasonSuoritus) || data.päätasonSuoritus.matchesWith(oppiaine)
  }

  private def kaikkiOppiaineetVälilehtiRow(row: AikuistenPerusopetusRaporttiRows, oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")

    AikuistenPerusopetusRaporttiKaikkiOppiaineetVälilehtiRow(
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
        tutkintokoodi = row.päätasonSuoritus.koulutusmoduuliKoodiarvo,
        suorituksenNimi = row.päätasonSuoritus.koulutusmoduuliNimi,
        suorituksenTila = row.päätasonSuoritus.vahvistusPäivä.fold("kesken")(_ => "valmis"),
        suorituksenVahvistuspäivä = row.päätasonSuoritus.vahvistusPäivä.map(_.toLocalDate),
        läsnäolopäiviä_aikajakson_aikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
        rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(", "),
        ryhmä = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "ryhmä"),
        ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        majoitusetu = lisätiedot.flatMap(_.majoitusetu).map(lengthInDaysInDateRange(_, alku, loppu)),
        sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        yhteislaajuus = row.osasuoritukset.filter(_.suorituksenTyyppi == "aikuistenperusopetuksenkurssi").flatMap(_.koulutusmoduuliLaajuusArvo.map(_.toDouble)).sum
      ),
      oppiaineet = oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet, isOppiaineenOppimäärä)
    )
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
          tunnustettu = JsonSerializer.extract[Option[OsaamisenTunnustaminen]](kurssisuoritus.data \ "tunnustettu").isDefined
        ).toString
      ).mkString(", ")
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
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä koko oppimäärän suoritus vai aineopintosuoritus.")),
      CompactColumn("Tutkintokoodi/koulutusmoduulin koodi", comment = Some("Päätason suorituksen koulutusmoduulin koodiarvo")),
      CompactColumn("Suorituksen nimi", comment = Some("Päätason suorituksen koulutusmoduulin nimi")),
      CompactColumn("Suorituksen tila", comment = Some("Onko kyseinen päätason suoritus \"kesken\" vai \"valmis\".")),
      CompactColumn("Suorituksen vahvistuspäivä", comment = Some("Päätason suorituksen vahvistuspäivä. Vain \"valmis\"-tilaisilla suorituksilla on tässä kentässä jokin päivämäärä.")),
      CompactColumn("Läsnäolopäiviä aikajakson aikana", comment = Some("Kuinka monta kalenteripäivää oppija on ollut raportin tulostusparametreissa määriteltynä aikajaksona \"Läsnä\"-tilassa KOSKI-palvelussa.")),
      CompactColumn("Rahoitukset", comment = Some("Rahoituskoodit aikajärjestyksessä, joita opiskeluoikeuden läsnäolojaksoille on siirretty. Rahoituskoodien nimiarvot koodistossa https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
      CompactColumn("Ryhmä"),
      CompactColumn("Ulkomaanjaksot", comment = Some("Kuinka monta ulkomaanjaksopäivää oppijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Majoitusetu", comment = Some("Kuinka monta majoitusetupäivää oppijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Sisäoppilaitosmainen majoitus", comment = Some("Kuinka monta päivää oppija on ollut KOSKI-datan mukaan sisäoppilaitosmaisessa majoituksessa raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Yhteislaajuus", comment = Some("Suoritettujen opintojen yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista."))
    ) ++ oppiaineet.map(x => CompactColumn(title = x.oppiaine.toColumnTitle, comment = Some("Otsikon nimessä näytetään ensin oppiaineen koodi, sitten oppiaineen nimi ja viimeiseksi tieto, onko kyseessä valtakunnallinen vai paikallinen oppiaine (esim. BI Biologia valtakunnallinen). Sarakkeen arvossa näytetään pilkulla erotettuna oppiaineelle siirretty arvosana ja oppiaineessa suoritettujen kurssien määrä.")))
  }

  private def oppiaineKohtaisetColumnSettings(kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    Seq(
      Column("Oppijan oid"),
      Column("Hetu"),
      Column("Sukunimi"),
      Column("Etunimet"),
      CompactColumn("Toimipiste"),
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä koko oppimäärän suoritus vai aineopintosuoritus."))
    ) ++ kurssit.map(k => CompactColumn(title = k.toColumnTitle, comment = Some("Otsikon nimessä näytetään ensin kurssin koodi, sitten kurssin nimi ja viimeiseksi tieto siitä, onko kurssi valtakunnallinen vai paikallinen. Kurssisarake sisältää aina seuraavat tiedot, jos opiskelijalla on kyseisen kurssi suoritettuna: kurssityyppi (pakollinen, syventävä, soveltava), arvosana, kurssin laajuus ja \"tunnustettu\" jos kyseinen kurssi on tunnustettu.")))
  }
}

case class AikuistenPerusopetusRaporttiKaikkiOppiaineetVälilehtiRow(
  muut: AikuistenPerusopetusRaporttiOppiaineetVälilehtiMuut,
  oppiaineet: Seq[String]) {
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
  tutkintokoodi: String,
  suorituksenNimi: Option[String],
  suorituksenTila: String,
  suorituksenVahvistuspäivä: Option[LocalDate],
  läsnäolopäiviä_aikajakson_aikana: Int,
  rahoitukset: String,
  ryhmä: Option[String],
  ulkomaanjaksot: Option[Int],
  majoitusetu: Option[Int],
  sisäoppilaitosmainenMajoitus: Option[Int],
  yhteislaajuus: Double
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
