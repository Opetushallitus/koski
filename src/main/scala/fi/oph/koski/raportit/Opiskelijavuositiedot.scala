package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{LähdejärjestelmäId, Organisaatio, Osaamisalajakso}
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}

case class OpiskelijavuositiedotRow(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  sisältyyOpiskeluoikeuteenOid: Option[String],
  aikaleima: LocalDate,
  toimipisteOidit: String,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  koulutusmoduulit: String,
  osaamisalat: Option[String],
  viimeisinOpiskeluoikeudenTila: String,
  opintojenRahoitukset: Option[String],
  opiskeluoikeusPäättynyt: Boolean,
  päättymispäivä: Option[LocalDate],
  läsnäPäivät: Int,
  lomaPäivät: Int,
  majoitusPäivät: Int,
  sisäoppilaitosmainenMajoitusPäivät: Int,
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät: Int,
  erityinenTukiPäivät: Int,
  vaativanErityisenTuenErityinenTehtäväPäivät: Int,
  hojksPäivät: Int,
  vaikeastiVammainenPäivät: Int,
  vammainenJaAvustajaPäivät: Int,
  osaAikaisuusProsentit: Option[String],
  osaAikaisuusKeskimäärin: Float,
  opiskeluvalmiuksiaTukevatOpinnotPäivät: Int,
  vankilaopetuksessaPäivät: Int,
  oppisopimusJossainPäätasonSuorituksessaPäivät: Int,
  lisätiedotHenkilöstökoulutus: Boolean,
  lisätiedotKoulutusvienti: Boolean
)

object Opiskelijavuositiedot {

  private[raportit] def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[OpiskelijavuositiedotRow] = {
    val result = raportointiDatabase.opiskeluoikeusAikajaksot(oppilaitosOid, "ammatillinenkoulutus", alku, loppu)
    val rows = result.map(r => buildRow(alku, loppu, r))
    rows
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä", width = Some(4000)),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä", width = Some(4000)),
    "sisältyyOpiskeluoikeuteenOid" -> Column("Sisältyy opiskeluoikeuteen"),
    "aikaleima" -> Column("Päivitetty"),
    "toimipisteOidit" -> Column("Toimipisteet"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("Hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "koulutusmoduulit" -> Column("Tutkinnot"),
    "osaamisalat" -> Column("Osaamisalat"),
    "viimeisinOpiskeluoikeudenTila" -> Column("Viimeisin tila"),
    "opintojenRahoitukset" -> Column("Rahoitukset"),
    "opiskeluoikeusPäättynyt" -> Column("Päättynyt"),
    "päättymispäivä" -> Column("Päättymispäivä"),
    "läsnäPäivät" -> Column("Läsnä (pv)", width = Some(2000)),
    "lomaPäivät" -> Column("Loma (pv)", width = Some(2000)),
    "majoitusPäivät" -> Column("Majoitus (pv)", width = Some(2000)),
    "sisäoppilaitosmainenMajoitusPäivät" -> Column("Sisäoppilaitosmainen majoitus (pv)", width = Some(2000)),
    "vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät" -> Column("Vaativan erityisen tuen yhteydessä järjestettävä majoitus (pv)", width = Some(2000)),
    "erityinenTukiPäivät" -> Column("Erityinen tuki (pv)", width = Some(2000)),
    "vaativanErityisenTuenErityinenTehtäväPäivät" -> Column("Vaativat erityisen tuen tehtävä (pv)", width = Some(2000)),
    "hojksPäivät" -> Column("Hojks (pv)", width = Some(2000)),
    "vaikeastiVammainenPäivät" -> Column("Vaikeasti vammainen (pv)", width = Some(2000)),
    "vammainenJaAvustajaPäivät" -> Column("Vammainen ja avustaja (pv)", width = Some(2000)),
    "osaAikaisuusProsentit" -> Column("Osa-aikaisuusjaksot (prosentit)", width = Some(2000)),
    "osaAikaisuusKeskimäärin" -> Column("Osa-aikaisuus keskimäärin (%)", width = Some(2000)),
    "opiskeluvalmiuksiaTukevatOpinnotPäivät" -> Column("Opiskeluvalmiuksia tukevat opinnot (pv)", width = Some(2000)),
    "vankilaopetuksessaPäivät" -> Column("Vankilaopetuksessa (pv)", width = Some(2000)),
    "oppisopimusJossainPäätasonSuorituksessaPäivät" -> Column("Oppisopimus (pv)", width = Some(2000)),
    "lisätiedotHenkilöstökoulutus" -> Column("Henkilöstökoulutus", width = Some(2000)),
    "lisätiedotKoulutusvienti" -> Column("Koulutusvienti", width = Some(2000))
  )

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"opiskelijavuositiedot_${oppilaitosOid}_${alku.toString.replaceAll("-", "")}-${loppu.toString.replaceAll("-", "")}.xlsx"

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate): String =
    s"Opiskelijavuositiedot $oppilaitosOid ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}"

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String = s"""
    |Opiskelijavuositiedot (ammatillinen koulutus)
    |Oppilaitos: $oppilaitosOid
    |Aikajakso: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
    |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadCompleted.toLocalDateTime)} tietojen pohjalta)
    |
    |Tarkempia ohjeita taulukon sisällöstä:
    |
    |- Tutkinnot: kaikki opiskeluoikeudella olevat päätason suoritusten tutkinnot pilkulla erotettuna (myös ennen raportin aikajaksoa valmistuneet, ja raportin aikajakson jälkeen alkaneet)
    |- Osaamisalat: kaikkien ym. tutkintojen osaamisalat pilkulla erotettuna (myös ennen/jälkeen raportin aikajaksoa)
    |
    |- Viimeisin tila: opiskeluoikeuden tila raportin aikajakson lopussa
    |- Rahoitukset: raportin aikajaksolla esiintyvät rahoitusmuodot pilkulla erotettuna
    |- Päättynyt: kertoo onko opiskeluoikeus päättynyt raportin aikajaksolla
    |- Päättymispäivä: mukana vain jos opiskeluoikeus on päättynyt raportin aikajaksolla
    |
    |- Osa-aikaisuusjaksot (prosentit): raportin aikajakson osa-aikaisuusprosentit pilkulla erotettuna
    |- Osa-aikaisuus keskimäärin (%): raportin aikajakson osa-aikaisuusprosenttien päivillä painotettu keskiarvo
    |- Oppisopimus (pv): opiskeluoikeuden jollain päätason suorituksella on oppisopimusjakso, joka mahtuu kokonaan tai osittain raportin aikajaksoon
    """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  def buildRow(alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow])): OpiskelijavuositiedotRow = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonSuoritukset) = data
    val osaamisalat = päätasonSuoritukset
      .flatMap(s => JsonSerializer.extract[Option[List[Osaamisalajakso]]](s.data \ "osaamisala"))
      .flatten
      .filterNot(j => j.alku.exists(_.isAfter(loppu)))
      .filterNot(j => j.loppu.exists(_.isBefore(alku)))
      .map(_.osaamisala.koodiarvo)
      .sorted
      .distinct
    // jätä turha päättävän jakson "-" pois rahoitusmuotolistasta
    val aikajaksotOpintojenRahoitukseen = if (aikajaksot.last.opiskeluoikeusPäättynyt && aikajaksot.last.opintojenRahoitus.isEmpty) aikajaksot.dropRight(1) else aikajaksot
    val opintojenRahoitukset = Some(distinctAdjacent(aikajaksotOpintojenRahoitukseen.map(_.opintojenRahoitus.getOrElse("-"))).mkString(",")).filter(_ != "-")
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    new OpiskelijavuositiedotRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      sisältyyOpiskeluoikeuteenOid = opiskeluoikeus.sisältyyOpiskeluoikeuteenOid,
      aikaleima = opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      toimipisteOidit = päätasonSuoritukset.map(_.toimipisteOid).sorted.distinct.mkString(","),
      oppijaOid = "*", // opiskeluoikeus.oppijaOid,
      hetu = Some("*"), // henkilö.flatMap(_.hetu),
      sukunimi = Some("*"), // henkilö.map(_.sukunimi),
      etunimet = Some("*"), // henkilö.map(_.etunimet),
      koulutusmoduulit = päätasonSuoritukset.map(_.koulutusmoduuliKoodiarvo).sorted.mkString(","),
      osaamisalat = if (osaamisalat.isEmpty) None else Some(osaamisalat.mkString(",")),
      viimeisinOpiskeluoikeudenTila = aikajaksot.last.tila,
      opintojenRahoitukset = opintojenRahoitukset,
      opiskeluoikeusPäättynyt = aikajaksot.last.opiskeluoikeusPäättynyt,
      päättymispäivä = aikajaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku.toLocalDate), // toimii koska päättävä jakso on aina yhden päivän mittainen, jolloin truncateToDates ei muuta sen alkupäivää
      läsnäPäivät = aikajaksoPäivät(aikajaksot, a => if (a.tila == "lasna") 1 else 0),
      lomaPäivät = aikajaksoPäivät(aikajaksot, a => if (a.tila == "loma") 1 else 0),
      majoitusPäivät = aikajaksoPäivät(aikajaksot, _.majoitus),
      sisäoppilaitosmainenMajoitusPäivät = aikajaksoPäivät(aikajaksot, _.sisäoppilaitosmainenMajoitus),
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitusPäivät = aikajaksoPäivät(aikajaksot, _.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus),
      erityinenTukiPäivät = aikajaksoPäivät(aikajaksot, _.erityinenTuki),
      vaativanErityisenTuenErityinenTehtäväPäivät = aikajaksoPäivät(aikajaksot, _.vaativanErityisenTuenErityinenTehtävä),
      hojksPäivät = aikajaksoPäivät(aikajaksot, _.hojks),
      vaikeastiVammainenPäivät = aikajaksoPäivät(aikajaksot, _.vaikeastiVammainen),
      vammainenJaAvustajaPäivät = aikajaksoPäivät(aikajaksot, _.vammainenJaAvustaja),
      osaAikaisuusProsentit = Some(distinctAdjacent(aikajaksot.map(_.osaAikaisuus)).mkString(",")).filter(_ != "100"),
      osaAikaisuusKeskimäärin = aikajaksoPäivät(aikajaksot, _.osaAikaisuus).toFloat / aikajaksoPäivät(aikajaksot, _ => 1),
      opiskeluvalmiuksiaTukevatOpinnotPäivät = aikajaksoPäivät(aikajaksot, _.opiskeluvalmiuksiaTukevatOpinnot),
      vankilaopetuksessaPäivät = aikajaksoPäivät(aikajaksot, _.vankilaopetuksessa),
      oppisopimusJossainPäätasonSuorituksessaPäivät = aikajaksoPäivät(aikajaksot, _.oppisopimusJossainPäätasonSuorituksessa),
      lisätiedotHenkilöstökoulutus = opiskeluoikeus.lisätiedotHenkilöstökoulutus,
      lisätiedotKoulutusvienti = opiskeluoikeus.lisätiedotKoulutusvienti
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

  private def aikajaksoPäivät(aikajaksot: Seq[ROpiskeluoikeusAikajaksoRow], f: ROpiskeluoikeusAikajaksoRow => Byte): Int =
    aikajaksot.map(j => f(j) * j.lengthInDays).sum
}
