package fi.oph.koski.tools

import fi.oph.koski.http.DefaultHttpTester
import fi.oph.common.json.JsonSerializer
import fi.oph.common.log.Logging
import fi.oph.koski.opiskeluoikeus.ValidationResult
import fi.oph.koski.schema._

object InvalidDataFixer extends App with DefaultHttpTester with Logging {
  authGet("api/opiskeluoikeus/validate") {
    val results = JsonSerializer.parse[List[ValidationResult]](body).filter(!_.isOk)
    println(s"Löytyi ${results.length} rikkinäistä opiskeluoikeutta")
    results.foreach { result =>
      println("FAIL oid=" + result.opiskeluoikeusOid + " " + result.errors)
    }
    println("Hit ENTER to try to fix all")
    scala.io.StdIn.readLine()
    println("Starting")
    results.foreach { result =>
      authGet("api/opiskeluoikeus/" + result.opiskeluoikeusOid) {
        println("FIXING oid=" + result.opiskeluoikeusOid + " " + result.errors)
        val found = JsonSerializer.parse[Opiskeluoikeus](body)
        println("Deserialized OK")
        val oppija = Oppija(
          OidHenkilö(result.henkilöOid),
          List(found)
        )
        put("api/oppija", JsonSerializer.writeWithRoot(oppija), headers = authHeaders() ++ jsonContent) {
          println("RESPONSE " + response.status)
        }
      }
    }
  }
}
