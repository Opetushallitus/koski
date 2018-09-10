package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeParseException

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{LähdejärjestelmäId, Organisaatio, Osaamisalajakso}
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.FinnishDateFormat.{finnishDateFormat, finnishDateTimeFormat}
import org.scalatra.ContentEncodingSupport

class RaportitServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {

  private lazy val raportointiDatabase = application.raportointiDatabase

  get("/oppijavuosiraportti") {

    val loadCompleted = raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses)
    if (loadCompleted.isEmpty) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }

    if (!koskiSession.hasRaportitAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
    }

    val oppilaitosOid = OrganisaatioOid.validateOrganisaatioOid(getStringParam("oppilaitosOid")) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) if !koskiSession.hasReadAccess(oid) => haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
      case Right(oid) => oid
    }
    val (alku, loppu) = try {
      (LocalDate.parse(getStringParam("alku")), LocalDate.parse(getStringParam("loppu")))
    } catch {
      case e: DateTimeParseException => haltWithStatus(KoskiErrorCategory.badRequest.format.pvm())
    }
    if (loppu.isBefore(alku)) {
      haltWithStatus(KoskiErrorCategory.badRequest.format.pvm("loppu ennen alkua"))
    }

    // temporary restriction
    if (application.config.getStringList("oppijavuosiraportti.enabledForUsers").indexOf(koskiSession.username) < 0) {
      haltWithStatus(KoskiErrorCategory.forbidden("Ei sallittu tälle käyttäjälle"))
    }

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=oppijavuosiraportti&oppilaitosOid=$oppilaitosOid&alku=$alku&loppu=$loppu")))

    val rows = Oppijavuosiraportti.buildOppijavuosiraportti(raportointiDatabase, oppilaitosOid, alku, loppu)

    if (Environment.isLocalDevelopmentEnvironment && params.contains("text")) {
      contentType = "text/plain"
      response.writer.print(rows.map(_.toString).mkString("\n\n"))
    } else {
      contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      response.setHeader("Content-Disposition", s"""attachment; filename="oppijavuosiraportti_${oppilaitosOid}_$alku-$loppu.xlsx""")
      ExcelWriter.writeExcel(
        WorkbookSettings(s"Oppijavuosiraportti $oppilaitosOid $alku - $loppu"),
        Seq(
          DataSheet("Opiskeluoikeudet", rows, OppijavuosiraporttiRow.columnSettings),
          DocumentationSheet("Ohjeet", OppijavuosiraporttiRow.documentation(oppilaitosOid, alku, loppu, loadCompleted.get))
        ),
        response.getOutputStream
      )
    }
  }

}

case class OppijavuosiraporttiRow(
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
  koulutusmuoto: String,
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

object OppijavuosiraporttiRow {

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
    "koulutusmuoto" -> Column("Koulutusmuoto"),
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

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp) =
    s"""
      |Ammatilliset opiskeluoikeudet
      |Oppilaitos: $oppilaitosOid
      |Aikaväli: ${finnishDateFormat.format(alku)} - ${finnishDateFormat.format(loppu)}
      |Raportti luotu: ${finnishDateTimeFormat.format(LocalDateTime.now)} (${finnishDateTimeFormat.format(loadCompleted.toLocalDateTime)} tietojen pohjalta)
      |
      |Tarkempia ohjeita taulukon sisällöstä:
      |
      |- Tutkinnot: kaikki opiskeluoikeudella olevat päätason suoritusten tutkinnot pilkulla erotettuna (myös ennen raportin aikaväliä valmistuneet, ja raportin aikavälin jälkeen alkaneet)
      |- Osaamisalat: kaikkien ym. tutkintojen osaamisalat pilkulla erotettuna (myös ennen/jälkeen raportin aikaväliä)
      |
      |- Viimeisin tila: opiskeluoikeuden tila raportin aikavälin lopussa
      |- Rahoitukset: raportin aikavälillä esiintyvät rahoitusmuodot pilkulla erotettuna
      |- Päättynyt: kertoo onko opiskeluoikeus päättynyt raportin aikavälillä
      |- Päättymispäivä: mukana vain jos opiskeluoikeus on päättynyt raportin aikavälillä
      |
      |- Osa-aikaisuusjaksot (prosentit): raportin aikavälin osa-aikaisuusprosentit pilkulla erotettuna
      |- Osa-aikaisuus keskimäärin (%): raportin aikavälin osa-aikaisuusprosenttien päivillä painotettu keskiarvo
      |- Oppisopimus (pv): opiskeluoikeuden jollain päätason suorituksella on oppisopimusjakso, joka mahtuu kokonaan tai osittain raportin aikaväliin
    """.stripMargin.trim.stripPrefix("\n").stripSuffix("\n")

  def build(alku: LocalDate, loppu: LocalDate, data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow])): OppijavuosiraporttiRow = {
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
    new OppijavuosiraporttiRow(
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
      koulutusmuoto = opiskeluoikeus.koulutusmuoto,
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

object Oppijavuosiraportti {
  private[raportit] def buildOppijavuosiraportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Organisaatio.Oid, alku: LocalDate, loppu: LocalDate): Seq[OppijavuosiraporttiRow] = {
    val result = raportointiDatabase.opiskeluoikeusAikajaksot(oppilaitosOid, alku, loppu)
    val rows = result.map(r => OppijavuosiraporttiRow.build(alku, loppu, r))
    rows
  }
}
