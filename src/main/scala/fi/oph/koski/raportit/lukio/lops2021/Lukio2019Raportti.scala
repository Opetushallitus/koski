package fi.oph.koski.raportit.lukio.lops2021

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

case class Lukio2019Raportti(repository: Lukio2019RaportitRepository, t: LocalizationReader) extends GlobalExecutionContext {

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
    val oppiaineetJaOsasuoritukset = opetettavatOppiaineetJaNiidenKurssit(isOppiaineenOppimäärä, isOppiaine, rows, t)

    val oppiaineJaLisätiedotFuture = oppiaineJaLisätiedotSheet(rows, oppiaineetJaOsasuoritukset, alku, loppu)
    val osasuorituksetFuture = oppiaineKohtaisetSheetit(rows, oppiaineetJaOsasuoritukset)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotFuture
      osasuoritukset <- osasuorituksetFuture
    } yield (oppiaineJaLisätiedot +: osasuoritukset)

    Futures.await(future, atMost = 6.minutes)
  }

  private def oppiaineJaLisätiedotSheet(
    opiskeluoikeusData: Seq[Lukio2019RaporttiRows],
    oppiaineetJaOsasuoritukset: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate
  ): Future[DynamicDataSheet] = {
    Future {
      DynamicDataSheet(
        title = t.get("raportti-excel-oppiaineet-sheet-name"),
        rows = opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaOsasuoritukset, alku, loppu)).map(_.toSeq),
        columnSettings = oppiaineJaLisätiedotColumnSettings(oppiaineetJaOsasuoritukset)
      )
    }
  }

  private def oppiaineKohtaisetSheetit(
    rows: Seq[Lukio2019RaporttiRows],
    oppiaineetJaOsasuoritukset: Seq[YleissivistäväRaporttiOppiaineJaKurssit]
  ): Future[Seq[DynamicDataSheet]] = {
    Future {
      oppiaineetJaOsasuoritukset.map(oppiaineKohtainenSheet(_, rows))
    }
  }

  private def oppiaineKohtainenSheet(
    oppiaineJaOsasuoritukset: YleissivistäväRaporttiOppiaineJaKurssit,
    data: Seq[Lukio2019RaporttiRows]
  ): DynamicDataSheet = {
    val oppiaine = oppiaineJaOsasuoritukset.oppiaine
    val osasuoritukset = oppiaineJaOsasuoritukset.oppijoidenRivitJärjestettyKursseittain
    val filtered = data.filter(notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine))

    DynamicDataSheet(
      title = oppiaine.toSheetTitle(t),
      rows = filtered.map(oppiainekohtaisetOsasuorituksetiedot(_, osasuoritukset)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(osasuoritukset)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: YleissivistäväRaporttiOppiaine)(data: Lukio2019RaporttiRows) = {
    !isOppiaineenOppimäärä(data.päätasonSuoritus) || data.päätasonSuoritus.matchesWith(oppiaine, t.language)
  }

  private def kaikkiOppiaineetVälilehtiRow(
    row: Lukio2019RaporttiRows,
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate
  ): Lukio2019RaporttiKaikkiOppiaineetVälilehtiRow = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val lukionOsasuoritukset = row.osasuoritukset.filter(s => List("lukionvaltakunnallinenmoduuli", "lukionpaikallinenopintojakso").contains(s.suorituksenTyyppi))
    val oppimääräSuoritettu = JsonSerializer.extract[Option[Boolean]](row.opiskeluoikeus.data \ "oppimääräSuoritettu")

    Lukio2019RaporttiKaikkiOppiaineetVälilehtiRow(
     muut = Lukio2019RaporttiOppiaineetVälilehtiMuut(
       opiskeluoikeudenOid = row.opiskeluoikeus.opiskeluoikeusOid,
       lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
       koulutustoimija = if(t.language == "sv") row.opiskeluoikeus.koulutustoimijaNimiSv else row.opiskeluoikeus.koulutustoimijaNimi,
       oppilaitoksenNimi = if(t.language == "sv") row.opiskeluoikeus.oppilaitosNimiSv else row.opiskeluoikeus.oppilaitosNimi,
       toimipiste = if(t.language == "sv") row.päätasonSuoritus.toimipisteNimiSv else row.päätasonSuoritus.toimipisteNimi,
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
       suorituksenTila = row.päätasonSuoritus.vahvistusPäivä.fold(t.get("raportti-excel-default-value-kesken"))(_ => t.get("raportti-excel-default-value-valmis")),
       suorituksenVahvistuspäivä = row.päätasonSuoritus.vahvistusPäivä.map(_.toLocalDate),
       oppimääräSuoritettu = oppimääräSuoritettu.getOrElse(false),
       läsnäolopäiviä_aikajakson_aikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
       rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(","),
       rahoitusmuodotOk = rahoitusmuodotOk(row),
       ryhmä = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "ryhmä"),
       pidennettyPäättymispäivä = lisätiedot.exists(_.pidennettyPäättymispäivä),
       ulkomainenVaihtoOpiskelija = lisätiedot.exists(_.ulkomainenVaihtoopiskelija),
       ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       erityisen_koulutustehtävän_tehtävät = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.flatMap(_.tehtävä.nimi.map(_.get(t.language))).mkString(","))),
       erityisen_koulutustehtävän_jaksot = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
       maksuttomuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
       maksullisuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => !m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
       oikeuttaMaksuttomuuteenPidennetty = lisätiedot.flatMap(_.oikeuttaMaksuttomuuteenPidennetty.map(omps => omps.map(_.toString).mkString(", "))).filter(_.nonEmpty),
       yhteislaajuus = lukionOsasuoritukset.map(_.laajuus).sum,
       yhteislaajuusSuoritetut = lukionOsasuoritukset
         .filterNot(k => k.tunnustettu)
         .map(_.laajuus).sum,
       yhteislaajuusHylätyt = lukionOsasuoritukset
         .filterNot(k => k.tunnustettu || k.arvioituJaHyväksytty)
         .map(_.laajuus).sum,
       yhteislaajuusTunnustetut = lukionOsasuoritukset
         .filter(k => k.arvioituJaHyväksytty && k.tunnustettu)
         .map(_.laajuus).sum,
       yhteislaajuusKorotettuEriVuonna = lukionOsasuoritukset
         .filter(_.korotettuEriVuonna)
         .map(_.laajuus).sum
     ),
      oppiaineet = oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet, isOppiaineenOppimäärä, t)
    )
  }

  private def oppiainekohtaisetOsasuorituksetiedot(
    oppijanRivit: Lukio2019RaporttiRows,
    oppijoidenRivitJärjestettyKursseittain: Seq[Seq[YleissivistäväRaporttiKurssi]]
  ): Lukio2019RaportinOppiaineenOsasuorituksetRow = {
    Lukio2019RaportinOppiaineenOsasuorituksetRow(
      stattisetKolumnit = Lukio2019OppiaineenKurssienVälilehtiStaattisetKolumnit(
        oppijanOid = oppijanRivit.opiskeluoikeus.oppijaOid,
        hetu = oppijanRivit.henkilo.hetu,
        sukinimi = oppijanRivit.henkilo.sukunimi,
        etunimet = oppijanRivit.henkilo.etunimet,
        toimipiste = if (t.language == "sv") oppijanRivit.päätasonSuoritus.toimipisteNimiSv else oppijanRivit.päätasonSuoritus.toimipisteNimi,
        opetussuunnitelma = opetussuunnitelma(oppijanRivit.päätasonSuoritus),
        suorituksenTyyppi = oppijanRivit.päätasonSuoritus.suorituksenTyyppi
      ),
      osasuoritukset = kurssienTiedot(oppijanRivit, oppijoidenRivitJärjestettyKursseittain)
    )
  }

  private def kurssienTiedot(
    oppijanRivit: Lukio2019RaporttiRows,
    oppijoidenRivitJärjestettyKursseittain: Seq[Seq[YleissivistäväRaporttiKurssi]]
  ): Seq[String] = {
    def onKurssiOppijalla(k: YleissivistäväRaporttiKurssi): Boolean =
      oppijanRivit.osasuoritukset.map(_.osasuoritusId).contains(k.osasuoritusRow.osasuoritusId)

    oppijoidenRivitJärjestettyKursseittain.map {
      // Tuottaa tyhjän solun jos oppijalla ei ole suorituksia tähän sarakkeeseen
      case kurssit: Seq[YleissivistäväRaporttiKurssi] if !kurssit.exists(k => onKurssiOppijalla(k)) => ""
      case kurssit: Seq[YleissivistäväRaporttiKurssi] =>
        val oppijanKurssit = kurssit.filter(k => onKurssiOppijalla(k))
        oppijanKurssit.map( kurssi =>
          Lukio2019ModuulinTiedot(
            pakollinen = JsonSerializer.extract[Boolean](kurssi.osasuoritusRow.data \ "koulutusmoduuli" \ "pakollinen"),
            arvosana = kurssi.osasuoritusRow.arviointiArvosanaKoodiarvo,
            laajuus = kurssi.osasuoritusRow.koulutusmoduuliLaajuusArvo,
            tunnustettu = kurssi.osasuoritusRow.tunnustettu,
            korotettuEriVuonna = kurssi.osasuoritusRow.korotettuEriVuonna
          ).toStringLokalisoitu(t)
        ).mkString(",")
    }
  }

  private def opetussuunnitelma(suoritus: RPäätasonSuoritusRow) = {
    if (isOppiaineenOppimäärä(suoritus)) {
      JsonSerializer.extract[Option[String]](suoritus.data \ "koulutusmoduuli" \ "perusteenDiaarinumero").map {
        case "60/011/2015" | "33/011/2003" => t.get("raportti-excel-default-value-lukio-nuorten-ops")
        case "70/011/2015" | "4/011/2004" => t.get("raportti-excel-default-value-lukio-aikuisten-ops")
        case diaarinumero => diaarinumero
      }
    } else {
      JsonSerializer.extract[Option[Koodistokoodiviite]](suoritus.data \ "oppimäärä").flatMap(_.nimi.map(_.get(t.language)))
    }
  }

  private def oppiaineJaLisätiedotColumnSettings(oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit]) = {
    Seq(
      CompactColumn(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
      CompactColumn(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
      CompactColumn(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
      CompactColumn(t.get("raportti-excel-kolumni-päivitetty"), comment = Some(t.get("raportti-excel-kolumni-päivitetty-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
      Column(t.get("raportti-excel-kolumni-oppijaOid")),
      Column(t.get("raportti-excel-kolumni-hetu")),
      Column(t.get("raportti-excel-kolumni-sukunimi")),
      Column(t.get("raportti-excel-kolumni-etunimet")),
      CompactColumn(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
      CompactColumn(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-kaikkiTilat"), comment = Some(t.get("raportti-excel-kolumni-kaikkiTilat-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-opetussuunnitelma"), comment = Some(t.get("raportti-excel-kolumni-opetussuunnitelma-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTila-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva"), comment = Some(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-oppimääräSuoritettu"), comment = Some(t.get("raportti-excel-kolumni-oppimääräSuoritettu-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-läsnäolopäiviäAikajaksolla"), comment = Some(t.get("raportti-excel-kolumni-läsnäolopäiviäAikajaksolla-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-rahoitukset"), comment = Some(t.get("raportti-excel-kolumni-rahoitukset-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-rahoitusmuodot"), comment = Some(t.get("raportti-excel-kolumni-rahoitusmuodot-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-ryhmä")),
      CompactColumn(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä"), comment = Some(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-ulkomainenVaihtoOpiskelija"), comment = Some(t.get("raportti-excel-kolumni-ulkomainenVaihtoOpiskelija-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-ulkomaanjaksot"), comment = Some(t.get("raportti-excel-kolumni-ulkomaanjaksot-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-erityinenKoulutustehtävä"), comment = Some(t.get("raportti-excel-kolumni-erityinenKoulutustehtävä-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-erityinenKoulutustehtäväJaksot"), comment = Some(t.get("raportti-excel-kolumni-erityinenKoulutustehtäväJaksot-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-maksuttomuus"), comment = Some(t.get("raportti-excel-kolumni-maksuttomuus-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-maksullisuus"), comment = Some(t.get("raportti-excel-kolumni-maksullisuus-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty"), comment = Some(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiOsasuoritukset"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiOsasuoritukset-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutOsasuoritukset"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutOsasuoritukset-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusHylätytOsasuoritukset"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusHylätytOsasuoritukset-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutOsasuoritukset"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutOsasuoritukset-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutOsasuoritukset"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutOsasuoritukset-comment")))
    ) ++ oppiaineet.map(x => CompactColumn(title = x.oppiaine.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-comment"))))
  }

  private def oppiaineKohtaisetColumnSettings(
    oppijoidenRivitJärjestettyKursseittain: Seq[Seq[YleissivistäväRaporttiKurssi]]
  ): Seq[Column] = {
    Seq(
      Column(t.get("raportti-excel-kolumni-oppijaOid")),
      Column(t.get("raportti-excel-kolumni-hetu")),
      Column(t.get("raportti-excel-kolumni-sukunimi")),
      Column(t.get("raportti-excel-kolumni-etunimet")),
      CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-opetussuunnitelma"), comment = Some(t.get("raportti-excel-kolumni-opetussuunnitelma-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-lukio-comment")))
    ) ++ oppijoidenRivitJärjestettyKursseittain.map(_.head).map(k => CompactColumn(title = k.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-lukio-comment"))))
  }
}

case class Lukio2019RaporttiOppiaineetVälilehtiMuut(
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
  maksuttomuus: Option[String],
  maksullisuus: Option[String],
  oikeuttaMaksuttomuuteenPidennetty: Option[String],
  yhteislaajuus: BigDecimal,
  yhteislaajuusSuoritetut: BigDecimal,
  yhteislaajuusHylätyt: BigDecimal,
  yhteislaajuusTunnustetut: BigDecimal,
  yhteislaajuusKorotettuEriVuonna: BigDecimal
)

case class Lukio2019RaporttiKaikkiOppiaineetVälilehtiRow(muut: Lukio2019RaporttiOppiaineetVälilehtiMuut, oppiaineet: Seq[String]) {
 def toSeq: Seq[Any] = muut.productIterator.toList ++ oppiaineet
}

case class Lukio2019OppiaineenKurssienVälilehtiStaattisetKolumnit(
  oppijanOid: String,
  hetu: Option[String],
  sukinimi: String,
  etunimet: String,
  toimipiste: String,
  opetussuunnitelma: Option[String],
  suorituksenTyyppi: String
)

case class Lukio2019ModuulinTiedot(pakollinen: Boolean, arvosana: Option[String], laajuus: Option[Double], tunnustettu: Boolean, korotettuEriVuonna: Boolean) {
  def toStringLokalisoitu(t: LocalizationReader): String = {
    List(
      Some(if (pakollinen) t.get("raportti-excel-default-value-pakollinen").capitalize else t.get("raportti-excel-default-value-vapaavalintainen").capitalize),
      Some(arvosana.map(s"${t.get("raportti-excel-default-value-arvosana")} " + _).getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))),
      Some(laajuus.map(s"${t.get("raportti-excel-default-value-laajuus").capitalize} " + _).getOrElse(t.get("raportti-excel-default-value-laajuus-puuttuu"))),
      if (tunnustettu) Some(t.get("raportti-excel-default-value-tunnustettu")) else None,
      if (korotettuEriVuonna) Some(t.get("raportti-excel-default-value-korotettuEriVuonna")) else None
    ).flatten.mkString(", ")
  }
}

case class Lukio2019RaportinOppiaineenOsasuorituksetRow(stattisetKolumnit: Lukio2019OppiaineenKurssienVälilehtiStaattisetKolumnit, osasuoritukset: Seq[String]) {
  def toSeq: Seq[Any] = stattisetKolumnit.productIterator.toList ++ osasuoritukset
}
