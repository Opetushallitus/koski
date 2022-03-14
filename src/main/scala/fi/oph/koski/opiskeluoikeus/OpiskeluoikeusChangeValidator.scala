package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.validation.{AmmatillinenValidation, TutkintokoulutukseenValmentavaKoulutusValidation}

class OpiskeluoikeusChangeValidator(organisaatioRepository: OrganisaatioRepository, ePerusteet: EPerusteetRepository) {
  def validateOpiskeluoikeusChange(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    HttpStatus.fold(
      validateOpiskeluoikeudenTyypinMuutos(oldState, newState),
      validateLähdejärjestelmäIdnPoisto(oldState, newState),
      validateOppilaitoksenMuutos(oldState, newState),
      AmmatillinenValidation.validateVanhanOpiskeluoikeudenTapaukset(oldState, newState, ePerusteet),
      TutkintokoulutukseenValmentavaKoulutusValidation.validateJärjestämislupaEiMuuttunut(oldState, newState)
    )
  }

  def validateOpiskeluoikeudenTyypinMuutos(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (oldState.tyyppi.koodiarvo != newState.tyyppi.koodiarvo) {
      KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ${oldState.tyyppi.koodiarvo}. Uusi tyyppi ${newState.tyyppi.koodiarvo}.")
    } else {
      HttpStatus.ok
    }
  }

  def validateLähdejärjestelmäIdnPoisto(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (oldState.lähdejärjestelmänId.isDefined && newState.lähdejärjestelmänId.isEmpty) {
      KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi poistaa.")
    } else {
      HttpStatus.ok
    }
  }

  private def validateOppilaitoksenMuutos(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val uusiOppilaitos = uusiOpiskeluoikeus.oppilaitos.map(_.oid)
    val vanhaOppilaitos = vanhaOpiskeluoikeus.oppilaitos.map(_.oid)
    val koulutustoimijaPysynytSamana = uusiOpiskeluoikeus.koulutustoimija.map(_.oid).exists(uusiOid => vanhaOpiskeluoikeus.koulutustoimija.map(_.oid).contains(uusiOid))
    val vanhaAktiivinen = vanhaOppilaitos.flatMap(oid => organisaatioRepository.getOrganisaatioHierarkia(oid).map(_.aktiivinen)).getOrElse(false)
    val uusiAktiivinen = uusiOppilaitos.flatMap(oid => organisaatioRepository.getOrganisaatioHierarkia(oid).map(_.aktiivinen)).getOrElse(false)
    val uusiOrganisaatioLöytyyOrganisaatioHistoriasta = vanhaOpiskeluoikeus.organisaatiohistoria.exists(_.exists(_.oppilaitos.exists(x => uusiOppilaitos.contains(x.oid))))
    val oppilaitoksenVaihtoSallittu = uusiOrganisaatioLöytyyOrganisaatioHistoriasta || (!vanhaAktiivinen && uusiAktiivinen)

    if (koulutustoimijaPysynytSamana && uusiOppilaitos != vanhaOppilaitos) {
      HttpStatus.validate(oppilaitoksenVaihtoSallittu) { KoskiErrorCategory.badRequest.validation.organisaatio.oppilaitoksenVaihto()}
    } else {
      HttpStatus.ok
    }
  }
}
