package fi.oph.koski.integrationtest

import fi.oph.koski.api.{OpiskeluoikeusTestMethodsAmmatillinen, SearchTestMethods}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema.{OidHenkilö, TäydellisetHenkilötiedot, YlioppilastutkinnonOpiskeluoikeus}
import org.scalatest.{FreeSpec, Matchers}

// This test is run against the Koski application deployed in the KoskiDev test environment.
// You need to provide environment variables KOSKI_USER, KOSKI_PASS
// Optionally override base url using KOSKI_BASE_URL for testing against your local server.
class OppijaIntegrationTest extends FreeSpec with Matchers with KoskidevHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods {
  val testOid = "1.2.246.562.24.92333381381"

  "Oppijan henkilötiedot, kansalaisuus ja äidinkieli" taggedAs(KoskiDevEnvironment) in {
    // This makes sure that our server is running, can authenticate a user, can insert data into the database and
    // return results, i.e. is up and running.

    putOpiskeluoikeus(defaultOpiskeluoikeus, OidHenkilö(testOid)) {
      verifyResponseStatusOk()
    }
    val o = oppija(testOid)
    val henkilö = o.henkilö.asInstanceOf[TäydellisetHenkilötiedot]
    henkilö.oid should equal(testOid)
    henkilö.kansalaisuus.flatMap(_.headOption).map(_.koodiarvo) should equal(Some("246"))
    henkilö.äidinkieli.map(_.koodiarvo) should equal(Some("FI"))

    o.opiskeluoikeudet.length should be >= 1
  }

  /*
  "Virta-integraatio" taggedAs(KoskiDevEnvironment) in {
    searchForHenkilötiedot("100869-192W").map(_.oid).headOption match {
      case None => fail("Virta-testihenkilöä ei löydy")
      case Some(oid) =>
        val o = oppija(oid)
        o.opiskeluoikeudet.filter(_.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo) == Some("virta")).length should be >= 1
    }
  }
  */

  "YTR-integraatio" taggedAs(KoskiDevEnvironment) in {
    searchForHenkilötiedot("140389-8638").map(_.oid).headOption match {
      case None => fail("YTR-testihenkilöä ei löydy")
      case Some(oid) =>
        val o = oppija(oid)
        o.opiskeluoikeudet.filter(_.isInstanceOf[YlioppilastutkinnonOpiskeluoikeus]).length should equal(1)
    }
  }

  // Doesn't perform good enough with large data sets so disabling for now
  /*"Tallennetun datan validiteetti" taggedAs(KoskiDevEnvironment) in {
    authGet("api/opiskeluoikeus/validate?errorsOnly=true") {
      def printValidity(result: ValidationResult) = {
        println(result.henkilöOid + (if (result.isOk) {" OK"} else {" FAIL " + result.errors}))
      }
      verifyResponseStatusOk()
      val results = Json.read[List[ValidationResult]](body)
      results.length should be >= 0
      println(s"Löytyi ${results.length} oppijaa")
      results.foreach(printValidity)
      results.flatMap(_.errors).length should equal(0)
    }
  }*/
}
