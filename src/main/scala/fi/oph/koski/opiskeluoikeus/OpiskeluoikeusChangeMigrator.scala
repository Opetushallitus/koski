package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.log.Logging
import fi.oph.koski.schema._


object OpiskeluoikeusChangeMigrator extends Logging {
  def migrate(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowDeleteCompleted: Boolean): KoskeenTallennettavaOpiskeluoikeus = {
    uusiOpiskeluoikeus match {
      case _: YlioppilastutkinnonOpiskeluoikeus =>
        uusiOpiskeluoikeus
      case _ if uusiOpiskeluoikeus.mitätöity =>
        // Jos uusi opiskeluoikeus ollaan mitätöimässä, jätetään huomiotta kaikki muut muutokset, joita ollaan mahdollisesti tekemässä
        vanhaOpiskeluoikeus.invalidated(uusiOpiskeluoikeus.mitätöintiPäivä.get)
      case _ =>
        val uusiOpiskeluoikeusSuorituksilla = if (allowDeleteCompleted) uusiOpiskeluoikeus else {
          logOsittainenSiirto(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)
          kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)
        }
        organisaationMuutosHistoria(vanhaOpiskeluoikeus, uusiOpiskeluoikeusSuorituksilla)
    }
  }

  def kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    if (OpiskeluoikeudenTyyppi.ammatillinenkoulutus == uusiOpiskeluoikeus.tyyppi) {
      uusiOpiskeluoikeus
    } else {
      val puuttuvatSuorituksetUudessa = vanhaOpiskeluoikeus.suoritukset
        .filter(kopioitavaPäätasonSuoritus)
        .filter { vanhaSuoritus =>
          vanhaSuoritus.valmis && !uusiOpiskeluoikeus.suoritukset.exists(_.koulutusmoduuli.tunniste == vanhaSuoritus.koulutusmoduuli.tunniste)
        }
      uusiOpiskeluoikeus.withSuoritukset(puuttuvatSuorituksetUudessa ++ uusiOpiskeluoikeus.suoritukset)
    }
  }

  private def logOsittainenSiirto(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Unit = {
    if (uusiOpiskeluoikeus.suoritukset.size < vanhaOpiskeluoikeus.suoritukset.size) {
      logger.info(
        s"Osittainen päätason suoritusten siirto opiskeluoikeuteen ${vanhaOpiskeluoikeus.oid.getOrElse("?")}: " +
          s"siirrossa ${koulutusmoduulit(uusiOpiskeluoikeus)}, tallennettuna ${koulutusmoduulit(vanhaOpiskeluoikeus)}"
      )
    }
  }

  private def koulutusmoduulit(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): String =
    opiskeluoikeus.suoritukset.map(s => s"${s.tyyppi.koodiarvo}/${s.koulutusmoduuli.tunniste.koodiarvo}").mkString(", ")

  private def organisaationMuutosHistoria(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    if (oppilaitoksenTaiKoulutustoimijanOidMuuttunut(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)) {
      val vanhaHistoria = vanhaOpiskeluoikeus.organisaatiohistoria.toList.flatten
      val muutos = OpiskeluoikeudenOrganisaatiohistoria(LocalDate.now(), vanhaOpiskeluoikeus.oppilaitos, vanhaOpiskeluoikeus.koulutustoimija)
      uusiOpiskeluoikeus.withHistoria(Some(vanhaHistoria :+ muutos))
    } else {
      uusiOpiskeluoikeus.withHistoria(vanhaOpiskeluoikeus.organisaatiohistoria)
    }
  }

  private def kopioitavaPäätasonSuoritus(suoritus: KoskeenTallennettavaPäätasonSuoritus) = suoritus match {
    case _: LukionOppiaineenOppimääränSuoritus2015 |
         _: LukionOppiaineidenOppimäärienSuoritus2019 |
         _: LukionOppimääränSuoritus2019 |
         _: EsiopetuksenSuoritus |
         _: NuortenPerusopetuksenOppiaineenOppimääränSuoritus |
         _: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus |
         _: EBTutkinnonSuoritus |
         _: EuropeanSchoolOfHelsinkiPäätasonSuoritus |
         _: TaiteenPerusopetuksenPäätasonSuoritus |
         _: IBPäätasonSuoritus => false
    case _ => true
  }

  private def oppilaitoksenTaiKoulutustoimijanOidMuuttunut(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    !(vanhaOpiskeluoikeus.oppilaitos.map(_.oid) == uusiOpiskeluoikeus.oppilaitos.map(_.oid) &&
      vanhaOpiskeluoikeus.koulutustoimija.map(_.oid) == uusiOpiskeluoikeus.koulutustoimija.map(_.oid))
  }
}
