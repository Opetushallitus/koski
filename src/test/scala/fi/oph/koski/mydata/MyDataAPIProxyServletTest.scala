package fi.oph.koski.mydata

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class MyDataAPIProxyServletTest extends AnyFreeSpec with KoskiHttpSpec with Matchers {

  implicit val formats = DefaultFormats

  val opiskelija = KoskiSpecificMockOppijat.markkanen
  val memberId = "hsl"
  val memberCode = "2769790-1" // HSL

  val muutLuovutuspalveluKäyttäjät = List(
    MockUsers.ytlKäyttäjä,
    MockUsers.valviraKäyttäjä,
    MockUsers.kelaLaajatOikeudet,
    MockUsers.kelaSuppeatOikeudet,
    MockUsers.migriKäyttäjä,
    MockUsers.tilastokeskusKäyttäjä,
    MockUsers.suomiFiKäyttäjä
  )

  def application = KoskiApplicationForTests

  "ApiProxyServlet" - {
    "Ei palauta mitään mikäli X-ROAD-MEMBER headeria ei ole asetettu" in {
      requestOpintoOikeudet(opiskelija.hetu.get, Map.empty, MockUsers.hslKäyttäjä){
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

    "Palauttaa 404-virheen kun opinto-oikeutta ei löydy" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, memberId)

      requestOpintoOikeudet("131047-803F", memberHeaders(memberCode)){
        status should equal(404)
        body should include("Oppijaa ei löydy annetulla oidilla tai käyttäjällä ei ole oikeuksia tietojen katseluun.")
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

      muutLuovutuspalveluKäyttäjät.map(user => {
        val wrongPasswordHeader = Map(basicAuthHeader(user.username, "wrong password"))
        requestOpintoOikeudetWithoutAuthHeaders(opiskelija.hetu.get, wrongPasswordHeader ++ memberHeaders(memberCode)) {
          status should equal(401)
        }
      })
    }

    "Palauttaa 200 mikäli HSL" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, memberId)

      requestOpintoOikeudetWithoutAuthHeaders(opiskelija.hetu.get, memberHeaders(memberCode) ++ authHeaders(MockUsers.hslKäyttäjä)) {
        status should equal(200)
      }
    }

    "Palauttaa 403 mikäli ei palveluväyläkäyttäjä" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, memberId)

      muutLuovutuspalveluKäyttäjät.map(user => {
        requestOpintoOikeudetWithoutAuthHeaders(opiskelija.hetu.get, memberHeaders(memberCode) ++ authHeaders(user)) {
          status should equal(403)
          body should include("Sallittu vain palveluväyläkäyttäjälle")
        }
      })
    }
  }

  def memberHeaders(memberCode: String) = Map("X-ROAD-MEMBER" -> memberCode)

  def requestOpintoOikeudet[A](hetu: String, headers: Map[String, String], user: KoskiMockUser = MockUsers.hslKäyttäjä)(f: => A) = {
    requestOpintoOikeudetWithoutAuthHeaders(hetu, headers ++ authHeaders(user))(f)
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
