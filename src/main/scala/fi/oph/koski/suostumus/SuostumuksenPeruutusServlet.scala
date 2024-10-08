package fi.oph.koski.suostumus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.KoskiAuditLogMessageField.{apply => _}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JsonAST.{JBool, JNothing, JObject, JString}
import org.json4s.JField

class SuostumuksenPeruutusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with Logging with NoCache {

  def session: KoskiSpecificSession = koskiSessionOption.get

  post("/:oid") {
    requireKansalainen
    renderStatus(
      application.suostumuksenPeruutusService.peruutaSuostumus(
        oid = getStringParam("oid"),
        suorituksenTyyppi = getOptionalStringParam("suorituksentyyppi")
      )(session)
    )
  }

  post("/suoritusjakoTehty/:oid") {
    requireKansalainen
    if (application.suostumuksenPeruutusService.suoritusjakoTekemättäWithAccessCheck(
      oid = getStringParam("oid"),
      suorituksenTyyppi = getOptionalStringParam("suorituksentyyppi")
    )(session).isOk) {
      renderObject(JObject("tehty" -> JBool(false)))
    } else {
      renderObject(JObject("tehty" -> JBool(true)))
    }
  }

  // Tätä rajapintaa ei käytetä käyttöliittymästä, mutta se mahdollistaa asiantuntijoiden tarkastaa opiskeluoikeuden
  // poiston syyn selviteltäessä ongelmatilanteita. Tämä palauttaa tarkoituksella myös mitätöidyt opiskeluoikeudet.
  get("/") {
    requireVirkailijaOrPalvelukäyttäjä
    if (session.hasGlobalReadAccess) {
      val peruutetutSuostumukset = application.suostumuksenPeruutusService.listaaPerututSuostumukset(palautaMyösMitätöidyt = true)
      if (peruutetutSuostumukset.nonEmpty) {
        renderObject(
          peruutetutSuostumukset.map(peruttuOo =>
            JObject(
              List(
                Some(JField("Opiskeluoikeuden oid", JString(peruttuOo.oid))),
                Some(JField("Oppijan oid", JString(peruttuOo.oppijaOid))),
                Some(JField("Opiskeluoikeuden päättymispäivä", JString(peruttuOo.päättymispäivä.getOrElse("").toString))),
                peruttuOo.suostumusPeruttuAikaleima.map(sp => JField("Suostumus peruttu", JString(sp.toString))),
                peruttuOo.mitätöityAikaleima.map(m => JField("Mitätöity", JString(m.toString))),
                Some(JField("Oppilaitoksen oid", JString(peruttuOo.oppilaitosOid.getOrElse("")))),
                Some(JField("Oppilaitoksen nimi", JString(peruttuOo.oppilaitosNimi.getOrElse("")))),
                Some(JField("Suoritusten tyypit", JString(peruttuOo.suoritustyypit.mkString(", "))))
              ).flatten
            )
          )
        )
      } else {
        renderObject(JObject())
      }
    } else {
      KoskiErrorCategory.forbidden.vainVirkailija()
    }
  }

  get("/testimerkinta") {
    requireVirkailijaOrPalvelukäyttäjä
    application.suostumuksenPeruutusService.teeTestimerkintäSähköpostinotifikaatiotaVarten()
    renderObject(JObject("testimerkintä" -> JBool(true)))
  }
}
