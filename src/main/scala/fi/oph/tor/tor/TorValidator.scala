package fi.oph.tor.tor

import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.schema.{OpiskeluOikeus, Suoritus, TorOppija}
import fi.oph.tor.tor.DateValidation._
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.tutkinto.{TutkintoRakenneValidator, TutkintoRepository}
import org.json4s.JValue

class TorValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoViitePalvelu, val organisaatioRepository: OrganisaatioRepository) {
  def validateAsJson(oppija: TorOppija)(implicit user: TorUser): Either[HttpStatus, TorOppija] = {
    extractAndValidate(Json.toJValue(oppija))
  }

  def extractAndValidate(parsedJson: JValue)(implicit user: TorUser): Either[HttpStatus, TorOppija] = {
    TorJsonSchemaValidator.jsonSchemaValidate(parsedJson) match {
      case status if status.isOk =>
        val extractionResult: Either[HttpStatus, TorOppija] = ValidatingAndResolvingExtractor.extract[TorOppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository))
        extractionResult.right.flatMap { oppija =>
          validateOpiskeluoikeudet(oppija.opiskeluoikeudet) match {
            case status if status.isOk => Right(oppija)
            case status => Left(status)
          }
        }
      case status => Left(status)
    }
  }

  private def validateOpiskeluoikeudet(opiskeluoikeudet: Seq[OpiskeluOikeus])(implicit user: TorUser): HttpStatus = {
    if (opiskeluoikeudet.length == 0) {
      TorErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista()
    }
    else {
      HttpStatus.fold(opiskeluoikeudet.map(validateOpiskeluOikeus))
    }
  }

  private def validateOpiskeluOikeus(opiskeluOikeus: OpiskeluOikeus)(implicit user: TorUser): HttpStatus = {
    HttpStatus.validate(user.userOrganisations.hasReadAccess(opiskeluOikeus.oppilaitos)) { TorErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .then { HttpStatus.fold(
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("päättymispäivä", opiskeluOikeus.päättymispäivä)),
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluOikeus.arvioituPäättymispäivä)),
      DateValidation.validateJaksot("opiskeluoikeudenTila.opiskeluoikeusjaksot", opiskeluOikeus.opiskeluoikeudenTila.toList.flatMap(_.opiskeluoikeusjaksot)),
      DateValidation.validateJaksot("läsnäolotiedot.läsnäolojaksot", opiskeluOikeus.läsnäolotiedot.toList.flatMap(_.läsnäolojaksot)),
      validateSuoritus(opiskeluOikeus.suoritus)
    )}
      .then { TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(opiskeluOikeus.suoritus)}
  }

  def validateSuoritus(suoritus: Suoritus): HttpStatus = {
    val arviointipäivä = ("suoritus.arviointi.päivä", suoritus.arviointi.toList.flatten.flatMap(_.päivä))
    HttpStatus.fold(
      validateDateOrder(("suoritus.alkamispäivä", suoritus.alkamispäivä), arviointipäivä)
        :: validateDateOrder(arviointipäivä, ("suoritus.vahvistus.päivä", suoritus.vahvistus.flatMap(_.päivä)))
        :: validateStatus(suoritus)
        :: suoritus.osasuoritukset.toList.flatten.map(validateSuoritus(_))
    )
  }

  private def validateStatus(suoritus: Suoritus): HttpStatus = {
    val hasArviointi: Boolean = !suoritus.arviointi.toList.flatten.isEmpty
    val hasVahvistus: Boolean = suoritus.vahvistus.isDefined
    val tilaValmis: Boolean = suoritus.tila.koodiarvo == "VALMIS"
    if (hasVahvistus && !tilaValmis) {
      TorErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa()
    } else if (!hasVahvistus && tilaValmis) {
      TorErrorCategory.badRequest.validation.tila.vahvistusPuuttuu()
    } else if (!hasArviointi && hasVahvistus) {
      TorErrorCategory.badRequest.validation.tila.vahvistusIlmanArviointia()
    } else {
      HttpStatus.ok
    }
  }
}
