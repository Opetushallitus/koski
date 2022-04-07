package fi.oph.koski.raportit

import java.sql.Date
import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{Koodistokoodiviite, _}
import org.json4s.JValue
import fi.oph.koski.log.Logging
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

object PerusopetuksenVuosiluokkaRaportti extends VuosiluokkaRaporttiPaivalta with Logging {

  def buildRaportti(
    repository: PerusopetuksenRaportitRepository,
    oppilaitosOids: Seq[Oid],
    paiva: LocalDate,
    vuosiluokka: String,
    t: LocalizationReader
  ): Seq[PerusopetusRow] = {
    val rows = if (vuosiluokka == "9") {
      repository.peruskoulunPaattavatJaLuokalleJääneet(oppilaitosOids, paiva, vuosiluokka, t)
    } else {
      repository.perusopetuksenvuosiluokka(oppilaitosOids, paiva, vuosiluokka, t)
    }
    rows.map(buildRow(_, paiva, t))
  }

  private def buildRow(row: PerusopetuksenRaporttiRows, hakupaiva: LocalDate, t: LocalizationReader) = {
    val opiskeluoikeudenLisätiedot = JsonSerializer.extract[Option[PerusopetuksenOpiskeluoikeudenLisätiedot]](row.opiskeluoikeus.data \ "lisätiedot")
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")
    val (toimintaalueOsasuoritukset, muutOsasuoritukset) = row.osasuoritukset.partition(_.suorituksenTyyppi == "perusopetuksentoimintaalue")
    val (valtakunnalliset, paikalliset) = muutOsasuoritukset.partition(isValtakunnallinenOppiaine)
    val (pakollisetValtakunnalliset, valinnaisetValtakunnalliset) = valtakunnalliset.partition(isPakollinen)
    val (pakollisetPaikalliset, valinnaisetPaikalliset) = paikalliset.partition(isPakollinen)
    val kaikkiValinnaiset = valinnaisetPaikalliset.union(valinnaisetValtakunnalliset)
    val voimassaOlevatErityisenTuenPäätökset = opiskeluoikeudenLisätiedot.map(lt => combineErityisenTuenPäätökset(lt.erityisenTuenPäätös, lt.erityisenTuenPäätökset).filter(erityisentuenPäätösvoimassaPaivalla(_, hakupaiva))).getOrElse(List.empty)
    val päätasonVahvistusPäivä = row.päätasonSuoritus.vahvistusPäivä

    PerusopetusRow(
      opiskeluoikeusOid = row.opiskeluoikeus.opiskeluoikeusOid,
      oppilaitoksenNimi = if(t.language == "sv") row.opiskeluoikeus.oppilaitosNimiSv else row.opiskeluoikeus.oppilaitosNimi,
      oppilaitosRaportointipäivänä = oppilaitosRaportointipäivänä(row),
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      yksiloity = row.henkilo.yksiloity,
      oppijaOid = row.opiskeluoikeus.oppijaOid,
      hetu = row.henkilo.hetu,
      sukunimi = row.henkilo.sukunimi,
      etunimet = row.henkilo.etunimet,
      sukupuoli = row.henkilo.sukupuoli,
      kotikunta = if(t.language == "sv") row.henkilo.kotikuntaNimiSv else row.henkilo.kotikuntaNimiFi,
      opiskeluoikeudenAlkamispäivä = row.opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinTila = row.opiskeluoikeus.viimeisinTila.getOrElse(""),
      tilaHakupaivalla = row.aikajaksot.last.tila,
      suorituksenTila = if (row.päätasonSuoritus.vahvistusPäivä.isDefined) t.get("raportti-excel-default-value-valmis") else t.get("raportti-excel-default-value-kesken"),
      suorituksenAlkamispaiva = JsonSerializer.extract[Option[LocalDate]](row.päätasonSuoritus.data \ "alkamispäivä").getOrElse("").toString,
      suorituksenVahvistuspaiva = row.päätasonSuoritus.vahvistusPäivä.getOrElse("").toString,
      jaaLuokalle = JsonSerializer.extract[Option[Boolean]](row.päätasonSuoritus.data \ "jääLuokalle").getOrElse(false),
      luokka = row.luokka,
      voimassaolevatVuosiluokat = row.voimassaolevatVuosiluokat.mkString(","),
      aidinkieli = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "AI")(pakollisetValtakunnalliset),
      pakollisenAidinkielenOppimaara = getOppiaineenOppimäärä("AI", t)(pakollisetValtakunnalliset),
      kieliA1 = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "A1")(pakollisetValtakunnalliset),
      kieliA1Oppimaara = getOppiaineenOppimäärä("A1", t)(pakollisetValtakunnalliset),
      kieliA2 = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "A2")(pakollisetValtakunnalliset),
      kieliA2Oppimaara = getOppiaineenOppimäärä("A2", t)(pakollisetValtakunnalliset),
      kieliB = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "B1")(pakollisetValtakunnalliset),
      kieliBOppimaara = getOppiaineenOppimäärä("B1", t)(pakollisetValtakunnalliset),
      uskonto = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "KT")(pakollisetValtakunnalliset),
      elamankatsomustieto = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "ET")(pakollisetValtakunnalliset),
      uskonnonOppimaara = uskonnonOppimääräIfNotElämänkatsomustieto(pakollisetValtakunnalliset, t),
      historia = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "HI")(pakollisetValtakunnalliset),
      yhteiskuntaoppi = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "YH")(pakollisetValtakunnalliset),
      matematiikka = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "MA")(pakollisetValtakunnalliset),
      kemia = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "KE")(pakollisetValtakunnalliset),
      fysiikka = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "FY")(pakollisetValtakunnalliset),
      biologia = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "BI")(pakollisetValtakunnalliset),
      maantieto = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "GE")(pakollisetValtakunnalliset),
      musiikki = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "MU")(pakollisetValtakunnalliset),
      kuvataide = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "KU")(pakollisetValtakunnalliset),
      kotitalous = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "KO")(pakollisetValtakunnalliset),
      terveystieto = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "TE")(pakollisetValtakunnalliset),
      kasityo = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "KS")(pakollisetValtakunnalliset),
      liikunta = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "LI")(pakollisetValtakunnalliset),
      ymparistooppi = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "YL")(pakollisetValtakunnalliset),
      opintoohjaus = oppiaineenArvosanaTiedot(päätasonVahvistusPäivä, t, "OP")(pakollisetValtakunnalliset),
      kayttaymisenArvio = JsonSerializer.extract[Option[PerusopetuksenKäyttäytymisenArviointi]](row.päätasonSuoritus.data \ "käyttäytymisenArvio").map(_.arvosana.koodiarvo).getOrElse(""),
      paikallistenOppiaineidenKoodit = paikalliset.map(_.koulutusmoduuliKoodiarvo).mkString(","),
      pakollisetPaikalliset = pakollisetPaikalliset.map(r => nimiJaKoodi(r, t)).mkString(","),
      valinnaisetPaikalliset = valinnaisetPaikalliset.map(r => nimiJaKoodiJaArvosana(r, t)).mkString(","),
      valinnaisetValtakunnalliset = valinnaisetValtakunnalliset.map(r => nimiJaKoodiJaArvosana(r, t)).mkString(","),
      valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ >= _, 2)).map(r => nimiJaKoodiJaLaajuusJaArvosana(r, t)).mkString(","),
      valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ < _, 2)).map(r => nimiJaKoodiJaLaajuusJaArvosana(r, t)).mkString(","),
      numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(os => vuosiviikkotunteja(os, _ < _, 2) && isNumeroarviollinen(os)).map(r => nimiJaKoodiJaLaajuusJaArvosana(r, t)).mkString(","),
      valinnaisetEiLaajuutta = kaikkiValinnaiset.filter(_.koulutusmoduuliLaajuusArvo.isEmpty).map(r => nimiJaKoodi(r, t)).mkString(","),
      vahvistetutToimintaAlueidenSuoritukset = toimintaalueOsasuoritukset.filter(_.arviointiHyväksytty.getOrElse(false)).sortBy(_.koulutusmoduuliKoodiarvo).map(r => nimiJaKoodi(r, t)).mkString(","),
      majoitusetu = opiskeluoikeudenLisätiedot.exists(_.majoitusetu.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      kuljetusetu = opiskeluoikeudenLisätiedot.exists(_.kuljetusetu.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      kotiopetus = opiskeluoikeudenLisätiedot.exists(oo => oneOfAikajaksoistaVoimassaHakuPaivalla(oo.kotiopetus, oo.kotiopetusjaksot, hakupaiva)),
      ulkomailla = opiskeluoikeudenLisätiedot.exists(oo => oneOfAikajaksoistaVoimassaHakuPaivalla(oo.ulkomailla, oo.ulkomaanjaksot, hakupaiva)),
      perusopetuksenAloittamistaLykatty = opiskeluoikeudenLisätiedot.exists(_.perusopetuksenAloittamistaLykätty.getOrElse(false)),
      aloittanutEnnenOppivelvollisuutta = opiskeluoikeudenLisätiedot.exists(_.aloittanutEnnenOppivelvollisuutta),
      pidennettyOppivelvollisuus = opiskeluoikeudenLisätiedot.exists(_.pidennettyOppivelvollisuus.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      joustavaPerusopetus = opiskeluoikeudenLisätiedot.exists(_.joustavaPerusopetus.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      vuosiluokkiinSitoutumatonOpetus = opiskeluoikeudenLisätiedot.exists(_.vuosiluokkiinSitoutumatonOpetus),
      vammainen = opiskeluoikeudenLisätiedot.exists(_.vammainen.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      vaikeastiVammainen = opiskeluoikeudenLisätiedot.exists(_.vaikeastiVammainen.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      sisäoppilaitosmainenMajoitus = opiskeluoikeudenLisätiedot.exists(_.sisäoppilaitosmainenMajoitus.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      koulukoti = opiskeluoikeudenLisätiedot.exists(_.koulukoti.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      erityisenTuenPaatosVoimassa = voimassaOlevatErityisenTuenPäätökset.size > 0,
      erityisenTuenPaatosToimialueittain = voimassaOlevatErityisenTuenPäätökset.exists(_.opiskeleeToimintaAlueittain),
    )
  }

  private def oppilaitosRaportointipäivänä(data: PerusopetuksenRaporttiRows): Option[String] = {
    val organisaatiohistoriaRaportointipäivältä = data.organisaatiohistoriaResult.head
    val oppilaitosOidNyt = data.opiskeluoikeus.oppilaitosOid
    if (organisaatiohistoriaRaportointipäivältä.oppilaitosOid == oppilaitosOidNyt) {
      None
    } else {
      Some(organisaatiohistoriaRaportointipäivältä.toString)
    }
  }

  private val yleissivistäväkoodisto = Seq(
    "A1", "A2", "AI", "B1", "B2", "B3", "BI", "ET", "FI", "FY", "GE", "HI", "KE", "KO", "KS", "KT", "KU", "LI", "MA", "MU", "OP", "OPA", "PS", "TE", "YH", "YL"
  )

  private def isValtakunnallinenOppiaine(osasuoritus: ROsasuoritusRow) = {
    yleissivistäväkoodisto.contains(osasuoritus.koulutusmoduuliKoodiarvo) &&
      osasuoritus.koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava")
  }

  private def isPakollinen(osasuoritus: ROsasuoritusRow) = {
    JsonSerializer.extract[Option[Boolean]](osasuoritus.data \ "koulutusmoduuli" \ "pakollinen").getOrElse(false)
  }

  private def oppiaineenArvosanaTiedot(
    päätasonVahvistusPäivä: Option[Date],
    t: LocalizationReader,
    koodistoKoodit: String*
  )(oppiaineidenSuoritukset: Seq[ROsasuoritusRow]): String = {
    oppiaineidenSuoritukset.filter(s => koodistoKoodit.contains(s.koulutusmoduuliKoodiarvo)) match {
      case Nil => t.get("raportti-excel-default-value-oppiaine-puuttuu")
      case suoritukset@_ => suoritukset.map(
        oppiaineenArvosanaJaYksilöllistettyTieto(_, päätasonVahvistusPäivä, t)
      ).mkString(",")
    }
  }

  private def oppiaineenArvosanaJaYksilöllistettyTieto(
    osasuoritus: ROsasuoritusRow,
    päätasonVahvistusPäivä: Option[Date],
    t: LocalizationReader
  ): String = {
    val arvosana = osasuoritus.arviointiArvosanaKoodiarvo
      .getOrElse(t.get("raportti-excel-default-value-arvosana-puuttuu"))
    val viimeinenPäiväIlmanLaajuuksia = Date.valueOf(LocalDate.of(2020, 7, 31))
    if (päätasonVahvistusPäivä.exists(_.after(viimeinenPäiväIlmanLaajuuksia)) && osasuoritus.koulutusmoduuliPakollinen.getOrElse(false)) {
      val laajuus = osasuoritus.koulutusmoduuliLaajuusArvo.getOrElse(t.get("raportti-excel-default-value-laajuus-puuttuu"))
      s"$arvosana${täppäIfYksilöllistetty(osasuoritus)} ${t.get("raportti-excel-default-value-laajuus")}: $laajuus"
    } else {
      s"$arvosana${täppäIfYksilöllistetty(osasuoritus)}"
    }
  }

  private def täppäIfYksilöllistetty(osasuoritus: ROsasuoritusRow): String = {
    val isYksilöllistetty = JsonSerializer.extract[Option[Boolean]](osasuoritus.data \ "yksilöllistettyOppimäärä").getOrElse(false)
    if (isYksilöllistetty) "*" else ""
  }

  private def uskonnonOppimääräIfNotElämänkatsomustieto(osasuoritukset: Seq[ROsasuoritusRow], t: LocalizationReader): String = {
    val hasElämänkatsomustieto = osasuoritukset.exists(_.koulutusmoduuliKoodiarvo == "ET")
    if (hasElämänkatsomustieto) "" else uskonnonOppimäärä(osasuoritukset, t)
  }

  private def uskonnonOppimäärä(osasuoritukset: Seq[ROsasuoritusRow], t: LocalizationReader): String = {
    osasuoritukset
      .find(_.koulutusmoduuliKoodiarvo == "KT")
      .map { uskonto =>
        JsonSerializer.extract[Option[Koodistokoodiviite]](uskonto.data \ "koulutusmoduuli" \ "uskonnonOppimäärä")
          .flatMap(_.nimi.map(_.get(t.language)))
          .getOrElse(t.get("raportti-excel-default-value-oppimäärä-puuttuu"))
      }
      .getOrElse(t.get("raportti-excel-default-value-oppiaine-puuttuu"))
  }
  
  private def getOppiaineenOppimäärä(koodistoKoodi: String, t: LocalizationReader)(osasuoritukset: Seq[ROsasuoritusRow]): String = {
    osasuoritukset.filter(_.koulutusmoduuliKoodiarvo == koodistoKoodi) match {
      case Nil => t.get("raportti-excel-default-value-oppiaine-puuttuu")
      case found@_ => found.map(f => getOppiaineenNimi(f, t)).mkString(",")
    }
  }

  private def getOppiaineenNimi(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String = {
    val muuAine = getLokalisoituNimi(osasuoritus.data \ "koulutusmoduuli" \ "tunniste" \ "nimi", t)
    val kieliAine = getLokalisoituNimi(osasuoritus.data \ "koulutusmoduuli" \ "kieli" \ "nimi", t)
    val result = (kieliAine, muuAine) match {
      case (Some(_), _) => kieliAine
      case (_, Some(_)) => muuAine
      case _ => None
    }
    result.getOrElse(t.get("raportti-excel-default-value-oppiaine-puuttuu"))
  }

  private def nimiJaKoodiJaLaajuusJaArvosana(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String = {
    nimiJaKoodi(osasuoritus, t) + " " +
      osasuoritus.koulutusmoduuliLaajuusArvo.getOrElse(t.get("raportti-excel-default-value-laajuus-puuttuu")) + " " +
      osasuoritus.arviointiArvosanaKoodiarvo.getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))
  }

  private def nimiJaKoodi(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String = {
    s"${getOppiaineenNimi(osasuoritus, t)} (${osasuoritus.koulutusmoduuliKoodiarvo})"
  }

  private def nimiJaKoodiJaArvosana(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String = {
    s"${getOppiaineenNimi(osasuoritus, t)} (${osasuoritus.koulutusmoduuliKoodiarvo}) ${osasuoritus.arviointiArvosanaKoodiarvo.getOrElse(t.get("raportti-excel-default-value-ei-arvosanaa"))}"
  }

  private def getLokalisoituNimi(j: JValue, t: LocalizationReader): Option[String] = {
    JsonSerializer.extract[Option[LocalizedString]](j).map(_.get(t.language))
  }

  private val vuosiviikkotunnitKoodistoarvo = "3"

  private def vuosiviikkotunteja(osasuoritus: ROsasuoritusRow, op: (Double, Double) => Boolean, threshold: Double): Boolean = {
    osasuoritus.koulutusmoduuliLaajuusYksikkö.contains(vuosiviikkotunnitKoodistoarvo) &&
      op(osasuoritus.koulutusmoduuliLaajuusArvo.getOrElse(-1d), threshold)
  }

  private def isNumeroarviollinen(osasuoritus: ROsasuoritusRow) = {
    osasuoritus.arviointiArvosanaKoodiarvo.exists(_.matches("\\d+"))
  }

  private def oneOfAikajaksoistaVoimassaHakuPaivalla(aikajakso: Option[Jakso], aikajaksot: Option[List[Jakso]], hakupaiva: LocalDate): Boolean = {
    aikajakso.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)) ||
      aikajaksot.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))
  }

  private def aikajaksoVoimassaHakuPaivalla(aikajakso: Jakso, paiva: LocalDate): Boolean = {
    (aikajakso.alku, aikajakso.loppu) match {
      case (alku, Some(loppu)) => !alku.isAfter(paiva) && !loppu.isBefore(paiva)
      case (alku, _) => !alku.isAfter(paiva)
    }
  }

  private def combineErityisenTuenPäätökset(
    erityisenTuenPäätös: Option[ErityisenTuenPäätös],
    erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]]
  ): List[ErityisenTuenPäätös] = {
    erityisenTuenPäätös.toList ++ erityisenTuenPäätökset.toList.flatten
  }

  private def erityisentuenPäätösvoimassaPaivalla(päätös: ErityisenTuenPäätös, paiva: LocalDate): Boolean = {
    (päätös.alku, päätös.loppu) match {
      case (Some(alku), Some(loppu)) => !alku.isAfter(paiva) && !loppu.isBefore(paiva)
      case (Some(alku), _) => !alku.isAfter(paiva)
      case _ => false
    }
  }

  def title(etuliite: String, oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String =
    s"$etuliite $vuosiluokka $oppilaitosOid ${finnishDateFormat.format(paiva)}"

  def documentation(t: LocalizationReader): String = t.get("raportti-excel-perusopetus-dokumentaatio")

  def filename(etuliite: String, oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String = {
    s"${etuliite}_${oppilaitosOid}_${vuosiluokka}_${paiva}.xlsx"
  }

  private def compactLisätiedotColumn(title: String, t: LocalizationReader) = CompactColumn(title, comment = Some(t.get("raportti-excel-kolumni-compactLisätiedotColumn-comment")))

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "oppilaitoksenNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi"), comment = Some(t.get("raportti-excel-kolumni-oppilaitoksenNimi-comment"))),
    "oppilaitosRaportointipäivänä" -> Column(t.get("raportti-excel-kolumni-oppilaitosRaportointipäivänä"), comment = Some(t.get("raportti-excel-kolumni-oppilaitosRaportointipäivänä-comment"))),
    "lähdejärjestelmä" -> Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    "lähdejärjestelmänId" -> CompactColumn(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    "yksiloity" -> Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "sukupuoli" -> Column(t.get("raportti-excel-kolumni-sukupuoli"), comment = Some(t.get("raportti-excel-kolumni-sukupuoli-comment"))),
    "kotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta"), comment = Some(t.get("raportti-excel-kolumni-kotikunta-comment"))),
    "opiskeluoikeudenAlkamispäivä" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    "viimeisinTila" -> CompactColumn(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    "tilaHakupaivalla" -> CompactColumn(t.get("raportti-excel-kolumni-tilaHakupaivalla"), comment = Some(t.get("raportti-excel-kolumni-tilaHakupaivalla-comment"))),
    "suorituksenTila" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila")),
    "suorituksenAlkamispaiva" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenAlkamispaiva")),
    "suorituksenVahvistuspaiva" -> CompactColumn(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
    "jaaLuokalle" -> CompactColumn(t.get("raportti-excel-kolumni-jaaLuokalle")),
    "luokka" -> CompactColumn(t.get("raportti-excel-kolumni-luokka")),
    "voimassaolevatVuosiluokat" -> CompactColumn(t.get("raportti-excel-kolumni-voimassaolevatVuosiluokat"), comment = Some(t.get("raportti-excel-kolumni-voimassaolevatVuosiluokat-comment"))),
    "aidinkieli" -> CompactColumn(t.get("raportti-excel-kolumni-aidinkieli")),
    "pakollisenAidinkielenOppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisenAidinkielenOppimaara")),
    "kieliA1" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA1")),
    "kieliA1Oppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA1Oppimaara")),
    "kieliA2" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA2")),
    "kieliA2Oppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA2Oppimaara")),
    "kieliB" -> CompactColumn(t.get("raportti-excel-kolumni-kieliB")),
    "kieliBOppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliBOppimaara")),
    "uskonto" -> CompactColumn(t.get("raportti-excel-kolumni-uskonto")),
    "elamankatsomustieto" -> CompactColumn(t.get("raportti-excel-kolumni-elamankatsomustieto")),
    "uskonnonOppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-uskonnonOppimaara")),
    "historia" -> CompactColumn(t.get("raportti-excel-kolumni-historia")),
    "yhteiskuntaoppi" -> CompactColumn(t.get("raportti-excel-kolumni-yhteiskuntaoppi")),
    "matematiikka" -> CompactColumn(t.get("raportti-excel-kolumni-matematiikka")),
    "kemia" -> CompactColumn(t.get("raportti-excel-kolumni-kemia")),
    "fysiikka" -> CompactColumn(t.get("raportti-excel-kolumni-fysiikka")),
    "biologia" -> CompactColumn(t.get("raportti-excel-kolumni-biologia")),
    "maantieto" -> CompactColumn(t.get("raportti-excel-kolumni-maantieto")),
    "musiikki" -> CompactColumn(t.get("raportti-excel-kolumni-musiikki")),
    "kuvataide" -> CompactColumn(t.get("raportti-excel-kolumni-kuvataide")),
    "kotitalous" -> CompactColumn(t.get("raportti-excel-kolumni-kotitalous")),
    "terveystieto" -> CompactColumn(t.get("raportti-excel-kolumni-terveystieto")),
    "kasityo" -> CompactColumn(t.get("raportti-excel-kolumni-kasityo")),
    "liikunta" -> CompactColumn(t.get("raportti-excel-kolumni-liikunta")),
    "ymparistooppi" -> CompactColumn(t.get("raportti-excel-kolumni-ymparistooppi")),
    "opintoohjaus" -> CompactColumn(t.get("raportti-excel-kolumni-opintoohjaus")),
    "kayttaymisenArvio" -> CompactColumn(t.get("raportti-excel-kolumni-kayttaymisenArvio")),
    "paikallistenOppiaineidenKoodit" -> CompactColumn(t.get("raportti-excel-kolumni-paikallistenOppiaineidenKoodit")),
    "pakollisetPaikalliset" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisetPaikalliset"), comment = Some(t.get("raportti-excel-kolumni-pakollisetPaikalliset-comment"))),
    "valinnaisetPaikalliset" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetPaikalliset")),
    "valinnaisetValtakunnalliset" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetValtakunnalliset")),
    "valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia")),
    "valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia")),
    "numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> CompactColumn(
      title = t.get("raportti-excel-kolumni-numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia"),
      comment = Some(t.get("raportti-excel-kolumni-numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia-comment"))),
    "valinnaisetEiLaajuutta" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetEiLaajuutta")),
    "vahvistetutToimintaAlueidenSuoritukset" -> CompactColumn(t.get("raportti-excel-kolumni-vahvistetutToimintaAlueidenSuoritukset"), comment = Some(t.get("raportti-excel-kolumni-vahvistetutToimintaAlueidenSuoritukset-comment"))),
    "majoitusetu" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-majoitusetu"), t),
    "kuljetusetu" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-kuljetusetu"), t),
    "kotiopetus" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-kotiopetus"), t),
    "ulkomailla" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-ulkomailla"), t),
    "perusopetuksenAloittamistaLykatty" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-perusopetuksenAloittamistaLykatty"), t),
    "aloittanutEnnenOppivelvollisuutta" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-aloittanutEnnenOppivelvollisuutta"), t),
    "pidennettyOppivelvollisuus" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-pidennettyOppivelvollisuus"), t),
    "joustavaPerusopetus" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-joustavaPerusopetus"), t),
    "vuosiluokkiinSitoutumatonOpetus" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-vuosiluokkiinSitoutumatonOpetus"), t),
    "vammainen" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-vammainen"), t),
    "vaikeastiVammainen" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-vaikeastiVammainen"), t),
    "sisäoppilaitosmainenMajoitus" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), t),
    "koulukoti" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-koulukoti"), t),
    "erityisenTuenPaatosVoimassa" -> CompactColumn(t.get("raportti-excel-kolumni-erityisenTuenPaatosVoimassa"), comment = Some(t.get("raportti-excel-kolumni-erityisenTuenPaatosVoimassa-comment"))),
    "erityisenTuenPaatosToimialueittain" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-erityisenTuenPaatosToimialueittain"), t)
  )
}

private[raportit] case class PerusopetusRow(
  opiskeluoikeusOid: String,
  oppilaitoksenNimi: String,
  oppilaitosRaportointipäivänä: Option[String],
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  sukupuoli: Option[String],
  kotikunta: Option[String],
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinTila: String,
  tilaHakupaivalla: String,
  suorituksenTila: String,
  suorituksenAlkamispaiva: String,
  suorituksenVahvistuspaiva: String,
  jaaLuokalle: Boolean,
  luokka: Option[String],
  voimassaolevatVuosiluokat: String,
  aidinkieli: String,
  pakollisenAidinkielenOppimaara: String,
  kieliA1: String,
  kieliA1Oppimaara: String,
  kieliA2: String,
  kieliA2Oppimaara: String,
  kieliB: String,
  kieliBOppimaara: String,
  uskonto: String,
  elamankatsomustieto: String,
  uskonnonOppimaara: String,
  historia: String,
  yhteiskuntaoppi: String,
  matematiikka: String,
  kemia: String,
  fysiikka: String,
  biologia: String,
  maantieto: String,
  musiikki: String,
  kuvataide: String,
  kotitalous: String,
  terveystieto: String,
  kasityo: String,
  liikunta: String,
  ymparistooppi: String,
  opintoohjaus: String,
  kayttaymisenArvio: String,
  paikallistenOppiaineidenKoodit: String,
  pakollisetPaikalliset: String,
  valinnaisetPaikalliset: String,
  valinnaisetValtakunnalliset: String,
  valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia: String,
  valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia: String,
  numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia: String,
  valinnaisetEiLaajuutta: String,
  vahvistetutToimintaAlueidenSuoritukset: String,
  majoitusetu: Boolean,
  kuljetusetu: Boolean,
  kotiopetus: Boolean,
  ulkomailla: Boolean,
  perusopetuksenAloittamistaLykatty: Boolean,
  aloittanutEnnenOppivelvollisuutta: Boolean,
  pidennettyOppivelvollisuus: Boolean,
  joustavaPerusopetus: Boolean,
  vuosiluokkiinSitoutumatonOpetus: Boolean,
  vammainen: Boolean,
  vaikeastiVammainen: Boolean,
  sisäoppilaitosmainenMajoitus: Boolean,
  koulukoti: Boolean,
  erityisenTuenPaatosVoimassa: Boolean,
  erityisenTuenPaatosToimialueittain: Boolean
)
