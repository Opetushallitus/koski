package fi.oph.koski.migri

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser._
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

class MigriServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresLuovutuspalvelu with NoCache {
  lazy val migriService =
    if (application.config.getString("opintopolku.virkailija.url") == "mock") new MockMigriService else new RemoteMigriService

  post("/hetu") {
    withJsonBody{ json =>
      renderEither(extractAndValidateHetu(json).flatMap(haeHetulla))
    }()
  }

  post("/oid") {
    withJsonBody { json =>
      renderEither(extractAndValidateOid(json).flatMap(haeOidilla))
    }()
  }

  post("/valinta/oid") {
    withJsonBody { json =>
      renderEither(extractAndValidateOid(json).flatMap(valintaTiedotOidilla))
    }()
  }

  private def extractAndValidateHetu(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[MigriHetuRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))

  private def extractAndValidateOid(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[MigriOidRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => HenkilöOid.validateHenkilöOid(req.oid))

  private def haeHetulla(hetu: String): Either[HttpStatus, MigriOppija] =
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = true, useYtr = true)(koskiSession)
      .flatMap(_.warningsToLeft)
      .flatMap(convertToMigriSchema)

  private def haeOidilla(oid: String): Either[HttpStatus, MigriOppija] =
    application.oppijaFacade.findOppija(oid, findMasterIfSlaveOid = true, useVirta = true, useYtr = true)(koskiSession)
      .flatMap(_.warningsToLeft)
      .flatMap(convertToMigriSchema)

  private def valintaTiedotOidilla(oid: String): Either[HttpStatus, String] = {
    val basicAuthRequest = new BasicAuthRequest(request)
    migriService.get(oid, basicAuthRequest)
  }

  private def convertToMigriSchema(oppija: Oppija): Either[HttpStatus, MigriOppija] =
    ConvertMigriSchema.convert(oppija).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
}

case class MigriHetuRequest(hetu: String)

case class MigriOidRequest(oid: String)

