package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.schema._


object OpiskeluoikeusChangeMigrator {
  def migrate(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowDeleteCompleted: Boolean): KoskeenTallennettavaOpiskeluoikeus = {
    val uusiOpiskeluoikeusSuorituksilla = if (allowDeleteCompleted) uusiOpiskeluoikeus else kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)
    organisaationMuutosHistoria(vanhaOpiskeluoikeus, uusiOpiskeluoikeusSuorituksilla)
  }

  private def kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
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
      val vanhaHistoria = vanhaOpiskeluoikeus.organisaatioHistoria.toList.flatten
      val muutos = OpiskeluoikeudenOrganisaatioHistoria(LocalDate.now(), vanhaOpiskeluoikeus.oppilaitos.get, vanhaOpiskeluoikeus.koulutustoimija.get)
      uusiOpiskeluoikeus.withHistoria(Some(vanhaHistoria :+ muutos))
    } else {
      uusiOpiskeluoikeus.withHistoria(vanhaOpiskeluoikeus.organisaatioHistoria)
    }
  }

  private def kopioitavaPäätasonSuoritus(suoritus: KoskeenTallennettavaPäätasonSuoritus) = suoritus match {
    case _: LukionOppiaineenOppimääränSuoritus |
         _: NuortenPerusopetuksenOppiaineenOppimääränSuoritus |
         _: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus => false
    case _ => true
  }

  private def oppilaitoksenTaiKoulutustoimijanOidMuuttunut(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    !(vanhaOpiskeluoikeus.oppilaitos.get.oid == uusiOpiskeluoikeus.oppilaitos.get.oid &&
      vanhaOpiskeluoikeus.koulutustoimija.get.oid == uusiOpiskeluoikeus.koulutustoimija.get.oid)
  }
}
