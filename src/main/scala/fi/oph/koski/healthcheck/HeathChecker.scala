package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiUser._

case class HeathChecker(application: KoskiApplication) {
  def healthcheck = application.facade.findOppija(application.config.getString("healthcheck.oppija.oid"))(systemUser)
}
