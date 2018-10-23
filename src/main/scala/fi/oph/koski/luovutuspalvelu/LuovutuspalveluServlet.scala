package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.schema.{Henkilö, OpiskeluoikeudenTyyppi, Opiskeluoikeus, TäydellisetHenkilötiedot}
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.Timing
import org.json4s.JValue
import org.json4s.JsonAST.{JBool, JObject}
import org.scalatra.ContentEncodingSupport

case class HetuRequestV1(v: Int, hetu: String, opiskeluoikeudenTyypit: List[String], käyttötarkoitus: Option[String])

case class HetuResponseV1(henkilö: LuovutuspalveluHenkilöV1, opiskeluoikeudet: Seq[Opiskeluoikeus])

case class LuovutuspalveluHenkilöV1(oid: Henkilö.Oid, hetu: Option[Henkilö.Hetu], syntymäaika: Option[LocalDate],  turvakielto: Boolean)

class LuovutuspalveluServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresLuovutuspalvelu with ContentEncodingSupport with NoCache with Timing {

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

  private def parseHetuRequestV1(parsedJson: JValue): Either[HttpStatus, HetuRequestV1] = {
    JsonSerializer.validateAndExtract[HetuRequestV1](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.v == 1, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      .flatMap(req => Hetu.validFormat(req.hetu).map(_ => req))
      .flatMap(req => {
        val tuntematonTyyppi = req.opiskeluoikeudenTyypit.find(t => application.koodistoViitePalvelu.validate("opiskeluoikeudentyyppi", t).isEmpty)
        if (tuntematonTyyppi.isDefined) {
          Left(KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudenTyyppi"))
        } else {
          Right(req)
        }
      })
  }

  private def buildLuovutuspalveluHenkilöV1(henkilö: Henkilö): LuovutuspalveluHenkilöV1 = {
    henkilö match {
      case th: TäydellisetHenkilötiedot => LuovutuspalveluHenkilöV1(th.oid, th.hetu, th.syntymäaika, th.turvakielto.getOrElse(false))
      case _ => throw new RuntimeException("expected TäydellisetHenkilötiedot")
    }
  }
}
