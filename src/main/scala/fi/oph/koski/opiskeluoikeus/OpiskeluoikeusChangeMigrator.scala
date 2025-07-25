package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.schema._


object OpiskeluoikeusChangeMigrator {
  def migrate(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowDeleteCompleted: Boolean): KoskeenTallennettavaOpiskeluoikeus = {
    uusiOpiskeluoikeus match {
      case _: YlioppilastutkinnonOpiskeluoikeus =>
        uusiOpiskeluoikeus
      case _ if uusiOpiskeluoikeus.mitätöity =>
        // Jos uusi opiskeluoikeus ollaan mitätöimässä, jätetään huomiotta kaikki muut muutokset, joita ollaan mahdollisesti tekemässä
        vanhaOpiskeluoikeus.invalidated(uusiOpiskeluoikeus.mitätöintiPäivä.get)
      case _ =>
        val uusiOpiskeluoikeusSuorituksilla = if (allowDeleteCompleted) uusiOpiskeluoikeus else kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)
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
