package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenPerustutkintoExample
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.oppija.OppijaYksilöintitiedolla
import fi.oph.koski.perftest.LocalPerfTest
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.{Timing, WithWarnings}

object EditorPerfTester extends App with Timing {
  implicit val application = KoskiApplication.apply
  implicit val user = KoskiSpecificSession.systemUser

  lazy val prebuiltModel = buildModel
  LocalPerfTest.runTest(LocalPerfTest.TestCase("build model", 10, (n) => buildModel))
  LocalPerfTest.runTest(LocalPerfTest.TestCase("serialize model", 10, (n) => EditorModelSerializer.serializeModel(prebuiltModel)))

  private def buildModel = {
    OppijaEditorModel.toEditorModel(WithWarnings(
      OppijaYksilöintitiedolla(
        Oppija(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(KoskiSpecificMockOppijat.eero),
          AmmatillinenPerustutkintoExample.perustutkinto.opiskeluoikeudet
        ), false
      ), Nil), editable = true)
  }

}
