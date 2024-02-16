package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import org.json4s.JValue

sealed trait QueryParameters {
  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean
  def asJson: JValue = JsonSerializer.serializeWithRoot(this)
}

// TODO: Väliaikainen testiparametri mahdollisimman yksinkertaiselle kyselylle
case class QueryHello(name: String) extends QueryParameters {
  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean = true
}

// TODO: Väliaikainen testiparametri mahdollisimman yksinkertaiselle kyselylle
case class QueryUnimplemteted(unimplemented: Boolean) extends QueryParameters {
  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean = true
}

// TODO: Väliaikainen testiparametri mahdollisimman yksinkertaiselle kyselylle
case class QueryNotAllowed(notAllowed: Boolean) extends QueryParameters {
  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean = false
}
