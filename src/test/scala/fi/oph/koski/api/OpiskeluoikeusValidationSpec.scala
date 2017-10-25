package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{MockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.json.JsonSerializer.parse
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.opiskeluoikeus.ValidationResult
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusValidationSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  implicit val session: KoskiSession = KoskiSession.systemUser

  "Validoi" - {
    "validi opiskeluoikeus" in {
      val opiskeluoikeusOid = oppija(MockOppijat.eero.oid).tallennettavatOpiskeluoikeudet.flatMap(_.oid).head
      authGet(s"api/opiskeluoikeus/validate/$opiskeluoikeusOid") {
        verifyResponseStatusOk()
        validationResult.errors should be(empty)
      }
    }

    "epävalidi opiskeluoikeus" in {
      val opiskeluoikeus = oppija(MockOppijat.eero.oid).tallennettavatOpiskeluoikeudet.head.withPäättymispäivä(LocalDate.now)
      KoskiApplicationForTests.opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(MockOppijat.eero.henkilö), opiskeluoikeus, allowUpdate = true)
      authGet(s"api/opiskeluoikeus/validate/${opiskeluoikeus.oid.get}") {
        verifyResponseStatusOk()
        validationResult.errors.map(_.key) should equal(List("badRequest.validation.date.päättymispäivämäärä"))
      }
    }
  }

  private def validationResult = parse[ValidationResult](body)
}