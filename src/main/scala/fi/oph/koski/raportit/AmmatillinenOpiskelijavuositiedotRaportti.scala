package fi.oph.koski.raportit

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.AmmatillinenRaporttiUtils.aikajaksoPäivät
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{Aikajakso, AmmatillisenOpiskeluoikeudenLisätiedot, LähdejärjestelmäId, OpiskeluoikeudenTyyppi, Organisaatio, Osaamisalajakso}
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}

case class OpiskelijavuositiedotRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  sisältyyOpiskeluoikeuteenOid: String,
  korotettuOpiskeluoikeusOidit: String,
  ostettu: Boolean,
  sisältyvätOpiskeluoikeudetOidit: String,
  sisältyvätOpiskeluoikeudetOppilaitokset: String,
  aikaleima: LocalDate,
  toimipisteOidit: String,
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  suorituksenTyyppi: String,
  koulutusmoduulit: String,
  päätasonSuoritustenNimet: String,
  osaamisalat: Option[String],
  päätasonSuorituksenSuoritustapa: String,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinOpiskeluoikeudenTila: Option[String],
  viimeisinOpiskeluoikeudenTilaAikajaksonLopussa: String,
  opintojenRahoitukset: String,
  läsnäRahoitusSyötetty: Boolean,
  lomaTaiValmistunutRahoitusSyötetty: Boolean,
  opiskeluoikeusPäättynyt: Boolean,
  päättymispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  opiskelijavuosikertymä: Double,
  läsnäTaiValmistunutPäivät: Int,
  opiskelijavuoteenKuuluvatLomaPäivät: Int,
  muutLomaPäivät: Int,
  majoitusPäivät: Int,
  sisäoppilaitosmainenMajoitusPäivät: Int,
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät: Int,
  erityinenTukiPäivät: Int,
  vaativanErityisenTuenErityinenTehtäväPäivät: Int,
  hojksPäivät: Int,
  vaikeastiVammainenPäivät: Int,
  vammainenJaAvustajaPäivät: Int,
  osaAikaisuusProsentit: Option[String],
  osaAikaisuusKeskimäärin: Double,
  opiskeluvalmiuksiaTukevatOpinnotPäivät: Int,
  vankilaopetuksessaPäivät: Int,
  oppisopimusJossainPäätasonSuorituksessaPäivät: Int,
  lisätiedotHenkilöstökoulutus: Boolean,
  lisätiedotKoulutusvienti: Boolean,
  maksuttomuus: Option[String],
  maksullisuus: Option[String],
  oikeuttaMaksuttomuuteenPidennetty: Option[String]
)

object AmmatillinenOpiskalijavuositiedotRaportti extends AikajaksoRaportti {

  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    oppilaitosOid: Organisaatio.Oid,
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[OpiskelijavuositiedotRow] = {
    raportointiDatabase
      .opiskeluoikeusAikajaksot(oppilaitosOid, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, alku, loppu)
      .map(r => buildRow(alku, loppu, r, t))
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä"), width = Some(4000)),
    "lähdejärjestelmänId" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId"), width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column(t.get("raportti-excel-kolumni-sisältyyOpiskeluoikeuteenOid"), width = Some(4000)),
    "korotettuOpiskeluoikeusOidit" -> Column(t.get("raportti-excel-kolumni-korotettuOpiskeluoikeusOid"), width = Some(4000)),
    "ostettu"-> Column(t.get("raportti-excel-kolumni-ostettu"), width = Some(2000)),
    "sisältyvätOpiskeluoikeudetOidit" -> Column(t.get("raportti-excel-kolumni-sisältyvätOpiskeluoikeudetOidit"), width = Some(4000)),
    "sisältyvätOpiskeluoikeudetOppilaitokset"-> Column(t.get("raportti-excel-kolumni-sisältyvätOpiskeluoikeudetOppilaitokset"), width = Some(4000)),
    "aikaleima" -> Column(t.get("raportti-excel-kolumni-päivitetty")),
    "toimipisteOidit" -> Column(t.get("raportti-excel-kolumni-toimipisteOidit")),
    "yksiloity" -> Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "suorituksenTyyppi" -> Column(t.get("raportti-excel-kolumni-suorituksenTyyppi")),
    "koulutusmoduulit" -> Column(t.get("raportti-excel-kolumni-koulutusmoduulit")),
    "päätasonSuoritustenNimet" -> Column(t.get("raportti-excel-kolumni-koulutusmoduuliNimet")),
    "osaamisalat" -> Column(t.get("raportti-excel-kolumni-osaamisalat")),
    "päätasonSuorituksenSuoritustapa" -> Column(t.get("raportti-excel-kolumni-päätasonSuorituksenSuoritustapa")),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "viimeisinOpiskeluoikeudenTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila")),
    "viimeisinOpiskeluoikeudenTilaAikajaksonLopussa" -> Column(t.get("raportti-excel-kolumni-viimeisinOpiskeluoikeudenTilaAikajaksonLopussa")),
    "opintojenRahoitukset" -> Column(t.get("raportti-excel-kolumni-rahoitukset")),
    "läsnäRahoitusSyötetty"-> Column(t.get("raportti-excel-kolumni-läsnäRahoitusSyötetty"), width = Some(2000)),
    "lomaTaiValmistunutRahoitusSyötetty" -> Column(t.get("raportti-excel-kolumni-lomaTaiValmistunutRahoitusSyötetty"), width = Some(2000)),
    "opiskeluoikeusPäättynyt" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusPäättynyt"), width = Some(2000)),
    "päättymispäivä" -> Column(t.get("raportti-excel-kolumni-päättymispäivä")),
    "arvioituPäättymispäivä" -> Column(t.get("raportti-excel-kolumni-arvioituPäättymispäivä")),
    "opiskelijavuosikertymä" -> Column(t.get("raportti-excel-kolumni-opiskelijavuosikertymä"), width = Some(2000)),
    "läsnäTaiValmistunutPäivät" -> Column(t.get("raportti-excel-kolumni-läsnäTaiValmistunutPäivät"), width = Some(2000)),
    "opiskelijavuoteenKuuluvatLomaPäivät" -> Column(t.get("raportti-excel-kolumni-opiskelijavuoteenKuuluvatLomaPäivät"), width = Some(2000)),
    "muutLomaPäivät" -> Column(t.get("raportti-excel-kolumni-muutLomaPäivät"), width = Some(2000)),
    "majoitusPäivät" -> Column(t.get("raportti-excel-kolumni-majoitusPäivät"), width = Some(2000)),
    "sisäoppilaitosmainenMajoitusPäivät" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitusPäivät"), width = Some(2000)),
    "vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät" -> Column(t.get("raportti-excel-kolumni-vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät"), width = Some(2000)),
    "erityinenTukiPäivät" -> Column(t.get("raportti-excel-kolumni-erityinenTukiPäivät"), width = Some(2000)),
    "vaativanErityisenTuenErityinenTehtäväPäivät" -> Column(t.get("raportti-excel-kolumni-vaativanErityisenTuenErityinenTehtäväPäivät"), width = Some(2000)),
    "hojksPäivät" -> Column(t.get("raportti-excel-kolumni-hojksPäivät"), width = Some(2000)),
    "vaikeastiVammainenPäivät" -> Column(t.get("raportti-excel-kolumni-vaikeastiVammainenPäivät"), width = Some(2000)),
    "vammainenJaAvustajaPäivät" -> Column(t.get("raportti-excel-kolumni-vammainenJaAvustajaPäivät"), width = Some(2000)),
    "osaAikaisuusProsentit" -> Column(t.get("raportti-excel-kolumni-osaAikaisuusProsentit"), width = Some(2000)),
    "osaAikaisuusKeskimäärin" -> Column(t.get("raportti-excel-kolumni-osaAikaisuusKeskimäärin"), width = Some(2000)),
    "opiskeluvalmiuksiaTukevatOpinnotPäivät" -> Column(t.get("raportti-excel-kolumni-opiskeluvalmiuksiaTukevatOpinnotPäivät"), width = Some(2000)),
    "vankilaopetuksessaPäivät" -> Column(t.get("raportti-excel-kolumni-vankilaopetuksessaPäivät"), width = Some(2000)),
    "oppisopimusJossainPäätasonSuorituksessaPäivät" -> Column(t.get("raportti-excel-kolumni-oppisopimusJossainPäätasonSuorituksessaPäivät"), width = Some(2000)),
    "lisätiedotHenkilöstökoulutus" -> Column(t.get("raportti-excel-kolumni-lisätiedotHenkilöstökoulutus"), width = Some(2000)),
    "lisätiedotKoulutusvienti" -> Column(t.get("raportti-excel-kolumni-lisätiedotKoulutusvienti"), width = Some(2000)),
    "maksuttomuus" -> Column(t.get("raportti-excel-kolumni-maksuttomuus"), width = Some(2000)),
    "maksullisuus" -> Column(t.get("raportti-excel-kolumni-maksullisuus"), width = Some(2000)),
    "oikeuttaMaksuttomuuteenPidennetty" -> Column(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty"), width = Some(2000))
  )

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): String =
    s"${t.get("raportti-excel-ammatillinen-opiskelijavuosi-tiedoston-etuliite")}_${oppilaitosOid}_${alku.toString.replaceAll("-", "")}-${loppu.toString.replaceAll("-", "")}.xlsx"

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): String =
    s"${t.get("raportti-excel-ammatillinen-opiskelijavuosi-title-etuliite")} $oppilaitosOid ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}"

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadStarted: LocalDateTime, t: LocalizationReader): String = s"""
    |${t.get("raportti-excel-ammatillinen-opiskelijavuosi-ohje-title")}
    |${t.get("raportti-excel-kolumni-oppilaitosOid")}: $oppilaitosOid
    |${t.get("raportti-excel-ammatillinen-ohje-aikajakso")}: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
    |${t.get("raportti-excel-ammatillinen-ohje-luotu")}: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadStarted)} ${t.get("raportti-excel-ammatillinen-ohje-luotu-takaliite")})
    |
    |${t.get("raportti-excel-ammatillinen-opiskelijavuosi-ohje-body")}
    """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  def buildRow(
    alku: LocalDate,
    loppu: LocalDate,
    data: (ROpiskeluoikeusRow, RHenkilöRow, Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROpiskeluoikeusRow]),
    t: LocalizationReader
  ): OpiskelijavuositiedotRow = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset, sisältyvätOpiskeluoikeudet) = data
    val osaamisalat = päätasonSuoritukset
      .flatMap(s => JsonSerializer.extract[Option[List[Osaamisalajakso]]](s.data \ "osaamisala"))
      .flatten
      .filterNot(j => j.alku.exists(_.isAfter(loppu)))
      .filterNot(j => j.loppu.exists(_.isBefore(alku)))
      .map(_.osaamisala.koodiarvo)
      .sorted
      .distinct
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val arvioituPäättymispäivä = JsonSerializer.validateAndExtract[Option[LocalDate]](opiskeluoikeus.data \ "arvioituPäättymispäivä").toOption.flatten
    val (opiskelijavuoteenKuuluvatLomaPäivät, muutLomaPäivät) = AmmatillinenRaporttiUtils.lomaPäivät(aikajaksot)
    val (koulutusmoduulit, päätasonSuoritustenNimet) =
      päätasonSuoritukset
        .sortBy(_.koulutusmoduuliKoodiarvo)
        .map(ps => (
          ps.koulutusmoduuliKoodiarvo,
          ps.perusteestaKäytettäväNimi(t.language).orElse(ps.koulutusModuulistaKäytettäväNimi(t.language)).getOrElse("")
        ))
        .unzip
    val lisätiedot = JsonSerializer.extract[Option[AmmatillisenOpiskeluoikeudenLisätiedot]](opiskeluoikeus.data \ "lisätiedot")
    val korotettuOpiskeluoikeusOidit = päätasonSuoritukset.flatMap(s => JsonSerializer.extract[Option[String]](s.data \ "korotettuOpiskeluoikeusOid"))

    OpiskelijavuositiedotRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      sisältyyOpiskeluoikeuteenOid = opiskeluoikeus.sisältyyOpiskeluoikeuteenOid.getOrElse(""),
      korotettuOpiskeluoikeusOidit = korotettuOpiskeluoikeusOidit.mkString(","),
      ostettu = JsonSerializer.validateAndExtract[Boolean](opiskeluoikeus.data \ "ostettu").getOrElse(false),
      sisältyvätOpiskeluoikeudetOidit = sisältyvätOpiskeluoikeudet.map(_.opiskeluoikeusOid).mkString(","),
      sisältyvätOpiskeluoikeudetOppilaitokset = sisältyvätOpiskeluoikeudet
        .map(oo => if (t.language == "sv") oo.oppilaitosNimiSv else oo.oppilaitosNimi)
        .mkString(","),
      aikaleima = opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      toimipisteOidit = päätasonSuoritukset.map(_.toimipisteOid).sorted.distinct.mkString(","),
      yksiloity = henkilö.yksiloity,
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      suorituksenTyyppi = päätasonSuoritukset.map(_.suorituksenTyyppi).mkString(","),
      koulutusmoduulit = koulutusmoduulit.mkString(","),
      päätasonSuoritustenNimet = päätasonSuoritustenNimet.mkString(","),
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      päätasonSuorituksenSuoritustapa = AmmatillinenRaporttiUtils.suoritusTavat(päätasonSuoritukset, t.language),
      opiskeluoikeudenAlkamispäivä = opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinOpiskeluoikeudenTila = opiskeluoikeus.viimeisinTila,
      viimeisinOpiskeluoikeudenTilaAikajaksonLopussa = aikajaksot.last.tila,
      opintojenRahoitukset = aikajaksot.flatMap(_.opintojenRahoitus).sorted.distinct.mkString(","),
      läsnäRahoitusSyötetty = aikajaksot.filter(_.tila == "lasna").forall(_.opintojenRahoitus.nonEmpty),
      lomaTaiValmistunutRahoitusSyötetty = aikajaksot.filter(a => a.tila == "loma" || a.tila == "valmistunut").forall(_.opintojenRahoitus.nonEmpty),
      opiskeluoikeusPäättynyt = aikajaksot.last.opiskeluoikeusPäättynyt,
      päättymispäivä = aikajaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku.toLocalDate), // toimii koska päättävä jakso on aina yhden päivän mittainen, jolloin truncateToDates ei muuta sen alkupäivää
      arvioituPäättymispäivä = arvioituPäättymispäivä,
      opiskelijavuosikertymä = AmmatillinenRaporttiUtils.opiskelijavuosikertymä(aikajaksot),
      läsnäTaiValmistunutPäivät = aikajaksoPäivät(aikajaksot, a => (a.tila == "lasna" || a.tila == "valmistunut")),
      opiskelijavuoteenKuuluvatLomaPäivät = opiskelijavuoteenKuuluvatLomaPäivät,
      muutLomaPäivät = muutLomaPäivät,
      majoitusPäivät = aikajaksoPäivät(aikajaksot, _.majoitus),
      sisäoppilaitosmainenMajoitusPäivät = aikajaksoPäivät(aikajaksot, _.sisäoppilaitosmainenMajoitus),
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät = aikajaksoPäivät(aikajaksot, _.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus),
      erityinenTukiPäivät = aikajaksoPäivät(aikajaksot, _.erityinenTuki),
      vaativanErityisenTuenErityinenTehtäväPäivät = aikajaksoPäivät(aikajaksot, _.vaativanErityisenTuenErityinenTehtävä),
      hojksPäivät = aikajaksoPäivät(aikajaksot, _.hojks),
      vaikeastiVammainenPäivät = aikajaksoPäivät(aikajaksot, _.vaikeastiVammainen),
      vammainenJaAvustajaPäivät = aikajaksoPäivät(aikajaksot, _.vammainenJaAvustaja),
      osaAikaisuusProsentit = Some(AmmatillinenRaporttiUtils.distinctAdjacent(aikajaksot.map(_.osaAikaisuus)).mkString(",")).filter(_ != "100"),
      osaAikaisuusKeskimäärin = aikajaksot.map(a => a.osaAikaisuus * a.lengthInDays).sum.toDouble / aikajaksot.map(_.lengthInDays).sum,
      opiskeluvalmiuksiaTukevatOpinnotPäivät = aikajaksoPäivät(aikajaksot, _.opiskeluvalmiuksiaTukevatOpinnot),
      vankilaopetuksessaPäivät = aikajaksoPäivät(aikajaksot, _.vankilaopetuksessa),
      oppisopimusJossainPäätasonSuorituksessaPäivät = aikajaksoPäivät(aikajaksot, _.oppisopimusJossainPäätasonSuorituksessa),
      lisätiedotHenkilöstökoulutus = opiskeluoikeus.lisätiedotHenkilöstökoulutus,
      lisätiedotKoulutusvienti = opiskeluoikeus.lisätiedotKoulutusvienti,
      maksuttomuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
      maksullisuus = lisätiedot.flatMap(_.maksuttomuus.map(ms => ms.filter(m => !m.maksuton && m.overlaps(Aikajakso(alku, Some(loppu)))).map(_.toString).mkString(", "))).filter(_.nonEmpty),
      oikeuttaMaksuttomuuteenPidennetty = lisätiedot.flatMap(_.oikeuttaMaksuttomuuteenPidennetty.map(omps => omps.map(_.toString).mkString(", "))).filter(_.nonEmpty),
    )
  }
}
