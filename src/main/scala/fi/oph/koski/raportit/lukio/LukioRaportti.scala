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

    val oppiaineJaLisätiedotFuture = oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
    val kurssitFuture = oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit)

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
    loppu: LocalDate
  ): Future[DynamicDataSheet] = {
    Future {
      DynamicDataSheet(
        title = t.get("raportti-excel-oppiaineet-sheet-name"),
        rows = opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu)).map(_.toSeq),
        columnSettings = oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit)
      )
    }
  }

  private def oppiaineKohtaisetSheetit(
    rows: Seq[LukioRaporttiRows],
    oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit]
  ): Future[Seq[DynamicDataSheet]] = {
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

  private def kaikkiOppiaineetVälilehtiRow(
    row: LukioRaporttiRows,
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate
  ): LukioRaporttiKaikkiOppiaineetVälilehtiRow = {
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
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit-comment")))
    ) ++ oppiaineet.map(x => CompactColumn(title = x.oppiaine.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-comment"))))
  }

  private def oppiaineKohtaisetColumnSettings(kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    Seq(
      Column(t.get("raportti-excel-kolumni-oppijaOid")),
      Column(t.get("raportti-excel-kolumni-hetu")),
      Column(t.get("raportti-excel-kolumni-sukunimi")),
      Column(t.get("raportti-excel-kolumni-etunimet")),
      CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-opetussuunnitelma"), comment = Some(t.get("raportti-excel-kolumni-opetussuunnitelma-lukio-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-lukio-comment")))
    ) ++ kurssit.map(k => CompactColumn(title = k.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-lukio-comment"))))
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
