package fi.oph.koski.tools

import fi.oph.koski.http.DefaultHttpTester
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.ValidationResult
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, OidHenkilö, Opiskeluoikeus, Oppija}

object InvalidDataFixer extends App with DefaultHttpTester with Logging {
  authGet("api/opiskeluoikeus/validate") {
    val results = Json.read[List[ValidationResult]](body).filter(!_.isOk)
    println(s"Löytyi ${results.length} rikkinäistä opiskeluoikeutta")
    results.foreach { result =>
      println("FAIL id=" + result.opiskeluoikeusId + " " + result.errors)
    }
    println("Hit ENTER to try to fix all")
    readLine()
    println("Starting")
    results.foreach { result =>
      authGet("api/opiskeluoikeus/" + result.opiskeluoikeusId) {
        println("FIXING id=" + result.opiskeluoikeusId + " " + result.errors)
        val found = Json.read[Opiskeluoikeus](body)
        println("Deserialized OK")
        val oppija = Oppija(
          OidHenkilö(result.henkilöOid),
          List(found)
        )
        put("api/oppija", Json.write(oppija), headers = authHeaders() ++ jsonContent) {
          println("RESPONSE " + response.status)
        }
      }
    }
  }
}
