package fi.oph.koski.migri

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser._
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, RawJsonResponse}
import org.json4s.JValue
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest

class MigriServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresMigri with NoCache {
  lazy val migriService =
    if (Environment.isMockEnvironment(application.config)) new MockMigriService else new RemoteMigriService

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
      renderEither(extractAndValidateOids(json).flatMap(valintaTiedotOideilla))
    }()
  }

  post("/valinta/hetut") {
    withJsonBody { json =>
      renderEither(extractAndValidateHetus(json).flatMap(valintaTiedotHetuilla))
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

  private def extractAndValidateOids(json: JValue): Either[HttpStatus, List[String]] =
    JsonSerializer.validateAndExtract[MigriOidsRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .map(req => req.oids)

  private def extractAndValidateHetus(json: JValue): Either[HttpStatus, List[String]] =
    JsonSerializer.validateAndExtract[MigriHetusRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .map(req => req.hetut)

  private def haeHetulla(hetu: String): Either[HttpStatus, MigriOppija] =
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = true, useYtr = true)(koskiSession)
      .flatMap(_.warningsToLeft)
      .flatMap(convertToMigriSchema)

  private def haeOidilla(oid: String): Either[HttpStatus, MigriOppija] =
    application.oppijaFacade.findOppija(oid, findMasterIfSlaveOid = true, useVirta = true, useYtr = true)(koskiSession)
      .flatMap(_.warningsToLeft)
      .flatMap(convertToMigriSchema)

  private def valintaTiedotOideilla(oids: List[String]): Either[HttpStatus, RawJsonResponse] = {
    val basicAuthRequest = new BasicAuthRequest(request)
    migriService.getByOids(oids, basicAuthRequest)
  }

  private def valintaTiedotHetuilla(hetut: List[String]): Either[HttpStatus, RawJsonResponse] = {
    val basicAuthRequest = new BasicAuthRequest(request)
    migriService.getByHetus(hetut, basicAuthRequest)
  }

  private def convertToMigriSchema(oppija: Oppija): Either[HttpStatus, MigriOppija] =
    ConvertMigriSchema.convert(oppija).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
}

case class MigriHetuRequest(hetu: String)

case class MigriOidRequest(oid: String)

case class MigriOidsRequest(oids: List[String])

case class MigriHetusRequest(hetut: List[String])
