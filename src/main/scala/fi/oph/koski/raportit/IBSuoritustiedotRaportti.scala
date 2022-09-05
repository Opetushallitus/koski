package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.YleissivistäväUtils.{lengthInDaysInDateRange, opetettavatOppiaineetJaNiidenKurssit, oppiaineidentiedot, rahoitusmuodotOk, removeContinuousSameTila}
import fi.oph.koski.raportointikanta.ROsasuoritusRow
import fi.oph.koski.schema._
import fi.oph.koski.util.Futures

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class IBSuoritustiedotRaportti(repository: IBSuoritustiedotRaporttiRepository, t: LocalizationReader) extends GlobalExecutionContext {

  def build(
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    raportinTyyppi: IBSuoritustiedotRaporttiType
  )(implicit u: KoskiSpecificSession): Seq[DynamicDataSheet] = {
    val rows = repository
      .suoritustiedot(oppilaitosOid, alku, loppu, osasuoritustenAikarajaus, raportinTyyppi.päätasonSuoritusTyyppi)
    val oppiaineetJaKurssit = opetettavatOppiaineetJaNiidenKurssit(_ => false, raportinTyyppi.isOppiaine, rows, t)

    val oppiaineJaLisätiedotFuture = Future{
      DynamicDataSheet(
        title = t.get("raportti-excel-oppiaineet-sheet-name"),
        rows = rows.map(r => kaikkiOppiaineetVälilehtiRow(r, oppiaineetJaKurssit, alku, loppu, raportinTyyppi)),
        columnSettings = columnSettings(oppiaineetJaKurssit, raportinTyyppi, t)
      )
    }
    val kurssitFuture = Future(
      oppiaineetJaKurssit.map(oJaK => oppiaineKohtainenSheet(oJaK, rows, raportinTyyppi))
    )

    val dataSheets = for {
      oppiaineJaLisätiedot <- oppiaineJaLisätiedotFuture
      kurssit <- kurssitFuture
    } yield (oppiaineJaLisätiedot +: kurssit)

    Futures.await(dataSheets, atMost = 6.minutes)
  }

  private def kaikkiOppiaineetVälilehtiRow(
    row: IBRaporttiRows,
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    alku: LocalDate,
    loppu: LocalDate,
    raportinTyyppi: IBSuoritustiedotRaporttiType
  ): List[Any] = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val lisätiedot = JsonSerializer.extract[Option[LukionOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val kurssit = row.osasuoritukset.filter(raportinTyyppi.isKurssi)

    IBRaporttiRow(
      opiskeluoikeusOid = row.opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      koulutustoimijaNimi = if (t.language == "sv") row.opiskeluoikeus.koulutustoimijaNimiSv else row.opiskeluoikeus.koulutustoimijaNimi,
      oppilaitoksenNimi = if (t.language == "sv") row.opiskeluoikeus.oppilaitosNimiSv else row.opiskeluoikeus.oppilaitosNimi,
      toimipisteNimi = if (t.language == "sv") row.päätasonSuoritus.toimipisteNimiSv else row.päätasonSuoritus.toimipisteNimi,
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      aikaleima = row.opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      yksiloity = row.henkilo.yksiloity,
      oppijaOid = row.opiskeluoikeus.oppijaOid,
      hetu = row.henkilo.hetu,
      sukunimi = row.henkilo.sukunimi,
      etunimet = row.henkilo.etunimet,
      opiskeluoikeudenAlkamispäivä = row.opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      opiskeluoikeudenViimeisinTila = row.opiskeluoikeus.viimeisinTila,
      opiskeluoikeudenTilatAikajaksonAikana = removeContinuousSameTila(row.aikajaksot).map(_.tila).mkString(", "),
      päätasonSuoritukset = row.päätasonSuoritus.koulutusModuulistaKäytettäväNimi(t.language),
      opiskeluoikeudenPäättymispäivä = row.opiskeluoikeus.päättymispäivä.map(_.toLocalDate),
      rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(", "),
      rahoitusmuodotOk = rahoitusmuodotOk(row),
      maksuttomuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
      oikeuttaMaksuttomuuteenPidennetty = lisätiedot.flatMap(_.oikeuttaMaksuttomuuteenPidennetty.map(omps => omps.map(_.toString).mkString(", "))).filter(_.nonEmpty),
      pidennettyPäättymispäivä = lisätiedot.exists(_.pidennettyPäättymispäivä),
      ulkomainenVaihtoOpiskelija = lisätiedot.exists(_.ulkomainenVaihtoopiskelija),
      erityinenKoulutustehtäväJaksot = lisätiedot.flatMap(_.erityisenKoulutustehtävänJaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
      ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)),
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
    ).productIterator.toList ++ oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet, _ => false, t)
  }

  def columnSettings(
    oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit],
    raportinTyyppi: IBSuoritustiedotRaporttiType,
    t: LocalizationReader
  ): Seq[Column] = Seq(
    Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    Column(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
    Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    Column(t.get("raportti-excel-kolumni-toimipisteNimi")),
    Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    Column(t.get("raportti-excel-kolumni-päivitetty"), comment = Some(t.get("raportti-excel-kolumni-päivitetty-comment"))),
    Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    Column(t.get("raportti-excel-kolumni-oppijaOid")),
    Column(t.get("raportti-excel-kolumni-hetu")),
    Column(t.get("raportti-excel-kolumni-sukunimi")),
    Column(t.get("raportti-excel-kolumni-etunimet")),
    Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    Column(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    Column(t.get("raportti-excel-kolumni-kaikkiTilat"), comment = Some(t.get("raportti-excel-kolumni-kaikkiTilat-comment"))),
    Column(t.get("raportti-excel-kolumni-koulutusmoduuliNimet")),
    Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
    Column(t.get("raportti-excel-kolumni-rahoitukset"), comment = Some(t.get("raportti-excel-kolumni-rahoitukset-comment"))),
    Column(t.get("raportti-excel-kolumni-rahoitusmuodot"), comment = Some(t.get("raportti-excel-kolumni-rahoitusmuodot-comment"))),
    Column(t.get("raportti-excel-kolumni-maksuttomuus"), comment = Some(t.get("raportti-excel-kolumni-maksuttomuus-comment"))),
    Column(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty"), comment = Some(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty-comment"))),
    Column(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä"), comment = Some(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä-lukio-comment"))),
    Column(t.get("raportti-excel-kolumni-ulkomainenVaihtoOpiskelija"), comment = Some(t.get("raportti-excel-kolumni-ulkomainenVaihtoOpiskelija-lukio-comment"))),
    Column(t.get("raportti-excel-kolumni-erityinenKoulutustehtäväJaksot"), comment = Some(t.get("raportti-excel-kolumni-erityinenKoulutustehtäväJaksot-comment"))),
    Column(t.get("raportti-excel-kolumni-ulkomaanjaksot"), comment = Some(t.get("raportti-excel-kolumni-ulkomaanjaksot-comment"))),
    Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
    raportinTyyppi match {
      case PreIBSuoritusRaportti => Column(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssitOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssitOpintopisteet-comment")))
      case _ => Column(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKaikkiKurssit-comment")))
    },
    raportinTyyppi match {
      case PreIBSuoritusRaportti => Column(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssitOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssitOpintopisteet-comment")))
      case _ => Column(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusSuoritetutKurssit-comment")))
    },
    raportinTyyppi match {
      case PreIBSuoritusRaportti => Column(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssitOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssitOpintopisteet-comment")))
      case _ => Column(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusHylätytKurssit-comment")))
    },
    raportinTyyppi match {
      case PreIBSuoritusRaportti => Column(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssitOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssitOpintopisteet-comment")))
      case _ => Column(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusTunnustetutKurssit-comment")))
    },
    raportinTyyppi match {
      case PreIBSuoritusRaportti => Column(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssitOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssitOpintopisteet-comment")))
      case _ => Column(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit"), comment = Some(t.get("raportti-excel-kolumni-yhteislaajuusKorotetutKurssit-comment")))
    }
  ) ++ oppiaineet.map(x =>
    Column(title = x.oppiaine.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-comment")))
  )

  private def oppiaineKohtainenSheet(
    oppiaineJaKurssit: YleissivistäväRaporttiOppiaineJaKurssit,
    data: Seq[IBRaporttiRows],
    raportinTyyppi: IBSuoritustiedotRaporttiType
  ): DynamicDataSheet = {
    val oppiaine = oppiaineJaKurssit.oppiaine
    val kurssit = oppiaineJaKurssit.kurssit

    DynamicDataSheet(
      title = oppiaine.toSheetTitle(t),
      rows = data.map(oppiainekohtaisetOsasuorituksetiedot(_, kurssit, raportinTyyppi)).map(_.toSeq),
      columnSettings = oppiaineKohtaisetColumnSettings(kurssit)
    )
  }

  private def oppiainekohtaisetOsasuorituksetiedot(
    row: IBRaporttiRows,
    osasuoritukset: Seq[YleissivistäväRaporttiKurssi],
    raportinTyyppi: IBSuoritustiedotRaporttiType
  ): IBRaportinOppiaineenOsasuorituksetRow = {
    IBRaportinOppiaineenOsasuorituksetRow(
      stattisetKolumnit = IBRaporttiOppiaineenKurssienVälilehtiStaattisetKolumnit(
        oppijanOid = row.opiskeluoikeus.oppijaOid,
        hetu = row.henkilo.hetu,
        sukunimi = row.henkilo.sukunimi,
        etunimet = row.henkilo.etunimet,
        toimipiste = if (t.language == "sv") row.päätasonSuoritus.toimipisteNimiSv else row.päätasonSuoritus.toimipisteNimi,
        suorituksenTyyppi = row
          .päätasonSuoritus
          .koulutusModuulistaKäytettäväNimi(t.language)
          .getOrElse(row.päätasonSuoritus.suorituksenTyyppi)
      ),
      osasuoritukset = kurssienTiedot(row.osasuoritukset, osasuoritukset, raportinTyyppi)
    )
  }

  private def kurssienTiedot(
    osasuorituksetRow: Seq[ROsasuoritusRow],
    osasuoritukset: Seq[YleissivistäväRaporttiKurssi],
    raportinTyyppi: IBSuoritustiedotRaporttiType
  ): Seq[String] = {
    val osasuorituksetMap = osasuorituksetRow.groupBy(_.koulutusmoduuliKoodiarvo)
    osasuoritukset.map { kurssi =>
      osasuorituksetMap
        .getOrElse(kurssi.koulutusmoduuliKoodiarvo, Nil)
        .filter(_.matchesWith(kurssi, t.language))
        .map(kurssisuoritus =>
          IBModuulinTiedot(
            kurssinTyyppi = JsonSerializer.extract[Option[LocalizedString]](kurssisuoritus.data \ "koulutusmoduuli" \ "kurssinTyyppi" \ "nimi"),
            pakollinen = JsonSerializer.extract[Option[Boolean]](kurssisuoritus.data \ "koulutusmoduuli" \ "pakollinen"),
            arvosana = kurssisuoritus.arviointiArvosanaKoodiarvo,
            laajuus = raportinTyyppi match {
              case IBTutkinnonSuoritusRaportti => kurssisuoritus.koulutusmoduuliLaajuusArvo.orElse(Some(1.0))
              case _ => kurssisuoritus.koulutusmoduuliLaajuusArvo
            },
            tunnustettu = kurssisuoritus.tunnustettu,
            korotettuEriVuonna = kurssisuoritus.korotettuEriVuonna
          ).toStringLokalisoitu(t)
        )
        .mkString(",")
    }
  }

  private def oppiaineKohtaisetColumnSettings(osasuoritukset: Seq[YleissivistäväRaporttiKurssi]): Seq[Column] = {
    Seq(
      Column(t.get("raportti-excel-kolumni-oppijaOid")),
      Column(t.get("raportti-excel-kolumni-hetu")),
      Column(t.get("raportti-excel-kolumni-sukunimi")),
      Column(t.get("raportti-excel-kolumni-etunimet")),
      CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
      CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"),
        comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-ib-comment")))
    ) ++ osasuoritukset.map(k =>
      CompactColumn(title = k.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-comment")))
    )
  }
}

case class IBRaporttiRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  koulutustoimijaNimi: String,
  oppilaitoksenNimi: String,
  toimipisteNimi: String,
  lähdejärjestelmänId: Option[String],
  aikaleima: LocalDate,
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  opiskeluoikeudenViimeisinTila: Option[String],
  opiskeluoikeudenTilatAikajaksonAikana: String,
  päätasonSuoritukset: Option[String],
  opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  rahoitukset: String,
  rahoitusmuodotOk: Boolean,
  maksuttomuus: Option[String],
  oikeuttaMaksuttomuuteenPidennetty: Option[String],
  pidennettyPäättymispäivä: Boolean,
  ulkomainenVaihtoOpiskelija: Boolean,
  erityinenKoulutustehtäväJaksot: Option[Int],
  ulkomaanjaksot: Option[Int],
  sisäoppilaitosmainenMajoitus: Option[Int],
  yhteislaajuus: BigDecimal,
  yhteislaajuusSuoritetut: BigDecimal,
  yhteislaajuusHylätyt: BigDecimal,
  yhteislaajuusTunnustetut: BigDecimal,
  yhteislaajuusKorotettuEriVuonna: BigDecimal
)

case class IBRaporttiOppiaineenKurssienVälilehtiStaattisetKolumnit(
  oppijanOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  toimipiste: String,
  suorituksenTyyppi: String
)

case class IBRaportinOppiaineenOsasuorituksetRow(
  stattisetKolumnit: IBRaporttiOppiaineenKurssienVälilehtiStaattisetKolumnit,
  osasuoritukset: Seq[String]
) {
  def toSeq: Seq[Any] = stattisetKolumnit.productIterator.toList ++ osasuoritukset
}

case class IBModuulinTiedot(
  pakollinen: Option[Boolean],
  kurssinTyyppi: Option[LocalizedString],
  arvosana: Option[String],
  laajuus: Option[Double],
  tunnustettu: Boolean,
  korotettuEriVuonna: Boolean
) {
  def toStringLokalisoitu(t: LocalizationReader): String = {
    List(
      pakollinen.map { p =>
        if (p) t.get("raportti-excel-default-value-pakollinen").capitalize
        else t.get("raportti-excel-default-value-vapaavalintainen").capitalize
      }.orElse(
        Some(kurssinTyyppi.map(_.get(t.language)).getOrElse(t.get("raportti-excel-default-value-ei-tyyppiä")).capitalize)
      ),
      Some(arvosana
        .map(s"${t.get("raportti-excel-default-value-arvosana")} " + _)
        .getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))
      ),
      Some(laajuus
        .map(s"${t.get("raportti-excel-default-value-laajuus").capitalize} " + _)
        .getOrElse(t.get("raportti-excel-default-value-laajuus-puuttuu"))
      ),
      if (tunnustettu) Some(t.get("raportti-excel-default-value-tunnustettu")) else None,
      if (korotettuEriVuonna) Some(t.get("raportti-excel-default-value-korotettuEriVuonna")) else None
    ).flatten.mkString(", ")
  }
}

object IBSuoritustiedotRaporttiType {
  def raporttiTypeLokalisoitu(
    raportinTyyppi: IBSuoritustiedotRaporttiType,
    t: LocalizationReader
  ): String = raportinTyyppi match {
    case IBTutkinnonSuoritusRaportti => t.get("raportti-excel-ib-tutkinnon-suoritustiedot-tiedoston-tunniste")
    case PreIBSuoritusRaportti => t.get("raportti-excel-preib-suoritustiedot-tiedoston-tunniste")
    case _ => raportinTyyppi.typeName
  }
}

sealed trait IBSuoritustiedotRaporttiType {
  def typeName: String
  def päätasonSuoritusTyyppi: String
  def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean
  def isKurssi(osasuoritus: ROsasuoritusRow): Boolean
}

object IBTutkinnonSuoritusRaportti extends IBSuoritustiedotRaporttiType {
  val typeName = "ibtutkinto"
  val päätasonSuoritusTyyppi: String = "ibtutkinto"

  override def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean = osasuoritus.suorituksenTyyppi == "iboppiaine"

  override def isKurssi(osasuoritus: ROsasuoritusRow): Boolean = osasuoritus.suorituksenTyyppi == "ibkurssi"
}

object PreIBSuoritusRaportti extends IBSuoritustiedotRaporttiType {
  val typeName = "preiboppimaara"
  val päätasonSuoritusTyyppi: String = "preiboppimaara"

  override def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean = osasuoritus.suorituksenTyyppi == "lukionmuuopinto" ||
    osasuoritus.suorituksenTyyppi == "preiboppiaine" ||
    osasuoritus.suorituksenTyyppi == "lukionoppiaine" ||
    osasuoritus.suorituksenTyyppi == "lukionmuuopinto"

  override def isKurssi(osasuoritus: ROsasuoritusRow): Boolean = osasuoritus.suorituksenTyyppi == "lukionkurssi" ||
    osasuoritus.suorituksenTyyppi == "preibkurssi" ||
    osasuoritus.suorituksenTyyppi == "lukionvaltakunnallinenmoduuli" ||
    osasuoritus.suorituksenTyyppi == "lukionpaikallinenopintojakso"
}
