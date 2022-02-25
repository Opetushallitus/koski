package fi.oph.koski.raportit.aikuistenperusopetus

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.RaporttiUtils.arvioituAikavälillä
import fi.oph.koski.raportit.YleissivistäväUtils._
import fi.oph.koski.raportit._
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import java.time.LocalDate
import java.time.temporal.ChronoField
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

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
  osasuoritustenAikarajaus: Boolean,
  t: LocalizationReader
) extends GlobalExecutionContext {

  def build(): Seq[DynamicDataSheet] = {
    val rows = repository.suoritustiedot(oppilaitosOid, alku, loppu, osasuoritusMukanaAikarajauksessa, raporttiType.päätasonSuoritusTyyppi)
    val oppiaineetJaKurssit = opetettavatOppiaineetJaNiidenKurssit(raporttiType.isOppiaineenOppimäärä, raporttiType.isOppiaine, rows, t)

    val oppiaineJaLisätiedotFuture = oppiaineJaLisätiedotSheet(rows, oppiaineetJaKurssit, alku, loppu)
    val kurssitFuture = oppiaineKohtaisetSheetit(rows, oppiaineetJaKurssit)

    val future = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotFuture
      kurssit <- kurssitFuture
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(future, atMost = 6.minutes)
  }

  private def osasuoritusMukanaAikarajauksessa(row: ROsasuoritusRow): Boolean = {
    (!osasuoritustenAikarajaus || !raporttiType.isKurssi(row) || arvioituAikavälillä(alku, loppu)(row)) && eiOleArvioituOsallistuneeksi(row)
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

  private def removeUnwantedColumns(rows: Seq[Seq[Any]], columnSettings: Seq[Column], t: LocalizationReader): (Seq[Seq[Any]], Seq[Column]) = {
    raporttiType match {
      case AikuistenPerusopetusAlkuvaiheRaportti =>
        dropDataColumnByTitle(rows, columnSettings, t.get("raportti-excel-kolumni-alkuvaiheenSuoritus"))
      case AikuistenPerusopetusPäättövaiheRaportti =>
        dropDataColumnByTitle(rows, columnSettings, t.get("raportti-excel-kolumni-päättövaiheenSuoritus"))
      case AikuistenPerusopetusOppiaineenOppimääräRaportti =>
        (rows, columnSettings)
    }
  }

  private def oppiaineJaLisätiedotSheet(
    opiskeluoikeusData: Seq[AikuistenPerusopetusRaporttiRows],
    oppiaineetJaKurssit: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate
  ) = {
    Future {
      val (rows, columnSettings) = removeUnwantedColumns(
        opiskeluoikeusData.map(kaikkiOppiaineetVälilehtiRow(_, oppiaineetJaKurssit, alku, loppu)),
        oppiaineJaLisätiedotColumnSettings(oppiaineetJaKurssit),
        t
      )

      DynamicDataSheet(
        title = t.get("raportti-excel-oppiaineet-sheet-name"),
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
      title = oppiaine.toSheetTitle(t),
      rows = filtered.map(oppiainekohtaisetKurssitiedot(_, kurssit)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(kurssit)
    )
  }

  private def notOppimääränOpiskelijaFromAnotherOppiaine(oppiaine: YleissivistäväRaporttiOppiaine)(data: AikuistenPerusopetusRaporttiRows) = {
    !raporttiType.isOppiaineenOppimäärä(data.päätasonSuoritus) || data.päätasonSuoritus.matchesWith(oppiaine, t.language)
  }

  private def kaikkiOppiaineetVälilehtiRow(row: AikuistenPerusopetusRaporttiRows, oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit], alku: LocalDate, loppu: LocalDate) = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val kurssit = row.osasuoritukset.filter(raporttiType.isKurssi)

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
        suorituksenNimi = row.päätasonSuoritus.koulutusModuulistaKäytettäväNimi(t.language),
        suorituksenTila = row.päätasonSuoritus.vahvistusPäivä.fold(t.get("raportti-excel-default-value-kesken"))(_ => t.get("raportti-excel-default-value-valmis")),
        suorituksenVahvistuspäivä = row.päätasonSuoritus.vahvistusPäivä.map(_.toLocalDate),
        läsnäolopäiviä_aikajakson_aikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
        rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(", "),
        rahoitusmuodotOk = rahoitusmuodotOk(row),
        ryhmä = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "ryhmä"),
        ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        majoitusetu = lisätiedot.flatMap(_.majoitusetu).map(lengthInDaysInDateRange(_, alku, loppu)),
        sisäoppilaitosmainenMajoitus = lisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
        yhteislaajuus = kurssit
          .map(_.laajuus).sum,
        yhteislaajuusSuoritetut = kurssit
          .filterNot(k => k.tunnustettu)
          .map(_.laajuus).sum,
        yhteislaajuusHylätyt = kurssit
          .filterNot(k => k.tunnustettu || k.arvioituJaHyväksytty)
          .map(_.laajuus).sum,
        yhteislaajuusTunnustetut = kurssit
          .filter(k => k.arvioituJaHyväksytty && k.tunnustettu)
          .map(_.laajuus).sum,
        yhteislaajuusKorotettuEriVuonna = kurssit
          .filter(_.korotettuEriVuonna)
          .map(_.laajuus).sum
      ),
      oppiaineet = oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet, raporttiType.isOppiaineenOppimäärä, t)
    )
    data.toSeq
  }

  private def eiOleArvioituOsallistuneeksi(osasuoritus: ROsasuoritusRow): Boolean = {
    osasuoritus.arviointiArvosanaKoodiarvo.getOrElse("") != "O"
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
      osasuorituksetMap.getOrElse(kurssi.koulutusmoduuliKoodiarvo, Nil).filter(_.matchesWith(kurssi, t.language)).map(kurssisuoritus =>
        AikuistenPerusopetusKurssinTiedot(
          arvosana = kurssisuoritus.arviointiArvosanaKoodiarvo,
          laajuus = kurssisuoritus.koulutusmoduuliLaajuusArvo,
          tunnustettu = kurssisuoritus.tunnustettu,
          korotettuEriVuonna = kurssisuoritus.korotettuEriVuonna
        ).toStringLokalisoitu(t)
      ).mkString(", ")
    }
  }

  private def alkuvaiheenSuorituksiaColumn() = {
    val comment = raporttiType match {
      case AikuistenPerusopetusOppiaineenOppimääräRaportti =>
        Some(t.get("raportti-excel-kolumni-alkuvaiheenSuoritus-comment"))
      case _ => None
    }
    CompactColumn(t.get("raportti-excel-kolumni-alkuvaiheenSuoritus"), comment = comment)
  }

  private def päättövaiheenSuorituksiaColumn() = {
    val comment = raporttiType match {
      case AikuistenPerusopetusOppiaineenOppimääräRaportti =>
        Some(t.get("raportti-excel-kolumni-päättövaiheenSuoritus-comment"))
      case _ => None
    }
    CompactColumn(t.get("raportti-excel-kolumni-päättövaiheenSuoritus"), comment = comment)
  }

  private def oppiaineJaLisätiedotColumnSettings(oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit]) = {
    val yleisetColumns = Seq(
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
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-comment"))),
      alkuvaiheenSuorituksiaColumn(),
      päättövaiheenSuorituksiaColumn(),
      CompactColumn(t.get("raportti-excel-kolumni-koulutusModuuli"), comment = Some(t.get("raportti-excel-kolumni-koulutusModuuli-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenNimi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenNimi-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTila-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva"), comment = Some(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-läsnäolopäiviäAikajaksolla"), comment = Some(t.get("raportti-excel-kolumni-läsnäolopäiviäAikajaksolla-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-rahoitukset"), comment = Some(t.get("raportti-excel-kolumni-rahoitukset-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-rahoitusmuodot"), comment = Some(t.get("raportti-excel-kolumni-rahoitusmuodot-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-ryhmä")),
      CompactColumn(t.get("raportti-excel-kolumni-ulkomaanjaksot"), comment = Some(t.get("raportti-excel-kolumni-ulkomaanjaksot-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-majoitusetu"), comment = Some(t.get("raportti-excel-kolumni-majoitusetu-count-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit-comment"))),
      CompactColumn(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit-comment")))
    )

    val oppiaineColumns = oppiaineet.map(x =>
      CompactColumn(title = x.oppiaine.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-comment")))
    )

    yleisetColumns ++ oppiaineColumns
  }

  private def oppiaineKohtaisetColumnSettings(kurssit: Seq[YleissivistäväRaporttiKurssi]) = {
    Seq(
      Column(t.get("raportti-excel-kolumni-oppijaOid")),
      Column(t.get("raportti-excel-kolumni-hetu")),
      Column(t.get("raportti-excel-kolumni-sukunimi")),
      Column(t.get("raportti-excel-kolumni-etunimet")),
      CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-comment")))
    ) ++ kurssit.map(k => CompactColumn(title = k.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-kurssit-comment"))))
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
  yhteislaajuusTunnustetut: BigDecimal,
  yhteislaajuusKorotettuEriVuonna: BigDecimal
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

case class AikuistenPerusopetusKurssinTiedot(arvosana: Option[String], laajuus: Option[Double], tunnustettu: Boolean, korotettuEriVuonna: Boolean) {

  def toStringLokalisoitu(t: LocalizationReader): String = {
    List(
      Some(arvosana.map(s"${t.get("raportti-excel-default-value-arvosana")} " + _).getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))),
      Some(laajuus.map(s"${t.get("raportti-excel-default-value-laajuus").capitalize} " + _).getOrElse(t.get("raportti-excel-default-value-laajuus-puuttuu"))),
      if (tunnustettu) Some(t.get("raportti-excel-default-value-tunnustettu")) else None,
      if (korotettuEriVuonna) Some(t.get("raportti-excel-default-value-korotettuEriVuonna")) else None
    ).flatten.mkString(", ")
  }
}
