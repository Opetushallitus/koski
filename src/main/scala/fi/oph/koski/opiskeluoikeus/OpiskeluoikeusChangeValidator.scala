package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.validation.DateValidation.validateOpiskeluoikeudenPäivämäärät
import fi.oph.koski.validation.{AmmatillinenValidation, TaiteenPerusopetusValidation, TutkintokoulutukseenValmentavaKoulutusValidation}

import java.time.LocalDate

class OpiskeluoikeusChangeValidator(
  organisaatioRepository: OrganisaatioRepository,
  ePerusteetChangeValidator: EPerusteetOpiskeluoikeusChangeValidator,
  config: Config
) {
  val validaatioViimeinenPäiväEnnenVoimassaoloa = LocalDate.parse(config.getString("validaatiot.paivitetynOpiskeluoikeudenPaivamaarienValidaatioAstuuVoimaan")).minusDays(1)
  val päivitetynOpiskeluoikeudenPäivämäärienValidaatioAstunutVoimaan = LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)

  def validateOpiskeluoikeusChange(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    newState match {
      case _: YlioppilastutkinnonOpiskeluoikeus =>
        HttpStatus.ok
      case _ =>
        HttpStatus.fold(
          validateOpiskeluoikeudenTyypinMuutos(oldState, newState),
          validateLähdejärjestelmäIdnPoisto(oldState, newState),
          validateOppilaitoksenMuutos(oldState, newState),
          if (päivitetynOpiskeluoikeudenPäivämäärienValidaatioAstunutVoimaan) validateOpiskeluoikeudenPäivämäärät(newState) else HttpStatus.ok,
          ePerusteetChangeValidator.validateVanhanOpiskeluoikeudenTapaukset(oldState, newState),
          TutkintokoulutukseenValmentavaKoulutusValidation.validateJärjestämislupaEiMuuttunut(oldState, newState),
          TaiteenPerusopetusValidation.validateHankintakoulutusEiMuuttunut(oldState, newState),
          AmmatillinenValidation.validateKorotetunOpiskeluoikeudenLinkitysEiMuuttunut(oldState, newState)
        )
    }
  }

  def validateOpiskeluoikeudenTyypinMuutos(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (oldState.tyyppi.koodiarvo != newState.tyyppi.koodiarvo) {
      KoskiErrorCategory.forbidden.kiellettyMuutos(s"Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ${oldState.tyyppi.koodiarvo}. Uusi tyyppi ${newState.tyyppi.koodiarvo}.")
    } else {
      HttpStatus.ok
    }
  }

  def validateLähdejärjestelmäIdnPoisto(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    if (oldState.lähdejärjestelmänId.isDefined && newState.lähdejärjestelmänId.isEmpty && newState.lähdejärjestelmäkytkentäPurettu.isEmpty) {
      KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi poistaa.")
    } else {
      HttpStatus.ok
    }
  }

  private def validateOppilaitoksenMuutos(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {

    // Tässä listassa on väliaikaisesti sallittujen oppilaitosvaihdosten lähde- ja kohde -OID:t. Tuplena.
    // Lähde-OID --> kohde-OID
    val sallitutOppilaitosVaihdokset = List(
      (Some("1.2.246.562.10.63813695861"), Some("1.2.246.562.10.42923230215")),
      (Some("1.2.246.562.10.93428463247"), Some("1.2.246.562.10.77609835432")),
      (Some("1.2.246.562.10.21744269164"), Some("1.2.246.562.10.70112627842")),
      (Some("1.2.246.562.10.13857290038"), Some("1.2.246.562.10.38485743660")),
      (Some("1.2.246.562.10.82407007176"), Some("1.2.246.562.10.51885744782")),
      (Some("1.2.246.562.10.97089006874"), Some("1.2.246.562.10.22909222972")),
      (Some("1.2.246.562.10.77250993894"), Some("1.2.246.562.10.10779357598")),
      (Some("1.2.246.562.10.77250993894"), Some("1.2.246.562.10.29176843356")),
      (Some("1.2.246.562.10.77250993894"), Some("1.2.246.562.10.28263231921")),
      (Some("1.2.246.562.10.56312244082"), Some("1.2.246.562.10.58563612637")),
    )

    val uusiOppilaitos = uusiOpiskeluoikeus.oppilaitos.map(_.oid)
    val vanhaOppilaitos = vanhaOpiskeluoikeus.oppilaitos.map(_.oid)

    val oppilaitosPysynytSamana = uusiOppilaitos == vanhaOppilaitos

    val koulutustoimijaPysynytSamana = uusiOpiskeluoikeus.koulutustoimija.map(_.oid).exists(uusiOid => vanhaOpiskeluoikeus.koulutustoimija.map(_.oid).contains(uusiOid))
    val vanhaAktiivinen = vanhaOppilaitos.flatMap(oid => organisaatioRepository.getOrganisaatioHierarkia(oid).map(_.aktiivinen)).getOrElse(false)
    val uusiAktiivinen = uusiOppilaitos.flatMap(oid => organisaatioRepository.getOrganisaatioHierarkia(oid).map(_.aktiivinen)).getOrElse(false)
    val uusiOrganisaatioLöytyyOrganisaatioHistoriasta = vanhaOpiskeluoikeus.organisaatiohistoria.exists(_.exists(_.oppilaitos.exists(x => uusiOppilaitos.contains(x.oid))))
    val oppilaitoksenVaihtoSallittu = uusiOrganisaatioLöytyyOrganisaatioHistoriasta || (!vanhaAktiivinen && uusiAktiivinen)

    val oppilaitoksenVaihtoSallittuPoikkeustilanteessa = sallitutOppilaitosVaihdokset.exists(siirtymä => siirtymä._1 == vanhaOppilaitos && siirtymä._2 == uusiOppilaitos) && (uusiOrganisaatioLöytyyOrganisaatioHistoriasta || uusiAktiivinen)

    if (koulutustoimijaPysynytSamana && !oppilaitosPysynytSamana) {
      // Poikkeustilanteissa sallitaan siirto, jos vanha oppilaitos ei ole lakkautettu
      HttpStatus.validate(oppilaitoksenVaihtoSallittuPoikkeustilanteessa || oppilaitoksenVaihtoSallittu) {
        KoskiErrorCategory.badRequest.validation.organisaatio.oppilaitoksenVaihto()
      }
    } else {
      HttpStatus.ok
    }
  }
}
