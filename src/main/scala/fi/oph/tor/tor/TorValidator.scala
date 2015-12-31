package fi.oph.tor.tor

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.KoodistoPalvelu
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.schema.{OpiskeluOikeus, Suoritus, TorOppija}
import fi.oph.tor.tor.DateValidation._
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.tutkinto.{TutkintoRakenneValidator, TutkintoRepository}
import org.json4s.JValue

class TorValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoPalvelu, val organisaatioRepository: OrganisaatioRepository) {
  def validate(oppija: TorOppija)(implicit userContext: TorUser): HttpStatus = {
    if (oppija.opiskeluoikeudet.length == 0) {
      HttpStatus.badRequest("At least one OpiskeluOikeus required")
    }
    else {
      HttpStatus.fold(oppija.opiskeluoikeudet.map(validateOpiskeluOikeus))
    }
  }

  def extractAndValidate(parsedJson: JValue)(implicit userContext: TorUser): Either[HttpStatus, TorOppija] = {
    TorJsonSchemaValidator.jsonSchemaValidate(parsedJson) match {
      case status if status.isOk =>
        val extractionResult: Either[HttpStatus, TorOppija] = ValidatingAndResolvingExtractor.extract[TorOppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository))
        extractionResult.right.flatMap { oppija =>
          validate(oppija) match {
            case status if status.isOk => Right(oppija)
            case status => Left(status)
          }
        }
      case status => Left(status)
    }
  }

  private def validateOpiskeluOikeus(opiskeluOikeus: OpiskeluOikeus)(implicit userContext: TorUser): HttpStatus = {
    HttpStatus.validate(userContext.userOrganisations.hasReadAccess(opiskeluOikeus.oppilaitos)) { HttpStatus.forbidden("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .then { HttpStatus.fold(
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("päättymispäivä", opiskeluOikeus.päättymispäivä)),
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluOikeus.arvioituPäättymispäivä)),
      DateValidation.validateJaksot("opiskeluoikeudenTila.opiskeluoikeusjaksot", opiskeluOikeus.opiskeluoikeudenTila.toList.flatMap(_.opiskeluoikeusjaksot)),
      DateValidation.validateJaksot("läsnäolotiedot.läsnäolojaksot", opiskeluOikeus.läsnäolotiedot.toList.flatMap(_.läsnäolojaksot)),
      validateSuoritus(opiskeluOikeus.suoritus)
    )}
      .then { TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(opiskeluOikeus)}
  }

  def validateSuoritus(suoritus: Suoritus): HttpStatus = {
    val arviointipäivä = ("suoritus.arviointi.päivä", suoritus.arviointi.toList.flatten.flatMap(_.päivä))
    HttpStatus.fold(
      validateDateOrder(("suoritus.alkamispäivä", suoritus.alkamispäivä), arviointipäivä),
      validateDateOrder(arviointipäivä, ("suoritus.vahvistus.päivä", suoritus.vahvistus.flatMap(_.päivä)))
    )
  }
}
