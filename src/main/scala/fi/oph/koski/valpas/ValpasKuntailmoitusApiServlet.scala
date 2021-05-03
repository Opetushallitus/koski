package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedot, ValpasKuntailmoitusLaajatTiedotJaOppijaOid}
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s._

class ValpasKuntailmoitusApiServlet(implicit val application: KoskiApplication)
  extends ValpasApiServlet
    with NoCache
    with RequiresValpasSession {

  private lazy val koodistoViitePalvelu = application.koodistoViitePalvelu
  private lazy val organisaatioRepository = application.organisaatioRepository
  private lazy val kuntailmoitusValidator = application.valpasKuntailmoitusInputValidator
  private lazy val kuntailmoitusService = application.valpasKuntailmoitusService

  post("/") {
    withJsonBody { (kuntailmoitusInputJson: JValue) =>
      val result: Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] =
        extractAndValidateKuntailmoitus(kuntailmoitusInputJson)
          .flatMap(kuntailmoitusService.createKuntailmoitus)
      renderEither[ValpasKuntailmoitusLaajatTiedot](result)
    }(parseErrorHandler = handleUnparseableJson)
  }

  private def extractAndValidateKuntailmoitus(kuntailmoitusInputJson: JValue) = {
    application.validatingAndResolvingExtractor.extract[ValpasKuntailmoitusLaajatTiedotJaOppijaOid](kuntailmoitusInputJson)
      .flatMap(kuntailmoitusInput =>
        Either.cond(
          kuntailmoitusInput.kuntailmoitus.id.isEmpty,
          kuntailmoitusInput,
          ValpasErrorCategory.notImplemented.kuntailmoituksenMuokkaus()
        )
      )
      .flatMap(kuntailmoitusValidator.validateKuntailmoitusInput)
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    haltWithStatus(status)
  }
}
