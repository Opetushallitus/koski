package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

class OppijaValidationMuuAmmatillinenSpec extends MuuAmmatillinenSpecification[MuunAmmatillisenKoulutuksenSuoritus] {
  "Osaamisen hankkimistapa, koulutussopimus, ryhmä" - {
    val suoritus = defaultPäätasonSuoritus.copy(
      osaamisenHankkimistavat = Some(List(OsaamisenHankkimistapajakso(date(2018,1,1), None, osaamisenHankkimistapaOppilaitos))),
      koulutussopimukset = Some(List(koulutussopimusjakso)),
      ryhmä = Some("XY")
    )
    "palautetaan HTTP 200" in setupOppijaWithTutkintoSuoritus(suoritus)(verifyResponseStatusOk())
  }

  "Paikallisen muun ammatillisen koulutuksen tallennus" - {
    "Onnistuu kun opiskeluoikeus alkaa siirtymäajalla" in {
      val oo = makeOpiskeluoikeus(date(2028, 12, 31)).copy(
        suoritukset = List(defaultPäätasonSuoritus.copy(
          alkamispäivä = Some(date(2028, 12, 31))
        ))
      )
      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Aiheuttaa validaatiovirheen rajapäivän jälkeen" in {
      val oo = makeOpiskeluoikeus(date(2029, 1, 1)).copy(
        suoritukset = List(defaultPäätasonSuoritus.copy(
          alkamispäivä = Some(date(2029, 1, 1))
        ))
      )
      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ammatillinen.paikallinenMuuAmmatillinenRajapäivänJälkeen())
      }
    }
  }

  "Ammatilliseen tehtävään valmistava koulutuksen tallennus" - {
    "Onnistuu vaikka opiskeluoikeus alkaa rajapäivän jälkeen" in {
      val oo = makeOpiskeluoikeus(date(2029, 1, 1)).copy(
        suoritukset = List(ansioJaLiikenneLentäjänMuuAmmatillinenKoulutus().copy(
          alkamispäivä = Some(date(2029, 1, 1))
        ))
      )
      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  override def defaultPäätasonSuoritus: MuunAmmatillisenKoulutuksenSuoritus = kiinteistösihteerinMuuAmmatillinenKoulutus()
}
