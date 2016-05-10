package fi.oph.tor.virta

import fi.oph.tor.config.TorApplication
import fi.oph.tor.toruser.MockUsers

object VirtaPerfTester extends App {
  implicit val user = MockUsers.kalle.asTorUser
  private val app: TorApplication = TorApplication()
  val oppijat = app.oppijaRepository.findOppijat("090888-929X").map(_.oid)
  println(oppijat)
  println(oppijat.flatMap(app.virta.findByOppijaOid(_)).length)
}
