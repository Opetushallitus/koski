package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{Aikajakso, LocalizedString, LähdejärjestelmäId, PerusopetuksenOpiskeluoikeudenLisätiedot}
import fi.oph.koski.schema.Organisaatio.Oid
import org.json4s.JValue

object PerusopetuksenVuosiluokka extends VuosiluokkaRaporttiPaivalta {

  def buildRaportti(repository: PerusopetuksenRaportitRepository, oppilaitosOid: Oid, paiva: LocalDate, vuosiluokka: String): Seq[PerusopetusRow] = {
    val rows: Seq[(ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow])] = repository.perusopetuksenvuosiluokka(oppilaitosOid, paiva, vuosiluokka)
    rows.map(buildRow(_, paiva))
  }

  def title(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String = "TITLE TODO"

  def documentation(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String, loadCompleted: Timestamp): String = "Dokumentaatio TODO"

  def filename(oppilaitosOid: String, paiva: LocalDate, vuosiluokka: String): String = {
    s"Perusopeutuksen_vuosiluokka:${vuosiluokka}_${paiva}.xlsx"
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column("Opiskeluoikeuden oid"),
    "lähdejärjestelmä" -> Column("Lähdejärjestelmä"),
    "lähdejärjestelmänId" -> Column("Opiskeluoikeuden tunniste lähdejärjestelmässä"),
    "oppijaOid" -> Column("Oppijan oid"),
    "hetu" -> Column("hetu"),
    "sukunimi" -> Column("Sukunimi"),
    "etunimet" -> Column("Etunimet"),
    "viimeisinTila" -> Column("Viimeisin tila"),
    "luokka" -> Column("Luokan tunniste"),
    "aidinkieli" -> Column("Äidinkieli"),
    "kieliA" -> Column("A-kieli"),
    "kieliB" -> Column("B-kieli"),
    "uskonto" -> Column("Uskonto"),
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
    "paikallistenOppiaineidenKoodit" -> Column("Paikallisten oppiaineden koodit"),
    "pakollisetPaikalliset" -> Column("Pakolliset paikalliset oppiaineet"),
    "valinnaisetPaikalliset" -> Column("Valinnaiset paikaliset oppiaineet"),
    "valinnaisetValtakunnalliset" -> Column("Valinnaiset valtakunnalliset oppiaineet"),
    "valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet joiden laajuus on suurempi kuin 2 vuosiviikkotuntia"),
    "valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet joiden laajuus on pienempi kuin 2 vuosiviikko tuntia"),
    "numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia" -> Column("Valinnaiset oppiaineet joilla on numeroarviointi ja niiden laajuus on pienempi kuin 2 vuosiviikkotuntia")
  )

  private def buildRow(data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow])) = {
    val (opiskeluoikeus, henkilö, aikajaksot, päätasonsuoritukset, osasuoritukset) = data
    println(henkilö.map(_.oppijaOid))

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
      viimeisinTila = aikajaksot.last.tila,
      luokka = päätasonsuoritukset.flatMap(ps => JsonSerializer.extract[Option[String]](ps.data \ "luokka")).mkString(","),
      aidinkieli = getOppiaineenArvosana("AI")(pakollisetValtakunnalliset),
      kieliA = getOppiaineenArvosana("A1")(pakollisetValtakunnalliset),
      kieliB = getOppiaineenArvosana("B1")(pakollisetValtakunnalliset),
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
      paikallistenOppiaineidenKoodit = paikalliset.map(_.koulutusmoduuliKoodiarvo).mkString(","),
      pakollisetPaikalliset = paikalliset.filter(isPakollinen).map(nimiJaKoodi).mkString(","),
      valinnaisetPaikalliset = paikalliset.filterNot(isPakollinen).map(nimiJaKoodi).mkString(","),
      valinnaisetValtakunnalliset = valinnaisetValtakunnalliset.map(nimiJaKoodi).mkString(","),
      valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ > _, 2)).map(nimiJaKoodi).mkString(","),
      valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(vuosiviikkotunteja(_, _ < _, 2)).map(nimiJaKoodi).mkString(","),
      numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = kaikkiValinnaiset.filter(os => vuosiviikkotunteja(os, _ < _, 2) && isNumeroarviollinen(os)).map(nimiJaKoodi).mkString(",")
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
    val suoritus = oppiaineidenSuoritukset.filter(_.koulutusmoduuliKoodiarvo == koodistoKoodi) match {
      case Nil => None
      case Seq(suoritus) => suoritus.arviointiArvosanaKoodiarvo
      case _ => Option(s"Koodille ${koodistoKoodi} löytyy monta")
    }
    suoritus.getOrElse("Arvosana puuttuu")
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
}

private[raportit] case class PerusopetusRow
(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: Option[String],
  etunimet: Option[String],
  viimeisinTila: String,
  luokka: String,
  aidinkieli: String,
  kieliA: String,
  kieliB: String,
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
  paikallistenOppiaineidenKoodit: String,
  pakollisetPaikalliset: String,
  valinnaisetPaikalliset: String,
  valinnaisetValtakunnalliset: String,
  valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia: String,
  valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia: String,
  numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia: String
)
