package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportit.LukioRaporttiKurssitOrdering.lukioRaporttiKurssitOrdering
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration._

case class LukioRaportti(repository: LukioRaportitRepository) extends GlobalExecutionContext {

  private lazy val lukionoppiaineenoppimaara = "lukionoppiaineenoppimaara"
  private lazy val lukionoppiaine = "lukionoppiaine"
  private lazy val lukionmuuopinto = "lukionmuuopinto"

  def buildRaportti(oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[DynamicDataSheet] = {
    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu)
    val oppiaineetJaKurssit = lukiossaOpetettavatOppiaineetJaNiidenKurssit(rows)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
      kurssit <- oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit)
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def lukiossaOpetettavatOppiaineetJaNiidenKurssit(rows: Seq[LukioRaporttiRows]) = {
    rows.flatMap(oppianeetJaNiidenKurssit).groupBy(_.oppiaine).map { case (oppiaine, x) =>
      LukioRaporttiOppiaineJaKurssit(oppiaine, x.flatMap(_.kurssit).distinct.sorted(lukioRaporttiKurssitOrdering))
    }.toSeq.sorted(LukioRaporttiOppiaineetOrdering)
  }

  private def oppianeetJaNiidenKurssit(row: LukioRaporttiRows): Seq[LukioRaporttiOppiaineJaKurssit] = {
    if (row.päätasonSuoritus.suorituksenTyyppi == lukionoppiaineenoppimaara) {
     Seq(LukioRaporttiOppiaineJaKurssit(toOppiaine(row.päätasonSuoritus), row.osasuoritukset.map(toKurssi)))
    } else {
      oppiaineetJaNiidenKurssitOppimäärästä(row)
    }
  }

  private def oppiaineetJaNiidenKurssitOppimäärästä(row: LukioRaporttiRows): Seq[LukioRaporttiOppiaineJaKurssit] = {
    val kurssit = row.osasuoritukset.filter(_.ylempiOsasuoritusId.isDefined).groupBy(_.ylempiOsasuoritusId.get)
    val oppiaineet = row.osasuoritukset.filter(isLukionOppiaine)
    val combineOppiaineWithKurssit = (oppiaine: ROsasuoritusRow) => LukioRaporttiOppiaineJaKurssit(toOppiaine(oppiaine), kurssit.getOrElse(oppiaine.osasuoritusId, Nil).map(toKurssi))

    oppiaineet.map(combineOppiaineWithKurssit)
  }

  private def isLukionOppiaine(osasuoritus: ROsasuoritusRow) = osasuoritus.suorituksenTyyppi == lukionoppiaine || osasuoritus.suorituksenTyyppi == lukionmuuopinto

  private def toOppiaine(row: RSuoritusRow) = row match {
    case s: RPäätasonSuoritusRow => LukioRaporttiOppiaine(s.suorituksestaKäytettäväNimi.getOrElse("ei nimeä"), s.koulutusmoduuliKoodiarvo, !s.koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava"))
    case s: ROsasuoritusRow => LukioRaporttiOppiaine(s.suorituksestaKäytettäväNimi.getOrElse("ei nimeä"), s.koulutusmoduuliKoodiarvo, s.koulutusmoduuliPaikallinen)
  }
  private def toKurssi(row: ROsasuoritusRow) = LukioRaporttiKurssi(row.koulutusmoduuliNimi.getOrElse("ei nimeä"), row.koulutusmoduuliKoodiarvo, row.koulutusmoduuliPaikallinen)

  private def oppiaineJaLisätiedotSheet(opiskeluoikeusData: Seq[LukioRaporttiRows], oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    Future {
      DynamicDataSheet(
        title = "Oppiaineet ja lisätiedot",
        rows = opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu)).map(_.toSeq),
        columnSettings = oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit)
      )
    }
  }

  private def oppiaineKohtaisetSheetit(rows: Seq[LukioRaporttiRows], oppiaineetJaKurssit: Seq[LukioRaporttiOppiaineJaKurssit]) = {
    Future {
      oppiaineetJaKurssit.map(oppiaineKohtainenSheet(_, rows))
    }
  }

  private def oppiaineKohtainenSheet(oppiaineJaKurssit: LukioRaporttiOppiaineJaKurssit, data: Seq[LukioRaporttiRows]) = {
    val oppiaine = oppiaineJaKurssit.oppiaine
    val kurssit = oppiaineJaKurssit.kurssit
    val filtered = data.filter(notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine))

    DynamicDataSheet(
      title = oppiaine.toSheetTitle,
      rows = filtered.map(oppiainekohtaisetKurssitiedot(_, kurssit)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(kurssit)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: LukioRaporttiOppiaine)(data: LukioRaporttiRows) = {
    data.päätasonSuoritus.suorituksenTyyppi != lukionoppiaineenoppimaara || data.päätasonSuoritus.matchesWith(oppiaine)
  }

  private def kaikkiOppiaineetVälilehtiRow(row: LukioRaporttiRows, oppiaineet: Seq[LukioRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")

    LukioRaporttiKaikkiOppiaineetVälilehtiRow(
     muut = LukioRaporttiOppiaineetVälilehtiMuut(
       opiskeluoikeudenOid = row.opiskeluoikeus.opiskeluoikeusOid,
       lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
       koulutustoimija = row.opiskeluoikeus.koulutustoimijaNimi,
       oppilaitoksenNimi = row.opiskeluoikeus.oppilaitosNimi,
       toimipiste = row.päätasonSuoritus.toimipisteNimi,
       opiskeluoikeuden_tunniste_lähdejärjestelmässä = lähdejärjestelmänId.flatMap(_.id),
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
       läsnäolopäiviä_aikajakson_aikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
       rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(","),
       ryhmä = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "ryhmä"),
       pidennettyPäättymispäivä = lisätiedot.exists(_.pidennettyPäättymispäivä),
       ulkomainenVaihtoOpiskelija = lisätiedot.exists(_.ulkomainenVaihtoopiskelija),
       yksityisopiskelija = lisätiedot.exists(_.yksityisopiskelija),
       ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       erityisen_koulutustehtävän_tehtävät = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.flatMap(_.tehtävä.nimi.map(_.get("fi"))).mkString(","))),
       erityisen_koulutustehtävän_jaksot = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       syy_alle18vuotiaana_aloitettuun_opiskeluun_aikuisten_lukiokoulutuksessa = lisätiedot.flatMap(_.alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy.map(_.get("fi"))),
       yhteislaajuus = row.osasuoritukset.filter(_.suorituksenTyyppi == "lukionkurssi").flatMap(_.koulutusmoduuliLaajuusArvo.map(_.toDouble)).sum
     ),
      oppiaineet = oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet)
    )
  }

  private def oppiaineidentiedot(paatasonsuoritus: RPäätasonSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow], oppiaineet: Seq[LukioRaporttiOppiaineJaKurssit]): Seq[String] =  {
    val oppiaineentiedot = if (paatasonsuoritus.suorituksenTyyppi == lukionoppiaineenoppimaara) {
      oppiaineenOppimääränOppiaineenTiedot(paatasonsuoritus, osasuoritukset, oppiaineet)
    } else {
      oppimääränOppiaineenTiedot(osasuoritukset, oppiaineet)
    }

    oppiaineet.map(x => oppiaineentiedot(x.oppiaine).map(_.toString).mkString(","))
  }

  private def oppiaineenOppimääränOppiaineenTiedot(päätasonSuoritus: RPäätasonSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow], oppiaineet: Seq[LukioRaporttiOppiaineJaKurssit]): LukioRaporttiOppiaine => Seq[LukioOppiaineenTiedot] = {
    (oppiaine: LukioRaporttiOppiaine) => if (päätasonSuoritus.matchesWith(oppiaine)) { Seq(toLukioOppiaineenTiedot(päätasonSuoritus, osasuoritukset)) } else { Nil }
  }

  private def oppimääränOppiaineenTiedot(osasuoritukset: Seq[ROsasuoritusRow], oppiaineet: Seq[LukioRaporttiOppiaineJaKurssit]): LukioRaporttiOppiaine => Seq[LukioOppiaineenTiedot] = {
    val osasuorituksetMap = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
    val oppiaineenSuoritukset = (oppiaine: LukioRaporttiOppiaine) => osasuorituksetMap.getOrElse(oppiaine.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(oppiaine))
    (oppiaine: LukioRaporttiOppiaine) => oppiaineenSuoritukset(oppiaine).map(s => LukioOppiaineenTiedot(s.arviointiArvosanaKoodiarvo, osasuoritukset.count(_.ylempiOsasuoritusId.contains(s.osasuoritusId))))
  }

  private def toLukioOppiaineenTiedot(suoritus: RSuoritusRow, osasuoritukset: Seq[ROsasuoritusRow]) = {
    val laajuus = suoritus match {
      case _: RPäätasonSuoritusRow => osasuoritukset.size
      case s: ROsasuoritusRow => osasuoritukset.count(_.ylempiOsasuoritusId.contains(s.osasuoritusId))
    }
    LukioOppiaineenTiedot(suoritus.arviointiArvosanaKoodiarvo, laajuus)
  }

  private def oppiainekohtaisetKurssitiedot(row:LukioRaporttiRows, kurssit: Seq[LukioRaporttiKurssi]) = {
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

  private def kurssienTiedot(osasuoritukset: Seq[ROsasuoritusRow], kurssit: Seq[LukioRaporttiKurssi]) = {
    val osasuorituksetMap = osasuoritukset.groupBy(_.koulutusmoduuliKoodiarvo)
    kurssit.map { kurssi =>
      osasuorituksetMap.getOrElse(kurssi.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(kurssi)).map(kurssisuoritus =>
        LukioKurssinTiedot(
          kurssintyyppi = JsonSerializer.extract[Option[Koodistokoodiviite]](kurssisuoritus.data \ "koulutusmoduuli" \ "kurssinTyyppi").map(_.koodiarvo),
          arvosana = kurssisuoritus.arviointiArvosanaKoodiarvo,
          laajuus = kurssisuoritus.koulutusmoduuliLaajuusArvo,
          tunnustettu = JsonSerializer.extract[Option[OsaamisenTunnustaminen]](kurssisuoritus.data \ "tunnustettu").isDefined
        ).toString
      ).mkString(",")
    }
  }

  private def opetussuunnitelma(suoritus: RPäätasonSuoritusRow) = {
    if (suoritus.suorituksenTyyppi == lukionoppiaineenoppimaara) {
      JsonSerializer.extract[Option[String]](suoritus.data \ "koulutusmoduuli" \ "perusteenDiaarinumero").map {
        case "60/011/2015" | "33/011/2003" => "Lukio suoritetaan nuorten opetussuunnitelman mukaan"
        case "70/011/2015" | "4/011/2004" => "Lukio suoritetaan aikuisten opetussuunnitelman mukaan"
        case diaarinumero => diaarinumero
      }
    } else {
      JsonSerializer.extract[Option[Koodistokoodiviite]](suoritus.data \ "oppimäärä").flatMap(_.nimi.map(_.get("fi")))
    }
  }

  private[raportit] def removeContinuousSameTila(aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow]): Seq[ROpiskeluoikeusAikajaksoRow] = {
    if (aikajaksot.size < 2) {
      aikajaksot
    } else {
      val rest = aikajaksot.dropWhile(_.tila == aikajaksot.head.tila)
      aikajaksot.head +: removeContinuousSameTila(rest)
    }
  }

  private[raportit] def lengthInDaysInDateRange(jakso: Jakso, alku: LocalDate, loppu: LocalDate) = {
    val hakuvali = Aikajakso(alku, Some(loppu))
    if (jakso.overlaps(hakuvali)) {
      val start = if (jakso.alku.isBefore(alku)) alku else jakso.alku
      val end = if (jakso.loppu.exists(_.isBefore(loppu))) jakso.loppu.get else loppu
      ChronoUnit.DAYS.between(start, end).toInt + 1
    } else {
      0
    }
  }

  private def oppiaineJaLisätiedotColumnSettings(oppiaineet: Seq[LukioRaporttiOppiaineJaKurssit]) = {
    Seq(
      CompactColumn("Opiskeluoikeuden oid"),
      CompactColumn("Lähdejärjestelmä"),
      CompactColumn("Koulutustoimija"),
      CompactColumn("Oppilaitoksen nimi"),
      CompactColumn("Toimipiste"),
      CompactColumn("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
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
      CompactColumn("Läsnäolopäiviä aikajakson aikana", comment = Some("Kuinka monta kalenteripäivää opiskelija on ollut raportin tulostusparametreissa määriteltynä aikajaksona \"Läsnä\"-tilassa KOSKI-palvelussa.")),
      CompactColumn("Rahoitukset", comment = Some("Rahoituskoodit aikajärjestyksessä, joita opiskeluoikeuden läsnäolojaksoille on siirretty. Rahoituskoodien nimiarvot koodistossa https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
      CompactColumn("Ryhmä"),
      CompactColumn("Pidennetty päättymispäivä", comment = Some("Onko opiskelijalle merkitty opiskeluoikeuden lisätietoihin, että hänellä on pidennetty päättymispäivä (kyllä/ei).")),
      CompactColumn("Ulkomainen vaihto-opiskelija", comment = Some("Onko kyseesä ulkomainen vaihto-opiskelija (kyllä/ei).")),
      CompactColumn("Yksityisopiskelija", comment = Some("Onko kysessä yksityisopiskelija (kyllä/ei).")),
      CompactColumn("Ulkomaanjaksot", comment = Some("Kuinka monta ulkomaanjaksopäivää opiskelijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Erityisen koulutustehtävän tehtävät", comment = Some("Erityiset koulutustehtävät, jotka opiskelijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Erityisen koulutustehtävän jaksot", comment = Some("Kuinka monta erityisen koulutustehtävän jaksopäivää opiskelijalla on KOSKI-datan mukaan ollut raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Sisäoppilaitosmainen majoitus", comment = Some("Kuinka monta päivää opiskelija on ollut KOSKI-datan mukaan sisäoppilaitosmaisessa majoituksessa raportin tulostusparametreissa määritellyllä aikajaksolla.")),
      CompactColumn("Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa"),
      CompactColumn("Yhteislaajuus", comment = Some("Suoritettujen opintojen yhteislaajuus. Lasketaan yksittäisille kurssisuorituksille siirretyistä laajuuksista."))
    ) ++ oppiaineet.map(x => CompactColumn(title = x.oppiaine.toColumnTitle, comment = Some("Otsikon nimessä näytetään ensin oppiaineen koodi, sitten oppiaineen nimi ja viimeiseksi tieto, onko kyseessä valtakunnallinen vai paikallinen oppiaine (esim. BI Biologia valtakunnallinen). Sarakkeen arvossa näytetään pilkulla erotettuna oppiaineelle siirretty arvosana ja oppiaineessa suoritettujen kurssien määrä.")))
  }

  private def oppiaineKohtaisetColumnSettings(kurssit: Seq[LukioRaporttiKurssi]) = {
    Seq(
      Column("Oppijan oid"),
      Column("Hetu"),
      Column("Sukunimi"),
      Column("Etunimet"),
      CompactColumn("Toimipiste"),
      CompactColumn("Opetussuunnitelma", comment = Some("Suoritetaanko lukio oppimäärää tai oppiaineen oppimäärää nuorten vai aikuisten opetussuunnitelman mukaan.")),
      CompactColumn("Suorituksen tyyppi", comment = Some("Onko kyseessä koko lukion oppimäärän suoritus (\"lukionoppimaara\") vai aineopintosuoritus (\"lukionoppiaineenoppimaara\")."))
    ) ++ kurssit.map(k => CompactColumn(title = k.toColumnTitle, comment = Some("Otsikon nimessä näytetään ensin kurssin koodi, sitten kurssin nimi ja viimeiseksi tieto siitä, onko kurssi valtakunnallinen vai paikallinen. Kurssisarake sisältää aina seuraavat tiedot, jos opiskelijalla on kyseisen kurssi suoritettuna: kurssityyppi (pakollinen, syventävä, soveltava), arvosana, kurssin laajuus ja \"tunnustettu\" jos kyseinen kurssi on tunnustettu.")))
  }
}

case class LukioRaporttiOppiaineetVälilehtiMuut(
  opiskeluoikeudenOid: String,
  lähdejärjestelmä: Option[String],
  koulutustoimija: String,
  oppilaitoksenNimi: String,
  toimipiste: String,
  opiskeluoikeuden_tunniste_lähdejärjestelmässä: Option[String],
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
  läsnäolopäiviä_aikajakson_aikana: Int,
  rahoitukset: String,
  ryhmä: Option[String],
  pidennettyPäättymispäivä: Boolean,
  ulkomainenVaihtoOpiskelija: Boolean,
  yksityisopiskelija: Boolean,
  ulkomaanjaksot: Option[Int],
  erityisen_koulutustehtävän_tehtävät: Option[String],
  erityisen_koulutustehtävän_jaksot: Option[Int],
  sisäoppilaitosmainenMajoitus: Option[Int],
  syy_alle18vuotiaana_aloitettuun_opiskeluun_aikuisten_lukiokoulutuksessa: Option[String],
  yhteislaajuus: Double
)

case class LukioOppiaineenTiedot(arvosana: Option[String], laajuus: Int) {
  override def toString: String = s"${arvosana.fold("Ei arvosanaa")("Arvosana " + _)}, $laajuus ${if (laajuus == 1) "kurssi" else "kurssia"}"
}

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

case class LukioKurssinTiedot(kurssintyyppi: Option[String], arvosana: Option[String], laajuus: Option[Double], tunnustettu: Boolean) {
  override def toString: String = s"${kurssintyyppi.getOrElse("Ei tyyppiä")},${arvosana.map("Arvosana " + _).getOrElse("Ei arvosanaa")},${laajuus.map("Laajuus " + _).getOrElse("Ei laajuutta")}${if (tunnustettu) ",tunnustettu}" else ""}"
}

case class LukioRaportinOppiaineenKurssitRow(stattisetKolumnit: LukioOppiaineenKurssienVälilehtiStaattisetKolumnit, kurssit: Seq[String]) {
  def toSeq: Seq[Any] = stattisetKolumnit.productIterator.toList ++ kurssit
}

sealed trait LukioRaporttiOppiaineTaiKurssi {
  def nimi: String
  def koulutusmoduuliKoodiarvo: String
  def koulutusmoduuliPaikallinen: Boolean
}

case class LukioRaporttiOppiaine(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends LukioRaporttiOppiaineTaiKurssi {
  def toSheetTitle: String = s"$koulutusmoduuliKoodiarvo ${if (koulutusmoduuliPaikallinen) "p" else "v"} ${nimi.capitalize}"
  def toColumnTitle: String = s"$koulutusmoduuliKoodiarvo ${nimi.capitalize} ${if (koulutusmoduuliPaikallinen) "paikallinen" else "valtakunnallinen"}"
}

case class LukioRaporttiKurssi(nimi: String, koulutusmoduuliKoodiarvo: String, koulutusmoduuliPaikallinen: Boolean) extends LukioRaporttiOppiaineTaiKurssi {
  def toColumnTitle: String = s"$koulutusmoduuliKoodiarvo ${nimi.capitalize} ${if (koulutusmoduuliPaikallinen) "paikallinen" else "valtakunnallinen"}"
}

case class LukioRaporttiOppiaineJaKurssit(oppiaine: LukioRaporttiOppiaine, kurssit: Seq[LukioRaporttiKurssi])
