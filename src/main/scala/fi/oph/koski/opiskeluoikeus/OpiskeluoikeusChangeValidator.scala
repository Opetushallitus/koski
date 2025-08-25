package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.eperusteetvalidation.EPerusteetOpiskeluoikeusChangeValidator
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.validation.DateValidation.validateOpiskeluoikeudenPäivämäärät
import fi.oph.koski.validation.{AmmatillinenValidation, TaiteenPerusopetusValidation, TutkintokoulutukseenValmentavaKoulutusValidation}
import fi.oph.koski.validation.LukionYhteisetValidaatiot.validateLukioJaAineopiskeluVaihto

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
          validateLukioJaAineopiskeluVaihto(oldState, newState),
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
      (Some("1.2.246.562.10.22398224919"), Some("1.2.246.562.10.16782894675")),
      (Some("1.2.246.562.10.320189452810"), Some("1.2.246.562.10.89001280935")),
      (Some("1.2.246.562.10.26156970159"), Some("1.2.246.562.10.23963734209")),
      (Some("1.2.246.562.10.22398224919"), Some("1.2.246.562.10.71855763059")),
      (Some("1.2.246.562.10.62076807171"), Some("1.2.246.562.10.24267102812")),
      (Some("1.2.246.562.10.66183733188"), Some("1.2.246.562.10.87820912514")),
      (Some("1.2.246.562.10.69625324038"), Some("1.2.246.562.10.57399459684")),
      (Some("1.2.246.562.10.59994895338"), Some("1.2.246.562.10.39018049740")),
      (Some("1.2.246.562.10.35533982381"), Some("1.2.246.562.10.64807240573")),
      (Some("1.2.246.562.10.2013080913404603081850"), Some("1.2.246.562.10.14528925440")),
      (Some("1.2.246.562.10.46842905385"), Some("1.2.246.562.10.64574186728")),
      (Some("1.2.246.562.10.59994895338"), Some("1.2.246.562.10.74719726361")),
      (Some("1.2.246.562.10.59994895338"), Some("1.2.246.562.10.73902641445")),
      (Some("1.2.246.562.10.22398224919"), Some("1.2.246.562.10.30106605360")),
      (Some("1.2.246.562.10.320189452810"), Some("1.2.246.562.10.93708633016")),
      (Some("1.2.246.562.10.92406966561"), Some("1.2.246.562.10.25187661175")),
      (Some("1.2.246.562.10.37172352199"), Some("1.2.246.562.10.95555572858")),
      (Some("1.2.246.562.10.89596178528"), Some("1.2.246.562.10.98274200892")),
      (Some("1.2.246.562.10.79971426246"), Some("1.2.246.562.10.78134171251")),
      (Some("1.2.246.562.10.34286696223"), Some("1.2.246.562.10.53366135829")),
      (Some("1.2.246.562.10.65736873723"), Some("1.2.246.562.10.17601303971")),
      (Some("1.2.246.562.10.69056688034"), Some("1.2.246.562.10.14400410743")),
      (Some("1.2.246.562.10.91418769674"), Some("1.2.246.562.10.14618519442")),
      (Some("1.2.246.562.10.26153243592"), Some("1.2.246.562.10.34245615793")),
      (Some("1.2.246.562.10.489856636810"), Some("1.2.246.562.10.59824586222")),
      (Some("1.2.246.562.10.320189452810"), Some("1.2.246.562.10.30902432985")),
      (Some("1.2.246.562.10.25829828331"), Some("1.2.246.562.10.83569595750")),
      (Some("1.2.246.562.10.12170066091"), Some("1.2.246.562.10.81351508031")),
      (Some("1.2.246.562.10.59133532776"), Some("1.2.246.562.10.34887305646")),
      (Some("1.2.246.562.10.62076807171"), Some("1.2.246.562.10.19243436128")),
      (Some("1.2.246.562.10.13554462898"), Some("1.2.246.562.10.89674817450")),
      (Some("1.2.246.562.10.13554462898"), Some("1.2.246.562.10.98164106845")),
      (Some("1.2.246.562.10.79971426246"), Some("1.2.246.562.10.93901970702")),
      (Some("1.2.246.562.10.35533982381"), Some("1.2.246.562.10.52279968649")),
      (Some("1.2.246.562.10.66183733188"), Some("1.2.246.562.10.37521176349")),
      (Some("1.2.246.562.10.48706566725"), Some("1.2.246.562.10.13835271456")),
      (Some("1.2.246.562.10.79971426246"), Some("1.2.246.562.10.52779770461")),
      (Some("1.2.246.562.10.41140013131"), Some("1.2.246.562.10.68040632413")),
      (Some("1.2.246.562.10.2013080913404603081850"), Some("1.2.246.562.10.26973562395")),
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
