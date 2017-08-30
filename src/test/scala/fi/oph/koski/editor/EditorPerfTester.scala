package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenPerustutkintoExample
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.Timing

object EditorPerfTester extends App with Timing {
  implicit val application = KoskiApplication.apply
  implicit val user = KoskiSession.systemUser

  lazy val prebuiltModel = buildModel
  runTest(TestCase("build model", 10, (n) => buildModel))
  runTest(TestCase("serialize model", 10, (n) => Json.write(prebuiltModel)))


  def runTest(testCase: TestCase) = {
    testCase.round(0)
    timed(s"${testCase.rounds} rounds ${testCase.name}") {
      (1 to testCase.rounds).foreach(testCase.round)
    }
  }

  private def buildModel = {
    OppijaEditorModel.toEditorModel(Oppija(MockOppijat.eero, AmmatillinenPerustutkintoExample.perustutkinto.opiskeluoikeudet), true)
  }

}



case class TestCase(name: String, rounds: Int, round: (Int) => Unit)