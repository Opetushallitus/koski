package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema._
import fi.oph.koski.schema.Organisaatio.Oid
import org.json4s.JValue

object PerusopetuksenVuosiluokka extends VuosiluokkaRaporttiPaivalta {

  def buildRaportti(repository: PerusopetuksenRaportitRepository, oppilaitosOid: Oid, paiva: LocalDate, vuosiluokka: String): Seq[PerusopetusRow] = {
    val rows = if (vuosiluokka == "10") {
      repository.peruskoulunPaattavatJaLuokalleJääneet(oppilaitosOid, paiva, vuosiluokka)
    } else {
      repository.perusopetuksenvuosiluokka(oppilaitosOid, paiva, vuosiluokka)
    }
    rows.map(buildRow(_, paiva))
  }

  private def buildRow(data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], RPäätasonSuoritusRow, Seq[ROsasuoritusRow], Seq[String]), hakupaiva: LocalDate) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonsuoritus, osasuoritukset, voimassaolevatVuosiluokat) = data

    val opiskeluoikeudenLisätiedot = JsonSerializer.extract[Option[PerusopetuksenOpiskeluoikeudenLisätiedot]](opiskeluoikeus.data \ "lisätiedot")
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val (toimintaalueOsasuoritukset, muutOsasuoritukset) = osasuoritukset.partition(_.suorituksenTyyppi == "perusopetuksentoimintaalue")
    val (valtakunnalliset, paikalliset) = muutOsasuoritukset.partition(isValtakunnallinenOppiaine)
    val (pakollisetValtakunnalliset, valinnaisetValtakunnalliset) = valtakunnalliset.partition(isPakollinen)
    val (pakollisetPaikalliset, valinnaisetPaikalliset) = paikalliset.partition(isPakollinen)
    val kaikkiValinnaiset = valinnaisetPaikalliset.union(valinnaisetValtakunnalliset)
    val voimassaOlevatErityisenTuenPäätökset = opiskeluoikeudenLisätiedot.map(lt => combineErityisenTuenPäätökset(lt.erityisenTuenPäätös, lt.erityisenTuenPäätökset).filter(erityisentuenPäätösvoimassaPaivalla(_, hakupaiva))).getOrElse(List.empty)

    PerusopetusRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      oppilaitoksenNimi = opiskeluoikeus.oppilaitosNimi,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.flatMap(_.hetu),
      sukunimi = henkilö.map(_.sukunimi),
      etunimet = henkilö.map(_.etunimet),
      sukupuoli = henkilö.flatMap(_.sukupuoli),
      viimeisinTila = opiskeluoikeus.viimeisinTila.getOrElse(""),
      tilaHakupaivalla = aikajaksot.last.tila,
      suorituksenTila = if (päätasonsuoritus.vahvistusPäivä.isDefined) "valmis" else "kesken",
      suorituksenAlkamispaiva = JsonSerializer.extract[Option[LocalDate]](päätasonsuoritus.data \ "alkamispäivä").getOrElse("").toString,
      suorituksenVahvistuspaiva = päätasonsuoritus.vahvistusPäivä.getOrElse("").toString,
      jaaLuokalle = JsonSerializer.extract[Option[Boolean]](päätasonsuoritus.data \ "jääLuokalle").getOrElse(false),
      luokka = JsonSerializer.extract[Option[String]](päätasonsuoritus.data \ "luokka").getOrElse(""),
      voimassaolevatVuosiluokat = voimassaolevatVuosiluokat.mkString(","),
      aidinkieli = oppiaineenArvosanaTiedot("AI")(pakollisetValtakunnalliset),
      pakollisenAidinkielenOppimaara = getOppiaineenOppimäärä("AI")(pakollisetValtakunnalliset),
      kieliA = oppiaineenArvosanaTiedot("A1")(pakollisetValtakunnalliset),
      kieliAOppimaara = getOppiaineenOppimäärä("A1")(pakollisetValtakunnalliset),
      kieliB = oppiaineenArvosanaTiedot("B1")(pakollisetValtakunnalliset),
      kieliBOppimaara = getOppiaineenOppimäärä("B1")(pakollisetValtakunnalliset),
      uskonto = oppiaineenArvosanaTiedot("KT", "ET")(pakollisetValtakunnalliset),
      uskonnonOppimaara = uskonnonOppimääräIfNotElämänkatsomustieto(pakollisetValtakunnalliset),
      historia = oppiaineenArvosanaTiedot("HI")(pakollisetValtakunnalliset),
      yhteiskuntaoppi = oppiaineenArvosanaTiedot("YH")(pakollisetValtakunnalliset),
      matematiikka = oppiaineenArvosanaTiedot("MA")(pakollisetValtakunnalliset),
      kemia = oppiaineenArvosanaTiedot("KE")(pakollisetValtakunnalliset),
      fysiikka = oppiaineenArvosanaTiedot("FY")(pakollisetValtakunnalliset),
      biologia = oppiaineenArvosanaTiedot("BI")(pakollisetValtakunnalliset),
      maantieto = oppiaineenArvosanaTiedot("GE")(pakollisetValtakunnalliset),
      musiikki = oppiaineenArvosanaTiedot("MU")(pakollisetValtakunnalliset),
      kuvataide = oppiaineenArvosanaTiedot("KU")(pakollisetValtakunnalliset),
      kotitalous = oppiaineenArvosanaTiedot("KO")(pakollisetValtakunnalliset),
      terveystieto = oppiaineenArvosanaTiedot("TE")(pakollisetValtakunnalliset),
      kasityo = oppiaineenArvosanaTiedot("KS")(pakollisetValtakunnalliset),
      liikunta = oppiaineenArvosanaTiedot("LI")(pakollisetValtakunnalliset),
      ymparistooppi = oppiaineenArvosanaTiedot("YL")(pakollisetValtakunnalliset),
      kayttaymisenArvio = JsonSerializer.extract[Option[PerusopetuksenKäyttäytymisenArviointi]](päätasonsuoritus.data \ "käyttäytymisenArvio").map(_.arvosana.koodiarvo).getOrElse(""),
      paikallistenOppiaineidenKoodit = paikalliset.map(_.koulutusmoduuliKoodiarvo).mkString(","),
      pakollisetPaikalliset = pakollisetPaikalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetPaikalliset = valinnaisetPaikalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetValtakunnalliset = valinnaisetValtakunnalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ >= _, 2)).map(nimiJaKoodiJaLaajuus).mkString(","),
      valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ < _, 2)).map(nimiJaKoodiJaLaajuus).mkString(","),
      numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(os => vuosiviikkotunteja(os, _ < _, 2) && isNumeroarviollinen(os)).map(nimiJaKoodiJaLaajuus).mkString(","),
      valinnaisetEiLaajuutta = kaikkiValinnaiset.filter(_.koulutusmoduuliLaajuusArvo.isEmpty).map(nimiJaKoodi).mkString(","),
      vahvistetutToimintaAlueidenSuoritukset = toimintaalueOsasuoritukset.filter(_.arviointiHyväksytty.getOrElse(false)).sortBy(_.koulutusmoduuliKoodiarvo).map(nimiJaKoodi).mkString(","),
      majoitusetu = opiskeluoikeudenLisätiedot.exists(_.majoitusetu.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      kuljetusetu = opiskeluoikeudenLisätiedot.exists(_.kuljetusetu.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      kotiopetus = opiskeluoikeudenLisätiedot.exists(oo => oneOfAikajaksoistaVoimassaHakuPaivalla(oo.kotiopetus, oo.kotiopetusjaksot, hakupaiva)),
      ulkomailla = opiskeluoikeudenLisätiedot.exists(oo => oneOfAikajaksoistaVoimassaHakuPaivalla(oo.ulkomailla, oo.ulkomaanjaksot, hakupaiva)),
      perusopetuksenAloittamistaLykatty = opiskeluoikeudenLisätiedot.exists(_.perusopetuksenAloittamistaLykätty),
      aloittanutEnnenOppivelvollisuutta = opiskeluoikeudenLisätiedot.exists(_.aloittanutEnnenOppivelvollisuutta),
      pidennettyOppivelvollisuus = opiskeluoikeudenLisätiedot.exists(_.pidennettyOppivelvollisuus.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      tehostetunTuenPaatos = opiskeluoikeudenLisätiedot.exists(oo => oneOfAikajaksoistaVoimassaHakuPaivalla(oo.tehostetunTuenPäätös, oo.tehostetunTuenPäätökset, hakupaiva)),
      joustavaPerusopetus = opiskeluoikeudenLisätiedot.exists(_.joustavaPerusopetus.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))),
      vuosiluokkiinSitoutumatonOpetus = opiskeluoikeudenLisätiedot.exists(_.vuosiluokkiinSitoutumatonOpetus),
      vammainen = opiskeluoikeudenLisätiedot.exists(_.vammainen.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      vaikeastiVammainen = opiskeluoikeudenLisätiedot.exists(_.vaikeastiVammainen.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      oikeusMaksuttomaanAsuntolapaikkaan = opiskeluoikeudenLisätiedot.flatMap(_.oikeusMaksuttomaanAsuntolapaikkaan.map(aikajaksoVoimassaHakuPaivalla(_, hakupaiva))).getOrElse(false),
      sisaoppilaitosmainenMaijoitus = opiskeluoikeudenLisätiedot.exists(_.koulukoti.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      koulukoti = opiskeluoikeudenLisätiedot.exists(_.koulukoti.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))),
      erityisenTuenPaatosVoimassa = voimassaOlevatErityisenTuenPäätökset.size > 0,
      erityisenTuenPaatosToimialueittain = voimassaOlevatErityisenTuenPäätökset.exists(_.opiskeleeToimintaAlueittain),
      erityisenTuenPaatosToteutuspaikat = voimassaOlevatErityisenTuenPäätökset.flatMap(_.toteutuspaikka.map(_.koodiarvo)).sorted.map(eritysopetuksentoteutuspaikkaKoodisto.getOrElse(_, "")).mkString(","),
      tukimuodot = tukimuodot(opiskeluoikeudenLisätiedot)
    )
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

  private def oppiaineenArvosanaTiedot(koodistoKoodit: String*)(oppiaineidenSuoritukset: Seq[ROsasuoritusRow]) = {
    oppiaineidenSuoritukset.filter(s => koodistoKoodit.contains(s.koulutusmoduuliKoodiarvo)) match {
      case Nil => "Oppiaine puuttuu"
      case Seq(suoritus) => oppiaineenArvosanaJaYksilöllistettyTieto(suoritus)
      case montaSamallaKoodilla@ _ => montaSamallaKoodilla.map(oppiaineenArvosanaJaYksilöllistettyTieto).mkString(",")
    }
  }

  private def oppiaineenArvosanaJaYksilöllistettyTieto(osasuoritus: ROsasuoritusRow) = {
    osasuoritus.arviointiArvosanaKoodiarvo match {
      case Some(arvosana) => arvosana + täppäIfYksilöllistetty(osasuoritus)
      case _ => "Arvosana puuttuu"
    }
  }

  private def täppäIfYksilöllistetty(osasuoritus: ROsasuoritusRow) = {
    val isYksilöllistetty = JsonSerializer.extract[Option[Boolean]](osasuoritus.data \ "yksilöllistettyOppimäärä").getOrElse(false)
    if (isYksilöllistetty) "*" else ""
  }

  private def uskonnonOppimääräIfNotElämänkatsomustieto(osasuoritukset: Seq[ROsasuoritusRow]) = {
    val hasElämänkatsomustieto = osasuoritukset.exists(_.koulutusmoduuliKoodiarvo == "ET")
    if (hasElämänkatsomustieto) "" else uskonnonOppimäärä(osasuoritukset)
  }

  private def uskonnonOppimäärä(osasuoritukset: Seq[ROsasuoritusRow]) = {
    osasuoritukset.find(_.koulutusmoduuliKoodiarvo == "KT") match {
      case Some(uskonto) => {
        JsonSerializer.extract[Option[Koodistokoodiviite]](uskonto.data \ "koulutusmoduuli" \ "uskonnonOppimäärä") match {
          case Some(uskonnonKoodistoviite) => uskonnonKoodistoviite.nimi.map(_.get("fi")).getOrElse("Oppimäärä puuttuu")
          case _ => "Oppimäärä puuttuu"
        }
      }
      case _ => "Oppimäärä puuttuu"
    }
  }

  private def getOppiaineenOppimäärä(koodistoKoodi: String)(osasuoritukset: Seq[ROsasuoritusRow]) = {
    osasuoritukset.filter(_.koulutusmoduuliKoodiarvo == koodistoKoodi) match {
      case Nil => "Oppiaine puuttuu"
      case Seq(suoritus) => getOppiaineenNimi(suoritus)
      case montaSamallaKoodilla@ _ => montaSamallaKoodilla.map(getOppiaineenNimi).mkString(",")
    }
  }

  private def getOppiaineenNimi(osasuoritus: ROsasuoritusRow) = {
    val muuAine = getFinnishNimi(osasuoritus.data \ "koulutusmoduuli" \ "tunniste" \ "nimi")
    val kieliAine = getFinnishNimi(osasuoritus.data \ "koulutusmoduuli" \ "kieli" \ "nimi")
    val result = (kieliAine, muuAine) match {
      case (Some(_), _)  => kieliAine
      case (_, Some(_)) => muuAine
      case _ => None
    }
    result.getOrElse("Oppiaine puuttuu")
  }

  private def nimiJaKoodiJaLaajuus(osasuoritus: ROsasuoritusRow) = {
    nimiJaKoodi(osasuoritus) + (osasuoritus.koulutusmoduuliLaajuusArvo match {
      case Some(laajuus) => s" ${laajuus}"
      case _ => s" Ei laajuutta"
    })
  }

  private def nimiJaKoodi(osasuoritus: ROsasuoritusRow) = {
    val jsonb = osasuoritus.data
    val kieliAine = getFinnishNimi(jsonb \ "koulutusmoduuli" \ "kieli" \ "nimi")
    val tunnisteNimi = getFinnishNimi(jsonb \ "koulutusmoduuli" \ "tunniste" \ "nimi")

    val kurssinNimi = (kieliAine, tunnisteNimi) match {
      case (Some(kieli), _) => kieli
      case (_, Some(nimi)) => nimi
      case _ => ""
    }
    s"${kurssinNimi} (${osasuoritus.koulutusmoduuliKoodiarvo})"
  }

  private def getFinnishNimi(j: JValue) = {
    JsonSerializer.extract[Option[LocalizedString]](j) match {
      case Some(nimi) => Option(nimi.get("fi"))
      case _ => None
    }
  }

  private val vuosiviikkotunnitKoodistoarvo = "3"

  private def vuosiviikkotunteja(osasuoritus: ROsasuoritusRow, op: (Float, Float) => Boolean, threshold: Float) = {
    osasuoritus.koulutusmoduuliLaajuusYksikkö.contains(vuosiviikkotunnitKoodistoarvo) &&
      op(osasuoritus.koulutusmoduuliLaajuusArvo.getOrElse(-1), threshold)
  }

  private def isNumeroarviollinen(osasuoritus: ROsasuoritusRow) = {
    osasuoritus.arviointiArvosanaKoodiarvo match {
      case Some(arvosana) => arvosana forall Character.isDigit
      case _ => false
    }
  }

  private def oneOfAikajaksoistaVoimassaHakuPaivalla(aikajakso: Option[Aikajakso], aikajaksot: Option[List[Aikajakso]], hakupaiva: LocalDate) = {
   aikajakso.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)) ||
     aikajaksot.exists(_.exists(aikajaksoVoimassaHakuPaivalla(_, hakupaiva)))
  }

  private def aikajaksoVoimassaHakuPaivalla(aikajakso: Aikajakso, paiva: LocalDate) = {
    (aikajakso.alku, aikajakso.loppu) match {
      case (alku, Some(loppu)) => !alku.isAfter(paiva) && !loppu.isBefore(paiva)
      case (alku, _) => !alku.isAfter(paiva)
    }
  }

  val perusopetuksenToiminaAlueKoodisto = Map(
    "1" -> "motoriset taidot",
    "2" -> "kieli ja kommunikaatio",
    "3"	-> "sosiaaliset taidot",
    "4"	-> "päivittäisten toimintojen taidot",
    "5"	->"kognitiiviset taidot"
  )

  val eritysopetuksentoteutuspaikkaKoodisto = Map(
    "1" -> "Opetus on kokonaan erityisryhmissä tai -luokassa",
    "2" -> "Opetuksesta 1-19 % on yleisopetuksen ryhmissä",
    "3"	-> "Opetuksesta 20-49 % on yleisopetuksen ryhmissä",
    "4"	-> "Opetuksesta 50-79 % on yleisopetuksen ryhmissä",
    "5"	-> "Opetuksesta 80-100 % on yleisopetuksen ryhmissä"
  )

 private def combineErityisenTuenPäätökset(erityisenTuenPäätös: Option[ErityisenTuenPäätös], erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]]) = {
   (erityisenTuenPäätös, erityisenTuenPäätökset) match {
     case (Some(paatos), Some(paatokset)) => paatos :: paatokset
     case (Some(paatos), _) => List(paatos)
     case (_, Some(paatokset)) => paatokset
     case _ => List.empty
   }
 }

  private def erityisentuenPäätösvoimassaPaivalla(päätös: ErityisenTuenPäätös, paiva: LocalDate) = {
    (päätös.alku, päätös.loppu) match {
      case (Some(alku), Some(loppu)) => !alku.isAfter(paiva) && !loppu.isBefore(paiva)
      case (Some(alku), _) => !alku.isAfter(paiva)
      case _ => false
    }
  }

  private def tukimuodot(lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot]) = {
    if (lisätiedot.isDefined) {
      lisätiedot.get.tukimuodot match {
        case Some(tukimuodot) => tukimuodot.flatMap(_.nimi.map(_.get("fi"))).mkString(",")
        case _ => ""
      }
    } else ""
  }

  def title(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String = "TITLE TODO"

  def documentation(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String, loadCompleted: Timestamp): String =
    s"""
      |Tarkempi kuvaus joistakin sarakkeista:
      |
      |- Sukupuoli: 1 = mies, 2 = nainen
      |- Viimeisin opiskeluoikeuden tila: Se opiskeluoikeuden tila, joka opiskeluoikeudella on nyt.
      |- Opiskeluoikeuden tila tulostuspäivänä: Opiskeluoikeuden tila, joka opiskeluoikeudella oli sinä päivämääränä, joka on syötetty tulostusparametreissa ”Päivä”-kenttään.
      |- Suorituksen vahvistuspäivä: Sen päätason suorituksen (vuosiluokka tai perusopetuksen oppimäärä), jolta raportti on tulostettu, vahvistuspäivä.
      |- Vuosiluokkien suoritukset, joilta puuttuu vahvistus: Lista niistä vuosiluokista, joilta puuttuu vahvistus. Jos tässä sarakkeessa on useampia vuosiluokkia, se on osoitus siitä, että tiedoissa on virheitä.
      |- Pakollisten oppiaineiden arvosana- ja oppimääräsarakkeet (sarakkeet Q-AI, TARKISTA NÄMÄ LOPULLISESTA RAPORTISTA): Valtakunnalliset oppiainesuoritukset (https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/koskioppiaineetyleissivistava/latest), jotka siirretty pakollisena.
      |- Paikallisten oppiaineiden koodit: Vuosiluokkasuorituksella olevien paikallisten oppiaineiden koodit. Jos tästä löytyy jokin valtakunnallinen oppiaine (https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/koskioppiaineetyleissivistava/latest), tiedonsiirroissa on siirretty virheellisesti valtakunnallinen oppiaine paikallisena.
      |- Pakolliset paikalliset oppiaineet: Pakollisissa oppiaineissa olevat paikalliset oppiaineet. Pääsääntöisesti, jos tässä sarakkeessa on mitään arvoja, oppiaineiden siirrossa on tapahtunut virhe (eli joko pakollinen valtakunnallinen oppiaine on siirretty pakollisena paikallisena oppiaineena tai valinnainen paikallinen oppiaine on siirretty pakollisena paikallisena oppiaineena). Vain tietyillä erityiskouluilla (esim. steinerkoulut) on pakollisia paikallisia oppiaineita.
      |- Valinnaiset oppiaineet joilla on numeroarviointi ja joiden laajuus on pienempi kuin 2 vuosiviikkotuntia: Jos tässä sarakkeessa on muita kuin tyhjiä kenttiä, kyseisten oppiaineiden siirrossa on jokin virhe (eli joko oppiaineen laajuus on oikeasti 2 vuosiviikkotuntia tai enemmän tai sitten alle 2 vuosiviikkotunnin laajuiselle valinnaiselle oppiaineelle on virheellisesti siirretty numeroarvosana). Alle 2 vuosiviikkotunnin laajuisella oppiainesuorituksella ei pitäisi olla numeroarvosanaa.
      |- Vahvistetut toiminta-alueiden suoritukset: Sarake listaa S-merkinnällä vahvistetut toiminta-alueen suoritukset niiltä oppilailta, jotka opiskelevat toiminta-alueittain. Jos sarakkeesta löytyy kenttiä, joissa on vähemmän kuin viisi toiminta-alueittain opiskeltavan perusopetuksen toiminta-aluetta (https://virkailija.opintopolku.fi/koski/dokumentaatio/koodisto/perusopetuksentoimintaalue/latest), suoritettujen toiminta-alueiden siirroissa on todennäköisesti virhe.
      |- Opiskeluoikeuden lisätiedoissa ilmoitettavat etu- ja tukimuodot (sarakkeet AS-BI, TARKISTA NÄMÄ LOPULLISESTA RAPORTISTA): Sarakkeessa oleva arvo kertoo, onko siirretyn KOSKI-datan mukaan kyseinen etu- tai tukimuoto ollut voimassa raportin tulostusparametrien ”Päivä”-kenttään syötettynä päivämääränä. Esimerkki: Jos raportti on tulostettu päivälle 1.6.2019 ja oppilaalla on opiskeluoikeuden lisätiedoissa majoitusjakso välillä 1.1.-31.12.2019, ”Majoitusetu”-sarakkeeseen tulostuu oppilaan rivillä ”Kyllä”.
    """.stripMargin

  def filename(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String = {
    s"Perusopetuksen_vuosiluokka_${oppilaitosOid}_${vuosiluokka}_${paiva}.xlsx"
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "oppilaitoksenNimi" -> Column("Oppilaitoksen nimi"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä"),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "sukupuoli" -> Column("Sukupuoli"),
    "viimeisinTila" -> Column("Viimeisin opiskeluoikeuden tila"),
    "tilaHakupaivalla" -> Column("Opiskeluoikeuden tila tulostuspäivänä"),
    "suorituksenTila" -> Column("Suorituksen tila"),
    "suorituksenAlkamispaiva" -> Column("Suoritukselle merkattu alkamaispäivä"),
    "suorituksenVahvistuspaiva" -> Column("Suorituksen vahvistuspäivä"),
    "jaaLuokalle" -> Column("Jää luokalle"),
    "luokka" -> Column("Luokan tunniste"),
    "voimassaolevatVuosiluokat" -> Column("Vuosiluokkien suoritukset joilta puuttuu vahvistus"),
    "aidinkieli" -> Column("Äidinkieli"),
    "pakollisenAidinkielenOppimaara" -> Column("Äidinkielen oppimäärä"),
    "kieliA" -> Column("A-kieli"),
    "kieliAOppimaara" -> Column("A-kielen oppimäärä"),
    "kieliB" -> Column("B-kieli"),
    "kieliBOppimaara" -> Column("B-kielen oppimäärä"),
    "uskonto" -> Column("Uskonto/Elämänkatsomustieto"),
    "uskonnonOppimaara" -> Column("Uskonnon oppimäärä"),
    "historia" -> Column("Historia"),
    "yhteiskuntaoppi" -> Column("Yhteiskuntaoppi"),
    "matematiikka" -> Column("Matematiikka"),
    "kemia" -> Column("Kemia"),
    "fysiikka" -> Column("Fysiikka"),
    "biologia" -> Column("Biologia"),
    "maantieto" -> Column("Maantieto"),
    "musiikki" -> Column("Musiikki"),
    "kuvataide" -> Column("Kuvataide"),
    "kotitalous" -> Column("Kotitalous"),
    "terveystieto" -> Column("Terveystieto"),
    "kasityo" -> Column("Käsityö"),
    "liikunta" -> Column("Liikunta"),
    "ymparistooppi" -> Column("Ympäristöoppi"),
    "kayttaymisenArvio" -> Column("Käyttäytymisen arviointi"),
    "paikallistenOppiaineidenKoodit" -> Column("Paikallisten oppiaineiden koodit"),
    "pakollisetPaikalliset" -> Column("Pakolliset paikalliset oppiaineet"),
    "valinnaisetPaikalliset" -> Column("Valinnaiset paikalliset oppiaineet"),
    "valinnaisetValtakunnalliset" -> Column("Valinnaiset valtakunnalliset oppiaineet"),
    "valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet, joiden laajuus on suurempi tai yhtäsuuri kuin 2 vuosiviikkotuntia"),
    "valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet, joiden laajuus on pienempi kuin 2 vuosiviikko tuntia"),
    "numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet, joilla on numeroarviointi ja joiden laajuus on pienempi kuin 2 vuosiviikkotuntia"),
    "valinnaisetEiLaajuutta" -> Column("Valinnaiset oppiaineet, joilla ei ole laajuutta"),
    "vahvistetutToimintaAlueidenSuoritukset" -> Column("Vahvistetut toiminta-alueiden suoritukset"),
    "majoitusetu" -> Column("Majoitusetu"),
    "kuljetusetu" -> Column("Kuljetusetu"),
    "kotiopetus" -> Column("Kotiopetus"),
    "ulkomailla" -> Column("Ulkomailla"),
    "perusopetuksenAloittamistaLykatty" -> Column("Perusopetuksen aloittamista lykätty"),
    "aloittanutEnnenOppivelvollisuutta" -> Column("Aloittanut ennen oppivelvollisuutta"),
    "pidennettyOppivelvollisuus" -> Column("Pidennetty oppivelvollisuus"),
    "tehostetunTuenPaatos" -> Column("Tehostetun tuen päätös"),
    "joustavaPerusopetus" -> Column("Joustava perusopetus"),
    "vuosiluokkiinSitoutumatonOpetus" -> Column("Vuosiluokkiin sitomaton opetus"),
    "vammainen" -> Column("Vammainen"),
    "vaikeastiVammainen" -> Column("Vaikeasti vammainen"),
    "oikeusMaksuttomaanAsuntolapaikkaan" -> Column("Oikeus maksuttomaan asuntolapaikkaan"),
    "sisaoppilaitosmainenMaijoitus" -> Column("Sisäoppilaitosmainen majoitus"),
    "koulukoti" -> Column("Koulukoti"),
    "erityisenTuenPaatosVoimassa" -> Column("Erityisen tuen päätös"),
    "erityisenTuenPaatosToimialueittain" -> Column("Opiskelee toimialueittain"),
    "erityisenTuenPaatosToteutuspaikat" -> Column("Erityisen tuen päätöksen toteutuspaikka"),
    "tukimuodot" -> Column("Tukimuodot")
  )
}

private[raportit] case class PerusopetusRow(
  opiskeluoikeusOid: String,
  oppilaitoksenNimi: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  sukupuoli: Option[String],
  viimeisinTila: String,
  tilaHakupaivalla: String,
  suorituksenTila: String,
  suorituksenAlkamispaiva: String,
  suorituksenVahvistuspaiva: String,
  jaaLuokalle: Boolean,
  luokka: String,
  voimassaolevatVuosiluokat: String,
  aidinkieli: String,
  pakollisenAidinkielenOppimaara: String,
  kieliA: String,
  kieliAOppimaara: String,
  kieliB: String,
  kieliBOppimaara: String,
  uskonto: String,
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
  tehostetunTuenPaatos: Boolean,
  joustavaPerusopetus: Boolean,
  vuosiluokkiinSitoutumatonOpetus: Boolean,
  vammainen: Boolean,
  vaikeastiVammainen: Boolean,
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean,
  sisaoppilaitosmainenMaijoitus: Boolean,
  koulukoti: Boolean,
  erityisenTuenPaatosVoimassa: Boolean,
  erityisenTuenPaatosToimialueittain: Boolean,
  erityisenTuenPaatosToteutuspaikat: String,
  tukimuodot: String
)
