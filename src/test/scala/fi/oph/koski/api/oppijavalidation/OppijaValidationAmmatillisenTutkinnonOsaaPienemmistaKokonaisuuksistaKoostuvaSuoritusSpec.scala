package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

class OppijaValidationAmmatillisenTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritusSpec extends MuuAmmatillinenSpecification[TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus] {
  "Osaamisen hankkimistapa, koulutussopimus, ryhmä" - {
    val suoritus = defaultPäätasonSuoritus.copy(
      osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))),
      koulutussopimukset = Some(List(koulutussopimusjakso)),
      ryhmä = Some("XY")
    )
    "palautetaan HTTP 200" in putTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
  }

  override def defaultPäätasonSuoritus: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = kiinteistösihteerinTutkinnonOsaaPienempiMuuAmmatillinenKokonaisuus()
}
