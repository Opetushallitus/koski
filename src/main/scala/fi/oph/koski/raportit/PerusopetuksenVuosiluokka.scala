package fi.oph.koski.raportit

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString, LähdejärjestelmäId, PerusopetuksenOppiaine}
import fi.oph.koski.schema.Organisaatio.Oid

object PerusopetuksenVuosiluokka extends VuosiluokkaRaportti {

  def buildRaportti(raportointiDatabase: RaportointiDatabase, oppilaitosOid: Oid, alku: LocalDate, loppu: LocalDate, vuosiluokka: String): Seq[PerusopetusRow] = {
    val rows: Seq[(ROpiskeluoikeusRow, Option[RHenkilöRow], List[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow])] = raportointiDatabase.perusopetuksenvuosiluokka(oppilaitosOid, alku, loppu, vuosiluokka)
    rows.map(buildRow(_))
  }

  def title(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, vuosiluokka: String): String = "TITLE TODO"

  def documentation(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, loadCompleted: Timestamp): String = "Dokumentaatio TODO"

  def filename(oppilaitosOid: String, alku: LocalDate, loppu: LocalDate, vuosiluokka: String): String = {
    s"Perusopeutuksen_vuosiluokka:${vuosiluokka}_${alku}_${loppu}"
  }

  val columnSettings: Seq[(String, Column)] = Seq.empty

  private def buildRow(data: (ROpiskeluoikeusRow, Option[RHenkilöRow], Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow])) = {
    val (opiskeluoikeus, henkilö, aikajaksot, _, osasuoritukset) = data
    println(henkilö.map(_.oppijaOid))

    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](opiskeluoikeus.data \ "lähdejärjestelmänId")
    val (valtakunnalliset, paikalliset) = osasuoritukset.partition(isValtakunnallinenOppiaine(_))
    val valinnaiset = osasuoritukset.filterNot(isPakollinen)

    PerusopetusRow(
      opiskeluoikeusOid = opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmänId.flatMap(_.id),
      oppijaOid = opiskeluoikeus.oppijaOid,
      hetu = henkilö.flatMap(_.hetu),
      sukunimi = henkilö.map(_.sukunimi),
      etunimet = henkilö.map(_.etunimet),
      viimeisinTila = aikajaksot.last.tila,
      aidinkieli = getOppiaineenArvosana("AI")(valtakunnalliset),
      kieliA = getOppiaineenArvosana("A1")(valtakunnalliset),
      kieliB = getOppiaineenArvosana("B1")(valtakunnalliset),
      uskonto = getOppiaineenArvosana("KT")(valtakunnalliset),
      historia = getOppiaineenArvosana("HI")(valtakunnalliset),
      yhteiskuntaoppi = getOppiaineenArvosana("YH")(valtakunnalliset),
      matematiikka = getOppiaineenArvosana("MA")(valtakunnalliset),
      kemia = getOppiaineenArvosana("KE")(valtakunnalliset),
      fysiikka = getOppiaineenArvosana("FY")(valtakunnalliset),
      biologia = getOppiaineenArvosana("BI")(valtakunnalliset),
      maantieto = getOppiaineenArvosana("GE")(valtakunnalliset),
      musiikki = getOppiaineenArvosana("MU")(valtakunnalliset),
      kuvataide = getOppiaineenArvosana("KU")(valtakunnalliset),
      kotitalous = getOppiaineenArvosana("KO")(valtakunnalliset),
      terveystieto = getOppiaineenArvosana("TE")(valtakunnalliset),
      kasityo = getOppiaineenArvosana("KS")(valtakunnalliset),
      liikunta = getOppiaineenArvosana("LI")(valtakunnalliset),
      paikallistenOppiaineidenKoodit = paikalliset.map(_.koulutusmoduuliKoodiarvo).mkString(","),
      pakollisetPaikalliset = paikalliset.filter(isPakollinen).map(nimiJaKoodi).mkString(","),
      valinnaisetPaikalliset = paikalliset.filterNot(isPakollinen).map(nimiJaKoodi).mkString(","),
      valinnaisetValtakunnalliset = osasuoritukset.filter(isValtakunnallinenOppiaine(_, pakollinen = false)).map(nimiJaKoodi).mkString(","),
      valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = valinnaiset.filter(vuosiviikkotuntejaEnemmänKuin(_, 2)).map(nimiJaKoodi).mkString(","),
      valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = valinnaiset.filter(vuosiviikkotutuntejaVähemmänKuin(_, 2)).map(nimiJaKoodi).mkString(","),
      numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = valinnaiset.filter(vuosiviikkotutuntejaVähemmänKuin(_, 2)).filter(isNumeroarviollinen).map(nimiJaKoodi).mkString(",")
    )
  }

  private val yleissivistäväkoodisto = Seq(
    "A1", "A2", "AI", "B1", "B2", "B3", "BI", "ET", "FI", "FY", "GE", "HI", "KE", "KO", "KS", "KT", "KU", "LI", "MA", "MU", "OP", "OPA", "PS", "TE", "YH", "YL"
  )

  private def isValtakunnallinenOppiaine(osasuoritus: ROsasuoritusRow, pakollinen: Boolean = true) = {
    yleissivistäväkoodisto.contains(osasuoritus.koulutusmoduuliKoodiarvo) &&
      osasuoritus.koulutusmoduuliKoodisto.contains("koskioppiaineetyleissivistava") &&
      (if (pakollinen) isPakollinen(osasuoritus) else !isPakollinen(osasuoritus))
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
    val koulutusmoduuliNimi = JsonSerializer.extract[Option[LocalizedString]](osasuoritus.data \ "koulutusmoduuli" \ "tunniste" \"nimi")
    val nimi = koulutusmoduuliNimi match {
      case Some(moduuli) => moduuli.get("fi")
      case _ => ""
    }

    kielenNimi(osasuoritus) match {
      case Some(kieli) => s"${nimi} (${osasuoritus.koulutusmoduuliKoodiarvo}) ${kieli}"
      case _ => s"${nimi} (${osasuoritus.koulutusmoduuliKoodiarvo})"
    }
  }

  private def kielenNimi(osasuoritus: ROsasuoritusRow) = {
    JsonSerializer.extract[Option[LocalizedString]](osasuoritus.data \ "koulutusmoduuli" \ "kieli" \ "nimi") match {
      case Some(kieli) => Option(kieli.get("fi"))
      case _ => None
    }
  }

  private val vuosiviikkotunnitKoodistoarvo = "3"

  private def vuosiviikkotuntejaEnemmänKuin(osasuoritus: ROsasuoritusRow, value: Float) = {
    osasuoritus.koulutusmoduuliLaajuusYksikkö == vuosiviikkotunnitKoodistoarvo &&
      osasuoritus.koulutusmoduuliLaajuusArvo.exists(_ > value)
  }

  private def vuosiviikkotutuntejaVähemmänKuin(osasuoritus: ROsasuoritusRow, value: Float) = {
    osasuoritus.koulutusmoduuliLaajuusYksikkö == vuosiviikkotunnitKoodistoarvo &&
      osasuoritus.koulutusmoduuliLaajuusArvo.exists(_ < value)
  }

  private def isNumeroarviollinen(osasuoritus: ROsasuoritusRow) = {
    osasuoritus.arviointiArvosanaKoodiarvo match {
      case Some(arvosana) => arvosana forall Character.isDigit
      case _=> false
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
  paikallistenOppiaineidenKoodit: String,
  pakollisetPaikalliset: String,
  valinnaisetPaikalliset: String,
  valinnaisetValtakunnalliset: String,
  valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia: String,
  valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia: String,
  numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia: String
)
