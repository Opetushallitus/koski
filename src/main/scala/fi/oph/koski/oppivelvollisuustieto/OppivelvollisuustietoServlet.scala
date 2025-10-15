package fi.oph.koski.oppivelvollisuustieto

import java.time.LocalDate
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, Palvelurooli, Rooli}
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

class OppivelvollisuustietoServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiCookieAndBasicAuthenticationSupport with NoCache {

  before() {
    requirePalvelurooli(Palvelurooli(Rooli.OPPIVELVOLLISUUSTIETO_RAJAPINTA))
  }

  val MAX_OIDS = 10000

  post("/oids") {
    withJsonBody { json =>
      val response = for {
        oids <- extractAndValidate(json)
        oppivelvollisuustiedot <- queryByOids(oids)
      } yield oppivelvollisuustiedot

      renderEither(response)
    }()
  }

  private def extractAndValidate(json: JValue) = {
    JsonSerializer.validateAndExtract[List[String]](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(validateRequestSize)
      .flatMap(oids => HttpStatus.foldEithers(oids.map(validateOid)))
  }

  private def validateRequestSize(oids: List[String]): Either[HttpStatus, List[String]] =
    if (oids.length > MAX_OIDS) Left(KoskiErrorCategory.badRequest(s"Rajapinnasta ei voi hakea yli $MAX_OIDS oidia")) else Right(oids)

  private def validateOid(oid: String): Either[HttpStatus, Oid] =
    if (Henkilö.isValidHenkilöOid(oid)) Right(oid) else Left(HttpStatus.ok)

  private def queryByOids(oids: Seq[String]): Either[HttpStatus, Seq[Oppivelvollisuustieto]] = {
    Right(Oppivelvollisuustiedot.queryByOids(oids, application.raportointiDatabase))
  }
}

