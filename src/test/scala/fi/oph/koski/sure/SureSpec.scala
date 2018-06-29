package fi.oph.koski.sure

import java.sql.Date
import java.time.{Instant, LocalDate, LocalDateTime}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JsonAST.JArray
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

class SureSpec extends FreeSpec with LocalJettyHttpSpecification with KoskiDatabaseMethods with Matchers {

  import fi.oph.koski.schema.KoskiSchema.deserializationContext
  val db = KoskiApplicationForTests.masterDatabase.db

  // MuuttunutEnnen/Jälkeen ovat LocalDateTime. Hmm.... DST-muutokset yms? Pitäisikö kuitenkin olla Instant

  "Sure-rajapinnat" - {
    "/api/sure/muuttuneet" - {
      "Palauttaa tyhjän listan jos ei muuttuneita" in {
        resetFixtures
        authGet("api/sure/muuttuneet?muuttunutJälkeen=2099-01-01T00:00:00Z&muuttunutEnnen=2099-01-02T00:00:00Z") {
          verifyResponseStatusOk()
          JsonMethods.parse(body) should equal(JArray(List.empty))
        }
      }
      "Palauttaa oikein muuttuneet opiskeluoikeudet" in {
        resetFixtures
        val muuttunutJälkeen = Instant.now
        val muuttunutEnnen = muuttunutJälkeen.plusSeconds(60)
        val henkilöOids = Set(MockOppijat.amis.oid, MockOppijat.lukiolainen.oid)
        val henkilöOidsIn = henkilöOids.map("'" + _ + "'").mkString(",")
        runDbSync(sqlu"update opiskeluoikeus set luokka = luokka where oppija_oid in (#$henkilöOidsIn)")
        authGet(s"api/sure/muuttuneet?muuttunutJälkeen=${muuttunutJälkeen}&muuttunutEnnen=${muuttunutEnnen}") {
          verifyResponseStatusOk()
          val oppijat = SchemaValidatingExtractor.extract[List[Oppija]](body).right.get
          oppijat.map(_.henkilö).toSet should equal(henkilöOids.map(OidHenkilö))
        }
      }
      "Palauttaa virheen jos toinen parametri puuttuu" in {
        authGet(s"api/sure/muuttuneet?muuttunutJälkeen=2099-01-01T00:00:00Z") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.missing())
        }
      }
      "Tuottaa oikean audit log viestin" in {
        val params = "muuttunutEnnen=2099-01-02T00:00:00Z&muuttunutJälkeen=2099-01-01T00:00:00Z"
        authGet(s"api/sure/muuttuneet?$params") {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("hakuEhto" -> params)))
        }
      }
    }
    "/api/sure/oids" - {
      "Palauttaa pyydetyt OIDit" in {
        val henkilöOids = Set(MockOppijat.amis.oid, MockOppijat.lukiolainen.oid)
        postOids(henkilöOids) {
          verifyResponseStatusOk()
          val oppijat = SchemaValidatingExtractor.extract[List[Oppija]](body).right.get
          oppijat.map(_.henkilö).toSet should equal(henkilöOids.map(OidHenkilö))
        }
      }
      "Palauttaa tyhjän listan jos OIDia ei löydy" in {
        postOids(Seq("1.2.246.562.24.90000000001")) {
          verifyResponseStatusOk()
          JsonMethods.parse(body) should equal(JArray(List.empty))
        }
      }
      "Palauttaa virheen jos OID on virheellinen" in {
        postOids(Seq("foobar")) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: foobar. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
        }
      }
      "Palauttaa virheen jos liian monta OIDia" in {
        val oids = List.fill(2000)("1.2.246.562.24.90000000001")
        postOids(oids) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Liian monta oidia, enintään 1000 sallittu."))
        }
      }
    }
  }

  private def postOids[A](oids: Iterable[String])(f: => A) =
    post("api/sure/oids", "[" + oids.map("\"" + _ + "\"").mkString(",") + "]", authHeaders() ++ jsonContent)(f)
}

