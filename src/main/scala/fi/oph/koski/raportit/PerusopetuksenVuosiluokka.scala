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
    val (valtakunnalliset, paikalliset) = osasuoritukset.partition(isValtakunnallinenOppiaine)
    val (pakollisetValtakunnalliset, valinnaisetValtakunnalliset) = valtakunnalliset.partition(isPakollinen)
    val (pakollisetPaikalliset, valinnaisetPaikalliset) = paikalliset.partition(isPakollinen)
    val kaikkiValinnaiset = valinnaisetPaikalliset.union(valinnaisetValtakunnalliset)

    PerusopetusRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
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
      aidinkieli = getOppiaineenArvosana("AI")(pakollisetValtakunnalliset),
      pakollisenAidinkielenOppimaara = getOppiaineenOppimäärä("AI")(pakollisetValtakunnalliset),
      kieliA = getOppiaineenArvosana("A1")(pakollisetValtakunnalliset),
      kieliAOppimaara = getOppiaineenOppimäärä("A1")(pakollisetValtakunnalliset),
      kieliB = getOppiaineenArvosana("B1")(pakollisetValtakunnalliset),
      kieliBOppimaara = getOppiaineenOppimäärä("B1")(pakollisetValtakunnalliset),
      uskonto = getOppiaineenArvosana("KT")(pakollisetValtakunnalliset),
      historia = getOppiaineenArvosana("HI")(pakollisetValtakunnalliset),
      yhteiskuntaoppi = getOppiaineenArvosana("YH")(pakollisetValtakunnalliset),
      matematiikka = getOppiaineenArvosana("MA")(pakollisetValtakunnalliset),
      kemia = getOppiaineenArvosana("KE")(pakollisetValtakunnalliset),
      fysiikka = getOppiaineenArvosana("FY")(pakollisetValtakunnalliset),
      biologia = getOppiaineenArvosana("BI")(pakollisetValtakunnalliset),
      maantieto = getOppiaineenArvosana("GE")(pakollisetValtakunnalliset),
      musiikki = getOppiaineenArvosana("MU")(pakollisetValtakunnalliset),
      kuvataide = getOppiaineenArvosana("KU")(pakollisetValtakunnalliset),
      kotitalous = getOppiaineenArvosana("KO")(pakollisetValtakunnalliset),
      terveystieto = getOppiaineenArvosana("TE")(pakollisetValtakunnalliset),
      kasityo = getOppiaineenArvosana("KS")(pakollisetValtakunnalliset),
      liikunta = getOppiaineenArvosana("LI")(pakollisetValtakunnalliset),
      ymparistooppi = getOppiaineenArvosana("YL")(pakollisetValtakunnalliset),
      kayttaymisenArvio = JsonSerializer.extract[Option[PerusopetuksenKäyttäytymisenArviointi]](päätasonsuoritus.data \ "käyttäytymisenArvio").map(_.arvosana.koodiarvo).getOrElse(""),
      paikallistenOppiaineidenKoodit = paikalliset.map(_.koulutusmoduuliKoodiarvo).mkString(","),
      pakollisetPaikalliset = pakollisetPaikalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetPaikalliset = valinnaisetPaikalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetValtakunnalliset = valinnaisetValtakunnalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ >= _, 2)).map(nimiJaKoodiJaLaajuus).mkString(","),
      valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ < _, 2)).map(nimiJaKoodiJaLaajuus).mkString(","),
      numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(os => vuosiviikkotunteja(os, _ < _, 2) && isNumeroarviollinen(os)).map(nimiJaKoodiJaLaajuus).mkString(","),
      valinnaisetEiLaajuutta = kaikkiValinnaiset.filter(_.koulutusmoduuliLaajuusArvo.isEmpty).map(nimiJaKoodi).mkString(","),
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
      erityisenTuenPaatos = erityisenTuenPäätökset(opiskeluoikeudenLisätiedot, hakupaiva),
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

  private def getOppiaineenArvosana(koodistoKoodi: String)(oppiaineidenSuoritukset: Seq[ROsasuoritusRow]) = {
    oppiaineidenSuoritukset.filter(_.koulutusmoduuliKoodiarvo == koodistoKoodi) match {
      case Nil => "Arvosana puuttuu"
      case Seq(suoritus) => suoritus.arviointiArvosanaKoodiarvo.getOrElse("Arvosana puuttuu")
      case montaSamallaKoodilla@ _ => montaSamallaKoodilla.map(_.arviointiArvosanaKoodiarvo.getOrElse("Arvosana puuttuu")).mkString(",")
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

  val eritysopetuksentoteutuspaikkaKoodisto = Map(
    "1" -> "Opetus on kokonaan erityisryhmissä tai -luokassa",
    "2" -> "Opetuksesta 1-19 % on yleisopetuksen ryhmissä",
    "3"	-> "Opetuksesta 20-49 % on yleisopetuksen ryhmissä",
    "4"	-> "Opetuksesta 50-79 % on yleisopetuksen ryhmissä",
    "5"	-> "Opetuksesta 80-100 % on yleisopetuksen ryhmissä"
  )

 private def erityisenTuenPäätökset(lisa: Option[PerusopetuksenOpiskeluoikeudenLisätiedot], hakupaiva: LocalDate) = {
   lisa match {
     case Some(lisätiedot) =>
       (
         voimassaOlevaErityisenTuenPäätösKoodiarvo(lisätiedot.erityisenTuenPäätös, hakupaiva) ::
         voimassaOlevatErityisenTuenPäätöksetKoodiarvot(lisätiedot.erityisenTuenPäätökset, hakupaiva)
       ).flatten.mkString(",")
     case _ => ""
   }
 }

  private def voimassaOlevatErityisenTuenPäätöksetKoodiarvot(päätökset: Option[List[ErityisenTuenPäätös]], hakupaiva: LocalDate) = {
    päätökset match {
      case Some(ps) => ps.filter(erityisentuenPäätösvoimassaPaivalla(_, hakupaiva)).flatMap(_.toteutuspaikka.map(viite => eritysopetuksentoteutuspaikkaKoodisto.get(viite.koodiarvo)))
      case _ => List.empty
    }
  }

  private def voimassaOlevaErityisenTuenPäätösKoodiarvo(päätös: Option[ErityisenTuenPäätös], hakupaiva: LocalDate) = {
    päätös match {
      case Some(p) if (erityisentuenPäätösvoimassaPaivalla(p, hakupaiva)) => p.toteutuspaikka.flatMap(viite => eritysopetuksentoteutuspaikkaKoodisto.get(viite.koodiarvo))
      case _ => None
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

  def documentation(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String, loadCompleted: Timestamp): String = "Dokumentaatio TODO"

  def filename(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String = {
    s"Perusopetuksen_vuosiluokka_${oppilaitosOid}_${vuosiluokka}_${paiva}.xlsx"
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä"),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "sukupuoli" -> Column("Sukupuoli"),
    "viimeisinTila" -> Column("Viimeisin opiskeluoikeuden tila"),
    "tilaHakupaivalla" -> Column("Opiskeluoikeuden tila raportin parametrina annettuna päivänä"),
    "suorituksenTila" -> Column("Suorituksen tila"),
    "suorituksenAlkamispaiva" -> Column("Vuosiluokan suoritukselle merkattu alkamaispaiva"),
    "suorituksenVahvistuspaiva" -> Column("Vuosiluokan suorituksen vahvistuspaiva"),
    "jaaLuokalle" -> Column("Jää luokalle"),
    "luokka" -> Column("Luokan tunniste"),
    "voimassaolevatVuosiluokat" -> Column("Vuosiluokkien suoritukset joilta puuttuu vahvistus"),
    "aidinkieli" -> Column("Äidinkieli"),
    "pakollisenAidinkielenOppimaara" -> Column("Pakollisen aidinkielen oppimäärä"),
    "kieliA" -> Column("A-kieli"),
    "kieliAOppimaara" -> Column("A-kielen oppimaara"),
    "kieliB" -> Column("B-kieli"),
    "kieliBOppimaara" -> Column("B-kielen oppimaara"),
    "uskonto" -> Column("Uskonto/Elämänkatsomustieto"),
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
    "kasityo" -> Column("Käsityo"),
    "liikunta" -> Column("Liikunta"),
    "ymparistooppi" -> Column("Ympäristöoppi"),
    "kayttaymisenArvio" -> Column("Käyttäytymisen arviointi"),
    "paikallistenOppiaineidenKoodit" -> Column("Paikallisten oppiaineden koodit"),
    "pakollisetPaikalliset" -> Column("Pakolliset paikalliset oppiaineet"),
    "valinnaisetPaikalliset" -> Column("Valinnaiset paikaliset oppiaineet"),
    "valinnaisetValtakunnalliset" -> Column("Valinnaiset valtakunnalliset oppiaineet"),
    "valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet joiden laajuus on suurempi kuin 2 vuosiviikkotuntia"),
    "valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet joiden laajuus on pienempi kuin 2 vuosiviikko tuntia"),
    "numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet joilla on numeroarviointi ja niiden laajuus on pienempi kuin 2 vuosiviikkotuntia"),
    "valinnaisetEiLaajuutta" -> Column("Valinnaiset oppiaineet joilla ei ole laajuutta"),
    "majoitusetu" -> Column("Majoitusetu"),
    "kuljetusetu" -> Column("Kuljetusetu"),
    "kotiopetus" -> Column("Kotiopetus"),
    "ulkomailla" -> Column("Ulkomailla"),
    "perusopetuksenAloittamistaLykatty" -> Column("Perusopetuksen aloittamista lykätty"),
    "aloittanutEnnenOppivelvollisuutta" -> Column("Aloittanut ennen oppivelvollisuutta"),
    "pidennettyOppivelvollisuus" -> Column("Pidennetty oppivelvollisuus"),
    "tehostetunTuenPaatos" -> Column("Tehostetun tuen päätös"),
    "joustavaPerusopetus" -> Column("Joustava perusopetus"),
    "vuosiluokkiinSitoutumatonOpetus" -> Column("Vuosiluokkiin sitoutumaton opetus"),
    "vammainen" -> Column("Vammainen"),
    "vaikeastiVammainen" -> Column("Vaikeasti vammainen"),
    "oikeusMaksuttomaanAsuntolapaikkaan" -> Column("Oikeus maksuttomaan asuntolapaikkaan"),
    "sisaoppilaitosmainenMaijoitus" -> Column("Sisäoppilaitosmainen majoitus"),
    "koulukoti" -> Column("Koulukoti"),
    "erityisenTuenPaatos" -> Column("Erityisen tuen päätos"),
    "tukimuodot" -> Column("Tukimuodot")
  )
}

private[raportit] case class PerusopetusRow(
  opiskeluoikeusOid: String,
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
  erityisenTuenPaatos: String,
  tukimuodot: String
)
