package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExamplesEuropeanSchoolOfHelsinki
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationEuropeanSchoolOfHelsinkiSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[EuropeanSchoolOfHelsinkiOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
      verifyResponseStatusOk()
    }
  }

  // TODO: TOR-1685 Lisää tarvittavat testit validaatioita toteutettaessa
}
