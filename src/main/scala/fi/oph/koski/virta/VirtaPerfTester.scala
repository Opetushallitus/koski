package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.MockUsers

object VirtaPerfTester extends App {
  implicit val user = MockUsers.kalle.asKoskiUser
  private val app: KoskiApplication = KoskiApplication()
  val oppijat = app.oppijaRepository.findOppijat("090888-929X").map(_.oid)
  println(oppijat)
  println(oppijat.flatMap(app.virta.findByOppijaOid(_)).length)
}
