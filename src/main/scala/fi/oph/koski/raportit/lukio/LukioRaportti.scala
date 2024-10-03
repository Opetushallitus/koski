package fi.oph.koski.raportit.lukio

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.YleissivistäväUtils._
import fi.oph.koski.raportit._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.util.Futures

import java.sql.Date
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
    osasuoritustenAikarajaus: Boolean,
    kotikuntaPvm: Option[LocalDate],
  ): Seq[DynamicDataSheet] = {
    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu, osasuoritustenAikarajaus, kotikuntaPvm)
    val oppiaineetJaKurssit = opetettavatOppiaineetJaNiidenKurssit(isOppiaineenOppimäärä, isOppiaine, rows, t)

    val oppiaineJaLisätiedotFuture = oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu, kotikuntaPvm)
    val kurssitFuture = oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit, kotikuntaPvm)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotFuture
      kurssit <- kurssitFuture
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def oppiaineJaLisätiedotSheet(
    opiskeluoikeusData: Seq[LukioRaporttiRows],
    oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate,
    kotikuntaPvm: Option[LocalDate],
  ): Future[DynamicDataSheet] = {
    Future {
      DynamicDataSheet(
        title = t.get("raportti-excel-oppiaineet-sheet-name"),
        rows = opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu, kotikuntaPvm)).map(_.toSeq),
        columnSettings = oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit)
      )
    }
  }

  private def oppiaineKohtaisetSheetit(
    rows: Seq[LukioRaporttiRows],
    oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    kotikuntaPvm: Option[LocalDate],
  ): Future[Seq[DynamicDataSheet]] = {
    Future {
      oppiaineetJaKurssit.map(oppiaineKohtainenSheet(_, rows, kotikuntaPvm))
    }
  }

  private def oppiaineKohtainenSheet(oppiaineJaKurssit: YleissivistäväRaporttiOppiaineJaKurssit, data: Seq[LukioRaporttiRows], kotikuntaPvm: Option[LocalDate]) = {
    val oppiaine = oppiaineJaKurssit.oppiaine
    val kurssit = oppiaineJaKurssit.oppijoidenRivitJärjestettyKursseittain
    val filtered = data.filter(notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine))

    DynamicDataSheet(
      title = oppiaine.toSheetTitle(t),
      rows = filtered.map(oppiainekohtaisetKurssitiedot(_, kurssit, kotikuntaPvm)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(kurssit)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: YleissivistäväRaporttiOppiaine)(data: LukioRaporttiRows) = {
    !isOppiaineenOppimäärä(data.päätasonSuoritus) || data.päätasonSuoritus.matchesWith(oppiaine, t.language)
  }

  private def kaikkiOppiaineetVälilehtiRow(
    row: LukioRaporttiRows,
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate,
    kotikuntaPvm: Option[LocalDate],
  ): LukioRaporttiKaikkiOppiaineetVälilehtiRow = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val lukionKurssit = row.osasuoritukset.filter(_.suorituksenTyyppi == "lukionkurssi")
    val oppimääräSuoritettu = JsonSerializer.extract[Option[Boolean]](row.opiskeluoikeus.data \ "oppimääräSuoritettu")

    LukioRaporttiKaikkiOppiaineetVälilehtiRow(
     muut = LukioRaporttiOppiaineetVälilehtiMuut(
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
       kotikunta = getKotikunta(row, kotikuntaPvm),
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

  private def getKotikunta(row: LukioRaporttiRows, kotikuntaPvm: Option[LocalDate]): Option[String] = {
    val kotikunta = if (t.language == "sv") row.henkilo.kotikuntaNimiSv else row.henkilo.kotikuntaNimiFi
    kotikuntaPvm match {
      case Some(pvm) => row.kotikuntaHistoriassa
        .map(r => Some(if (t.language == "sv") r.kotikunnanNimiSv else r.kotikunnanNimiFi))
        .getOrElse(formatKotikuntaEiTiedossa(kotikunta, pvm, t))
      case None => kotikunta
    }
  }

  private def oppiainekohtaisetKurssitiedot(
    row: LukioRaporttiRows,
    oppijoidenRivitJärjestettyKursseittain: Seq[Seq[YleissivistäväRaporttiKurssi]],
    kotikuntaPvm: Option[LocalDate],
  ): LukioRaportinOppiaineenKurssitRow = {
    val kotikunta = if (t.language == "sv") row.henkilo.kotikuntaNimiSv else row.henkilo.kotikuntaNimiFi

    LukioRaportinOppiaineenKurssitRow(
      stattisetKolumnit = LukioOppiaineenKurssienVälilehtiStaattisetKolumnit(
        oppijanOid = row.opiskeluoikeus.oppijaOid,
        hetu = row.henkilo.hetu,
        sukinimi = row.henkilo.sukunimi,
        etunimet = row.henkilo.etunimet,
        kotikunta = getKotikunta(row, kotikuntaPvm),
        toimipiste = if (t.language == "sv") row.päätasonSuoritus.toimipisteNimiSv else row.päätasonSuoritus.toimipisteNimi,
        opetussuunnitelma = opetussuunnitelma(row.päätasonSuoritus),
        suorituksenTyyppi = row.päätasonSuoritus.suorituksenTyyppi
      ),
      kurssit = kurssienTiedot(row, oppijoidenRivitJärjestettyKursseittain)
    )
  }


  private def kurssienTiedot(
    oppijanRivit: LukioRaporttiRows,
    oppijoidenRivitJärjestettyKursseittain: Seq[Seq[YleissivistäväRaporttiKurssi]]
  ): Seq[String] = {
    def onKurssiOppijalla(k: YleissivistäväRaporttiKurssi): Boolean =
      oppijanRivit.osasuoritukset.map(_.osasuoritusId).contains(k.osasuoritusRow.osasuoritusId)

    oppijoidenRivitJärjestettyKursseittain.map {
      // Tuottaa tyhjän solun jos oppijalla ei ole suorituksia tähän sarakkeeseen
      case kurssit: Seq[YleissivistäväRaporttiKurssi] if !kurssit.exists(k => onKurssiOppijalla(k)) => ""
      case kurssit: Seq[YleissivistäväRaporttiKurssi] =>
        val oppijanKurssit = kurssit.filter(k => onKurssiOppijalla(k))
        oppijanKurssit.map(kurssi =>
          LukioKurssinTiedot(
            kurssintyyppi = JsonSerializer.extract[Option[Koodistokoodiviite]](kurssi.osasuoritusRow.data \ "koulutusmoduuli" \ "kurssinTyyppi").map(_.koodiarvo),
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

  private def formatKotikuntaEiTiedossa(kotikunta: Option[String], pvm: LocalDate, t: LocalizationReader): Option[String] =
    kotikunta.map { kk =>
      s"${t.get("Ei tiedossa")} ${pvm.format(finnishDateFormat)} (${t.get("nykyinen kotikunta on")} $kk)"
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
      Column(t.get("raportti-excel-kolumni-kotikunta")),
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
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit-comment")))
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
      Column(t.get("raportti-excel-kolumni-kotikunta")),
      CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-opetussuunnitelma"), comment = Some(t.get("raportti-excel-kolumni-opetussuunnitelma-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-lukio-comment")))
    ) ++ oppijoidenRivitJärjestettyKursseittain.map(_.head).map(k => CompactColumn(title = k.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-lukio-comment"))))
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
  kotikunta: Option[String],
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

case class LukioRaporttiKaikkiOppiaineetVälilehtiRow(muut: LukioRaporttiOppiaineetVälilehtiMuut, oppiaineet: Seq[String]) {
 def toSeq: Seq[Any] = muut.productIterator.toList ++ oppiaineet
}

case class LukioOppiaineenKurssienVälilehtiStaattisetKolumnit(
  oppijanOid: String,
  hetu: Option[String],
  sukinimi: String,
  etunimet: String,
  kotikunta: Option[String],
  toimipiste: String,
  opetussuunnitelma: Option[String],
  suorituksenTyyppi: String
)

case class LukioKurssinTiedot(kurssintyyppi: Option[String], arvosana: Option[String], laajuus: Option[Double], tunnustettu: Boolean, korotettuEriVuonna: Boolean) {
  def toStringLokalisoitu(t: LocalizationReader): String = {
    List(
      Some(kurssintyyppi.getOrElse(t.get("raportti-excel-default-value-ei-tyyppiä")).capitalize),
      Some(arvosana.map(s"${t.get("raportti-excel-default-value-arvosana")} " + _).getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))),
      Some(laajuus.map(s"${t.get("raportti-excel-default-value-laajuus").capitalize} " + _).getOrElse(t.get("raportti-excel-default-value-laajuus-puuttuu"))),
      if (tunnustettu) Some(t.get("raportti-excel-default-value-tunnustettu")) else None,
      if (korotettuEriVuonna) Some(t.get("raportti-excel-default-value-korotettuEriVuonna")) else None
    ).flatten.mkString(", ")
  }
}

case class LukioRaportinOppiaineenKurssitRow(stattisetKolumnit: LukioOppiaineenKurssienVälilehtiStaattisetKolumnit, kurssit: Seq[String]) {
  def toSeq: Seq[Any] = stattisetKolumnit.productIterator.toList ++ kurssit
}
