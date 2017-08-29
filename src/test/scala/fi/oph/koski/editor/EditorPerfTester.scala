package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenPerustutkintoExample
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.Timing

object EditorPerfTester extends App with Timing {
  implicit val application = KoskiApplication.apply
  implicit val user = KoskiSession.systemUser
  round(0)

  val rounds = 1000
  timed(s"$rounds rounds") {
    (1 to rounds).foreach(round)
  }

  def round(n: Int) = OppijaEditorModel.toEditorModel(Oppija(MockOppijat.eero, AmmatillinenPerustutkintoExample.perustutkinto.opiskeluoikeudet), true)
}