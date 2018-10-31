package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.{Hetu, OppijaHenkilö}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.schema.{Henkilö, OpiskeluoikeudenTyyppi, Opiskeluoikeus, TäydellisetHenkilötiedot}
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.AuditLogMessage
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.OppijaOidHaku
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.util.Timing
import org.json4s.JValue
import org.json4s.JsonAST.{JBool, JObject}
import org.scalatra.ContentEncodingSupport

case class HetuRequestV1(v: Int, hetu: String, opiskeluoikeudenTyypit: List[String], käyttötarkoitus: Option[String])

case class HetuResponseV1(henkilö: LuovutuspalveluHenkilöV1, opiskeluoikeudet: Seq[Opiskeluoikeus])

case class LuovutuspalveluHenkilöV1(oid: Henkilö.Oid, hetu: Option[Henkilö.Hetu], syntymäaika: Option[LocalDate],  turvakielto: Boolean)

case class BulkHetuRequestV1(v: Int, hetut: List[String], opiskeluoikeudenTyypit: List[String], käyttötarkoitus: Option[String])

class LuovutuspalveluServlet(implicit val application: KoskiApplication) extends ApiServlet with ObservableSupport with RequiresLuovutuspalvelu with ContentEncodingSupport with NoCache with Timing {

  before() {
    // Tämä koodi ei ole vielä tuotantokelpoista.
    if (!application.features.luovutuspalvelu) {
      haltWithStatus(KoskiErrorCategory.badRequest("Luovutuspalvelu-rajapinta ei käytössä tässä ympäristössä."))
    }
  }

  get("/healthcheck") {
    renderObject(JObject("ok" -> JBool(true)))
  }

  post("/hetu") {
    withJsonBody { parsedJson =>
      // optimointi: tee Virta/YTR kutsut vain jos ko. tietoja tarvitaan (request.opiskeluoikeudenTyypit)
      val response = for {
        request <- parseHetuRequestV1(parsedJson)
        useVirta = request.opiskeluoikeudenTyypit.contains(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
        useYtr = request.opiskeluoikeudenTyypit.contains(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
        oppijaWithWarnings <- application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(request.hetu, useVirta = useVirta, useYtr = useYtr)
        oppija <- oppijaWithWarnings.warningsToLeft
        palautettavatOpiskeluoikeudet = oppija.opiskeluoikeudet.filter(oo => request.opiskeluoikeudenTyypit.contains(oo.tyyppi.koodiarvo))
        _ <- if (palautettavatOpiskeluoikeudet.isEmpty) Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()) else Right()
      } yield {
        HetuResponseV1(
          henkilö = buildLuovutuspalveluHenkilöV1(oppija.henkilö),
          opiskeluoikeudet = oppija.opiskeluoikeudet.filter(oo => request.opiskeluoikeudenTyypit.contains(oo.tyyppi.koodiarvo))
        )
      }
      renderEither(response)
    }()
  }

  post("/hetut") {
    withJsonBody { parsedJson =>
      parseBulkHetuRequestV1(parsedJson) match {
        case Left(status) => haltWithStatus(status)
        case Right(req) => {
          val henkilot = application.opintopolkuHenkilöFacade.findOppijatByHetus(req.hetut)
          val oids = henkilot.map(_.toHenkilötiedotJaOid.oid)
          val oidToHenkilo = henkilot.map(h => h.oid -> h).toMap
          val _koskiSession = koskiSession // take current session so it can be used in observable
          val auditLogMessages = oids.map(o => AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(oppijaHenkiloOid -> o)))
          val observable = OpiskeluoikeusQueryContext(request)(koskiSession, application).queryWithDistinctLoggingMsg(
            OppijaOidHaku(oids) :: opiskeluoikeusTyyppiQueryFilters(req.opiskeluoikeudenTyypit), None, auditLogMessages
          )
          streamResponse[JValue](observable.map(t => JsonSerializer.serializeWithUser(_koskiSession)(buildHetutResponseV1(oidToHenkilo(t._1), t._2))), koskiSession)
        }
      }
    }()
  }

  private def parseHetuRequestV1(parsedJson: JValue): Either[HttpStatus, HetuRequestV1] = {
    JsonSerializer.validateAndExtract[HetuRequestV1](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.v == 1, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      .flatMap(req => Hetu.validFormat(req.hetu).map(_ => req))
      .flatMap(req => validateOpiskeluoikeudenTyypit(req.opiskeluoikeudenTyypit, allowVirtaOrYtr = true).map(_ => req))
  }

  private def parseBulkHetuRequestV1(parsedJson: JValue): Either[HttpStatus, BulkHetuRequestV1] = {
    val MaxHetus = 1000
    JsonSerializer.validateAndExtract[BulkHetuRequestV1](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.v == 1, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      .flatMap(req => validateOpiskeluoikeudenTyypit(req.opiskeluoikeudenTyypit, allowVirtaOrYtr = false).map(_ => req))
      .filterOrElse(_.hetut.length <= MaxHetus, KoskiErrorCategory.badRequest.queryParam(s"Liian monta hetua, enintään $MaxHetus sallittu"))
      .flatMap(req => {
        req.hetut.map(Hetu.validFormat).collectFirst { case Left(status) => status } match {
          case Some(status) => Left(status)
          case None => Right(req)
        }
      })
  }

  private def buildLuovutuspalveluHenkilöV1(henkilö: Henkilö): LuovutuspalveluHenkilöV1 = {
    henkilö match {
      case th: TäydellisetHenkilötiedot => LuovutuspalveluHenkilöV1(th.oid, th.hetu, th.syntymäaika, th.turvakielto.getOrElse(false))
      case _ => throw new RuntimeException("expected TäydellisetHenkilötiedot")
    }
  }

  private def buildHetutResponseV1(h: OppijaHenkilö, oo: List[OpiskeluoikeusRow]): HetuResponseV1 =
    HetuResponseV1(
      LuovutuspalveluHenkilöV1(h.oid, h.hetu, h.syntymäaika, h.turvakielto),
      oo.map(_.toOpiskeluoikeus))

  private def opiskeluoikeusTyyppiQueryFilters(opiskeluoikeusTyypit: List[String]): List[OpiskeluoikeusQueryFilter] =
    opiskeluoikeusTyypit.map(t => OpiskeluoikeusQueryFilter.OpiskeluoikeudenTyyppi(Koodistokoodiviite(t, "opiskeluoikeudentyyppi")))

  private def validateOpiskeluoikeudenTyypit(tyypit: List[String], allowVirtaOrYtr: Boolean): Either[HttpStatus, List[String]] = {
    val virtaYtrTyypit = List("korkeakoulutus", "ylioppilastutkinto")
    if (tyypit.isEmpty) {
      Left(KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
    } else if (!tyypit.forall(application.koodistoViitePalvelu.validate("opiskeluoikeudentyyppi", _).isDefined)) {
      Left(KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
    } else if (!allowVirtaOrYtr && tyypit.exists(virtaYtrTyypit.contains(_))) {
      Left(KoskiErrorCategory.badRequest.queryParam("Korkeakoulutus tai ylioppilastutkinto ei sallittu"))
    } else {
      Right(tyypit)
    }
  }
}
