package fi.oph.koski.omadataoauth2

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.servlet.KoskiSpecificApiServlet
import org.json4s.JString
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.compact

trait ConvertErrorsToOAuth2Format extends KoskiSpecificApiServlet {

  // Mäpätään mm. luovutuspalvelu-flowsta tulevat errorit OAuth2 formaatiksi
  override def renderStatus(status: HttpStatus): Unit = {
    response.setStatus(status.statusCode)
    val oAuthError = OAuth2ErrorResponse("invalid_client", Some(status.errors.map {
      _.message match {
        case JString(s) => s
        case j => compact(JsonMethods.render(j))
      }
    }.mkString(";")), None)
    render(oAuthError)
  }

}
