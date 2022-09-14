package fi.oph.koski.raportit.tuva

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DynamicDataSheet}
import fi.oph.koski.raportit.YleissivistäväUtils.{lengthInDaysInDateRange, rahoitusmuodotOk, removeContinuousSameTila}
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._

import java.time.LocalDate

case class TuvaSuoritustiedotRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  koulutustoimijaNimi: String,
  oppilaitosNimi: String,
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
  järjestämislupa: String,
  järjestämislupaNimi: Option[String],
  maksuttomuus: Option[String],
  oikeuttaMaksuttomuuteenPidennetty: Option[String],
  majoitusPäivät: Option[Int],
  majoitusetuPäivät: Option[Int],
  sisäoppilaitosmainenMajoitusPäivät: Option[Int],
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät: Option[Int],
  erityinenTukiPäivät: Option[Int],
  erityisenTuenPäätöksetPäivät: Option[Int],
  koulukotiPäivät: Option[Int],
  vaativanErityisenTuenErityinenTehtäväPäivät: Option[Int],
  kuljetusetuPäivät: Option[Int],
  ulkomaanjaksot: Option[Int],
  vammainenPäivät: Option[Int],
  vaikeastiVammainenPäivät: Option[Int],
  vammainenJaAvustajaPäivät: Option[Int],
  osaAikaisuusProsentit: Option[String],
  osaAikaisuusKeskimäärin: Option[Double],
  vankilaopetuksessaPäivät: Option[Int],
  koulutusvienti: Option[Boolean],
  pidennettyPäättymispäivä: Option[Boolean]
){
  def kentät(järjestämislupa: String): Seq[Any] = järjestämislupa match {
    case s: String if s == TuvaSuoritustiedotRaportti.järjestämislupaAmmatillinen => ammatillisenKentät
    case s: String if s == TuvaSuoritustiedotRaportti.järjestämislupaLukio => lukioKentät
    case s: String if s == TuvaSuoritustiedotRaportti.järjestämislupaPerusopetus => perusopetusKentät
    case _ => yhteisetKentät
  }

  val yhteisetKentät: Seq[Any] = Seq(
    opiskeluoikeusOid,
    lähdejärjestelmä,
    koulutustoimijaNimi,
    oppilaitosNimi,
    toimipisteNimi,
    lähdejärjestelmänId,
    aikaleima,
    yksiloity,
    oppijaOid,
    hetu,
    sukunimi,
    etunimet,
    opiskeluoikeudenAlkamispäivä,
    opiskeluoikeudenViimeisinTila,
    opiskeluoikeudenTilatAikajaksonAikana,
    päätasonSuoritukset,
    opiskeluoikeudenPäättymispäivä,
    rahoitukset,
    rahoitusmuodotOk,
    järjestämislupaNimi,
    maksuttomuus,
    oikeuttaMaksuttomuuteenPidennetty
  )
  lazy val ammatillisenKentät: Seq[Any] = yhteisetKentät ++ Seq(
    majoitusPäivät,
    sisäoppilaitosmainenMajoitusPäivät,
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät,
    erityinenTukiPäivät,
    vaativanErityisenTuenErityinenTehtäväPäivät,
    ulkomaanjaksot,
    vaikeastiVammainenPäivät,
    vammainenJaAvustajaPäivät,
    osaAikaisuusProsentit,
    osaAikaisuusKeskimäärin,
    vankilaopetuksessaPäivät,
    koulutusvienti,
    pidennettyPäättymispäivä
  )
  lazy val lukioKentät: Seq[Any] = yhteisetKentät ++ Seq(
    sisäoppilaitosmainenMajoitusPäivät,
    ulkomaanjaksot,
    pidennettyPäättymispäivä
  )
  lazy val perusopetusKentät: Seq[Any] = yhteisetKentät ++ Seq(
    majoitusetuPäivät,
    sisäoppilaitosmainenMajoitusPäivät,
    erityisenTuenPäätöksetPäivät,
    koulukotiPäivät,
    kuljetusetuPäivät,
    ulkomaanjaksot,
    vammainenPäivät,
    vaikeastiVammainenPäivät,
    pidennettyPäättymispäivä
  )
}

object TuvaSuoritustiedotRaportti {

  val järjestämislupaAmmatillinen = "ammatillinen"
  val järjestämislupaLukio = "lukio"
  val järjestämislupaPerusopetus = "perusopetus"

  val defaultJärjestämisluvat = Seq(järjestämislupaAmmatillinen, järjestämislupaLukio, järjestämislupaPerusopetus)

  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[DynamicDataSheet] = {
    val rows = raportointiDatabase
      .opiskeluoikeusAikajaksot(oppilaitosOid, OpiskeluoikeudenTyyppi.tuva.koodiarvo, alku, loppu)
      .map(r => buildRow(alku, loppu, r, t))
      .groupBy(_.järjestämislupa)

    val järjestämisLuvatRaportilla = if(rows.nonEmpty) rows.keySet.toSeq else defaultJärjestämisluvat

    järjestämisLuvatRaportilla.map{ järjestämislupa =>
      DynamicDataSheet(
        title = järjestämislupa match {
          case s: String if s == järjestämislupaAmmatillinen => t.get("raportti-excel-tuva-ammatillinen-sheet-name")
          case s: String if s == järjestämislupaLukio => t.get("raportti-excel-tuva-lukio-sheet-name")
          case s: String if s == järjestämislupaPerusopetus => t.get("raportti-excel-tuva-perusopetus-sheet-name")
          case _ => järjestämislupa
        },
        rows = rows.getOrElse(järjestämislupa, Seq.empty).map(_.kentät(järjestämislupa)),
        columnSettings = columnSettings(järjestämislupa, t)
      )
    }.sortBy(_.title)
  }

  def columnSettings(järjestämislupa: String, t: LocalizationReader): Seq[Column] = {
    val yhteisetSarakkeet = Seq(
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
      Column(t.get("raportti-excel-kolumni-järjestämislupa")),
      Column(t.get("raportti-excel-kolumni-maksuttomuus"), comment = Some(t.get("raportti-excel-kolumni-maksuttomuus-comment"))),
      Column(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty"), comment = Some(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty-comment")))
    )
  lazy val ammatillisenSarakkeet = Seq(
      Column(t.get("raportti-excel-kolumni-majoitusPäivät")),
      Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitusPäivät"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
      Column(t.get("raportti-excel-kolumni-vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät")),
      Column(t.get("raportti-excel-kolumni-erityinenTukiPäivät")),
      Column(t.get("raportti-excel-kolumni-vaativanErityisenTuenErityinenTehtäväPäivät")),
      Column(t.get("raportti-excel-kolumni-ulkomaanjaksotPäivät")),
      Column(t.get("raportti-excel-kolumni-vaikeastiVammainenPäivät")),
      Column(t.get("raportti-excel-kolumni-vammainenJaAvustajaPäivät")),
      Column(t.get("raportti-excel-kolumni-osaAikaisuusProsentit")),
      Column(t.get("raportti-excel-kolumni-osaAikaisuusKeskimäärin")),
      Column(t.get("raportti-excel-kolumni-vankilaopetuksessaPäivät")),
      Column(t.get("raportti-excel-kolumni-lisätiedotKoulutusvienti")),
      Column(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä")),
    )
  lazy val lukionSarakkeet = Seq(
      Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitusPäivät"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
      Column(t.get("raportti-excel-kolumni-ulkomaanjaksotPäivät")),
      Column(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä")),
    )
  lazy val perusopetuksenSarakkeet = Seq(
      Column(t.get("raportti-excel-kolumni-majoitusetuPäivät"), comment = Some(t.get("raportti-excel-kolumni-majoitusetu-count-comment"))),
      Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitusPäivät"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-count-comment"))),
      Column(t.get("raportti-excel-kolumni-erityisenTuenPäätöksetPäivät")),
      Column(t.get("raportti-excel-kolumni-koulukotiPäivät")),
      Column(t.get("raportti-excel-kolumni-kuljetusetuPäivät")),
      Column(t.get("raportti-excel-kolumni-ulkomaanjaksotPäivät")),
      Column(t.get("raportti-excel-kolumni-muuKuinVaikeimminKehitysvammainenPäivät")),
      Column(t.get("raportti-excel-kolumni-vaikeimminKehitysvammainen")),
      Column(t.get("raportti-excel-kolumni-pidennettyPäättymispäivä"))
    )

    järjestämislupa match {
      case s: String if s == järjestämislupaAmmatillinen => yhteisetSarakkeet ++ ammatillisenSarakkeet
      case s: String if s == järjestämislupaLukio => yhteisetSarakkeet ++ lukionSarakkeet
      case s: String if s == järjestämislupaPerusopetus => yhteisetSarakkeet ++ perusopetuksenSarakkeet
      case _ => yhteisetSarakkeet
    }
  }

  def buildRow(
    alku: LocalDate,
    loppu: LocalDate,
    data: (ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow]),
    t: LocalizationReader
  ): TuvaSuoritustiedotRow = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset, _) = data
    val järjestämislupa = opiskeluoikeus.tuvaJärjestämislupa.getOrElse("N/A") // Järjestämislupa pitäisi löytyä aina
    val lisätiedot: Option[TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot] = järjestämislupa match {
      case s: String if s == järjestämislupaAmmatillinen =>
        JsonSerializer.extract[Option[TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot]](opiskeluoikeus.data \ "lisätiedot")
      case s: String if s == järjestämislupaLukio =>
        JsonSerializer.extract[Option[TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot]](opiskeluoikeus.data \ "lisätiedot")
      case s: String if s == järjestämislupaPerusopetus =>
        JsonSerializer.extract[Option[TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot]](opiskeluoikeus.data \ "lisätiedot")
      case _ => None
    }
    val päätasonSuoritus = päätasonSuoritukset.head // Täsmälleen yksi päätason suoritus jokaisella TUVA opiskeluoikeudella

    TuvaSuoritustiedotRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = opiskeluoikeus.lähdejärjestelmäKoodiarvo,
      koulutustoimijaNimi = if(t.language == "sv") opiskeluoikeus.koulutustoimijaNimiSv else opiskeluoikeus.koulutustoimijaNimi,
      oppilaitosNimi = if(t.language == "sv") opiskeluoikeus.oppilaitosNimiSv else opiskeluoikeus.oppilaitosNimi,
      toimipisteNimi = if(t.language == "sv") päätasonSuoritus.toimipisteNimiSv else päätasonSuoritus.toimipisteNimi,
      lähdejärjestelmänId = opiskeluoikeus.lähdejärjestelmäId,
      aikaleima = opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      yksiloity = henkilö.yksiloity,
      oppijaOid = henkilö.oppijaOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      opiskeluoikeudenViimeisinTila = opiskeluoikeus.viimeisinTila,
      opiskeluoikeudenTilatAikajaksonAikana = removeContinuousSameTila(aikajaksot).map(_.tila).mkString(", "),
      päätasonSuoritukset = päätasonSuoritus.koulutusModuulistaKäytettäväNimi(t.language),
      opiskeluoikeudenPäättymispäivä = opiskeluoikeus.päättymispäivä.map(_.toLocalDate),
      rahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).mkString(", "),
      rahoitusmuodotOk = rahoitusmuodotOk(aikajaksot),
      järjestämislupa = järjestämislupa,
      järjestämislupaNimi = JsonSerializer.extract[Option[LocalizedString]](opiskeluoikeus.data \ "järjestämislupa" \ "nimi").map(_.get(t.language)),
      maksuttomuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
      oikeuttaMaksuttomuuteenPidennetty = lisätiedot.flatMap(_.oikeuttaMaksuttomuuteenPidennetty.map(omps => omps.map(_.toString).mkString(", "))).filter(_.nonEmpty),
      majoitusPäivät = Some(aikajaksoPäivät(aikajaksot, _.majoitus)),
      majoitusetuPäivät = Some(aikajaksoPäivät(aikajaksot, _.majoitusetu)),
      sisäoppilaitosmainenMajoitusPäivät = Some(aikajaksoPäivät(aikajaksot, _.sisäoppilaitosmainenMajoitus)),
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät = Some(aikajaksoPäivät(aikajaksot, _.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus)),
      erityinenTukiPäivät = Some(aikajaksoPäivät(aikajaksot, _.sisäoppilaitosmainenMajoitus)),
      erityisenTuenPäätöksetPäivät = Some(aikajaksoPäivät(aikajaksot, _.erityinenTuki)),
      koulukotiPäivät = Some(aikajaksoPäivät(aikajaksot, _.koulukoti)),
      vaativanErityisenTuenErityinenTehtäväPäivät = Some(aikajaksoPäivät(aikajaksot, _.vaativanErityisenTuenErityinenTehtävä)),
      kuljetusetuPäivät = Some(aikajaksoPäivät(aikajaksot, _.kuljetusetu)),
      ulkomaanjaksot = lisätiedot.flatMap(_.ulkomaanjaksot.map(_.map(lengthInDaysInDateRange(_, alku, loppu)).sum)).orElse(Some(0)),
      vammainenPäivät = Some(aikajaksoPäivät(aikajaksot, _.vammainen)),
      vaikeastiVammainenPäivät = Some(aikajaksoPäivät(aikajaksot, _.vaikeastiVammainen)),
      vammainenJaAvustajaPäivät = Some(aikajaksoPäivät(aikajaksot, _.vammainenJaAvustaja)),
      osaAikaisuusProsentit = Some(distinctAdjacent(aikajaksot.map(_.osaAikaisuus)).mkString(",")).filter(_ != "100"),
      osaAikaisuusKeskimäärin = Some(aikajaksot.map(a => a.osaAikaisuus * a.lengthInDays).sum.toDouble / aikajaksot.map(_.lengthInDays).sum),
      vankilaopetuksessaPäivät = Some(aikajaksoPäivät(aikajaksot, _.vankilaopetuksessa)),
      koulutusvienti = lisätiedot.flatMap {
        case l: TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot => l.koulutusvienti
        case _ => None
      },
      pidennettyPäättymispäivä = lisätiedot.flatMap(_.pidennettyPäättymispäivä)
    )
  }

  private def distinctAdjacent[A](input: Seq[A]): Seq[A] = {
    if (input.size < 2) {
      input
    } else {
      val rest = input.dropWhile(_ == input.head)
      input.head +: distinctAdjacent(rest)
    }
  }

  private def aikajaksoPäivät(aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow], f: ROpiskeluoikeusAikajaksoRow => Boolean): Int =
    aikajaksot.map(aikajakso => if(f(aikajakso)) aikajakso.lengthInDays else 0).sum
}
