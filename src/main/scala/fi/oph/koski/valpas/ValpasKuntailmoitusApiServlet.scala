package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.{AuditLog, KoskiMessageField}
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedot, ValpasKuntailmoitusLaajatTiedotJaOppijaOid}
import fi.oph.koski.valpas.valpasuser.{RequiresValpasSession, ValpasSession}
import org.json4s._

class ValpasKuntailmoitusApiServlet(implicit val application: KoskiApplication)
  extends ValpasApiServlet
    with NoCache
    with RequiresValpasSession {

  private lazy val kuntailmoitusValidator = new ValpasKuntailmoitusInputValidator(application)
  private lazy val kuntailmoitusService = new ValpasKuntailmoitusService(application)

  post("/") {
    withJsonBody { (kuntailmoitusInputJson: JValue) =>
      val result: Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] =
        extractAndValidateKuntailmoitus(kuntailmoitusInputJson)
          .flatMap(kuntailmoitusService.createKuntailmoitus)
          .map(withAuditLogOppijaKuntailmoitus)
          .map(_.kuntailmoitus)
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

  private def withAuditLogOppijaKuntailmoitus
    (result: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
    (implicit session: ValpasSession)
  : ValpasKuntailmoitusLaajatTiedotJaOppijaOid = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KUNTAILMOITUS,
      // TODO: pitäisikö olla muutakin dataa kuin oppijan oid? Ts. pitäisikö auditlogista näkyä,
      //  että mikä oppilaitos/kunta on tehnyt ilmoituksen mihin kuntaan?
      Map(KoskiMessageField.oppijaHenkiloOid -> result.oppijaOid)
    ))
    result
  }
}
