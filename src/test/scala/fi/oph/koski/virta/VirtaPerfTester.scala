package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.MockUsers

object VirtaPerfTester extends App {
  private val app: KoskiApplication = KoskiApplication.apply
  implicit val user = MockUsers.kalle.toKoskiSpecificSession(app.käyttöoikeusRepository)
  val oppijat = app.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta("250668-293Y")
  println(oppijat)
  println(oppijat.toList.flatMap(app.virta.findByOppija(_)).length)
}
