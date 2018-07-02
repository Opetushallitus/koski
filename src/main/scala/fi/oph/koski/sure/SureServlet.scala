package fi.oph.koski.sure

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, SensitiveDataFilter}
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.OppijaOidHaku
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueries, OpiskeluoikeusQueryContext}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.Timing
import org.json4s.JValue
import org.scalatra.ContentEncodingSupport

class SureServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache with Timing {

  // palauttaa annettujen oppija-oidien kaikki (Koskeen tallennetut, ei mitätöidyt) opiskeluoikeudet.
  // mikäli jollekin OIDille ei löydy yhtään opiskeluoikeutta, tämä ei ole virhe (ja ko, OID puuttuu vastauksesta)
  post("/oids") {
    val MaxOids = 1000
    withJsonBody { parsedJson =>
      val oids = JsonSerializer.extract[List[String]](parsedJson)
      if (oids.size > MaxOids) {
        haltWithStatus(KoskiErrorCategory.badRequest.queryParam(s"Liian monta oidia, enintään $MaxOids sallittu."))
      }
      oids.map(HenkilöOid.validateHenkilöOid).collectFirst { case Left(status) => status } match {
        case None =>
          val serialize = SensitiveDataFilter(koskiSession).rowSerializer
          val observable = OpiskeluoikeusQueryContext(request)(koskiSession, application).queryWithoutHenkilötiedotRaw(
            List(OppijaOidHaku(oids)), None, "oids=" + oids.take(2).mkString(",") + ",...(" + oids.size + ")"
          )
          streamResponse[JValue](observable.map(t => serialize(OidHenkilö(t._1), t._2)), koskiSession)
        case Some(status) => haltWithStatus(status)
      }
    }()
  }
}
