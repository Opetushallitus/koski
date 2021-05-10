package fi.oph.koski.mydata

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.BasicAuthentication
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.scalatest.{FreeSpec, Matchers}

import java.time.LocalDate

class MyDataAPIProxyServletTest extends FreeSpec with KoskiHttpSpec with Matchers {

  implicit val formats = DefaultFormats

  val opiskelija = KoskiSpecificMockOppijat.markkanen
  val memberId = "hsl"
  val memberCode = "2769790-1" // HSL

  def application = KoskiApplicationForTests

  "ApiProxyServlet" - {
    "Ei palauta mitään mikäli X-ROAD-MEMBER headeria ei ole asetettu" in {
      requestOpintoOikeudet(opiskelija.hetu.get, Map.empty){
        status should equal(400)
        body should include("Vaadittu X-ROAD-MEMBER http-otsikkokenttä puuttuu")
      }
    }

    "Ei palauta mitään mikäli käyttäjä ei ole antanut lupaa" in {
      KoskiApplicationForTests.mydataRepository.delete(opiskelija.oid, memberId)

      requestOpintoOikeudet(opiskelija.hetu.get, memberHeaders(memberCode)){
        status should equal(403)
        body should include("X-ROAD-MEMBER:llä ei ole lupaa hakea opiskelijan tietoja")
      }
    }

    "Palauttaa opiskelutiedot mikäli käyttäjä on antanut siihen luvan" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, memberId)

      requestOpintoOikeudet(opiskelija.hetu.get, memberHeaders(memberCode)){
        status should equal(200)
        body should (include (KoskiSpecificMockOppijat.markkanen.etunimet) and include (KoskiSpecificMockOppijat.markkanen.sukunimi))
      }
    }

    "Palauttaa luvan viimeisen voimassaolopäivän opiskelijalle" in {
      val jsonMethods = org.json4s.jackson.JsonMethods
      val yearFromToday = LocalDate.now().plusYears(1)

      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, memberId)

      requestOpintoOikeudet(opiskelija.hetu.get, memberHeaders(memberCode)){
        status should equal(200)

        val response = jsonMethods.parse(body).extract[MyDataResponse]
        response.suostumuksenPaattymispaiva should be(yearFromToday.toString)
      }
    }

    "Palauttaa 401 mikäli luovutuspalvelukäyttäjän tunnukset ovat väärät" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, memberId)

      val wrongPasswordHeader = Map(BasicAuthentication.basicAuthHeader(MockUsers.luovutuspalveluKäyttäjä.username, "wrong password"))
      requestOpintoOikeudetWithoutAuthHeaders(opiskelija.hetu.get, wrongPasswordHeader ++ memberHeaders(memberCode)) {
        status should equal(401)
      }
    }
  }

  def memberHeaders(memberCode: String) = Map("X-ROAD-MEMBER" -> memberCode)

  def requestOpintoOikeudet[A](hetu: String, headers: Map[String, String])(f: => A) = {
    requestOpintoOikeudetWithoutAuthHeaders(hetu, headers ++ authHeaders(MockUsers.luovutuspalveluKäyttäjä))(f)
  }

  def requestOpintoOikeudetWithoutAuthHeaders[A](hetu: String, headers: Map[String, String])(f: => A) = {
    post(
      "api/omadata/oppija/",
      write(Map("hetu" -> hetu)),
      headers = jsonContent ++ headers
    )(f)
  }
}

case class MyDataResponse(
  suostumuksenPaattymispaiva: String
)
