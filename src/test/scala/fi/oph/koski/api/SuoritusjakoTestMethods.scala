package fi.oph.koski.api

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, Suoritusjako, SuoritusjakoRequest, SuoritusjakoUpdateResponse}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.KoskiSession
import org.mockito.Mockito.{mock, when}
import org.scalatra.servlet.RichRequest

trait SuoritusjakoTestMethods extends LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {

  val suoritusjakoHetu = "180497-112F"
  private val mockKoskiSession = {
    val request = mock(classOf[RichRequest])
    when(request.header("User-Agent")).thenReturn(Some("MockUserAgent/1.0"))
    when(request.header("HTTP_X_FORWARDED_FOR")).thenReturn(Some("10.1.2.3"))
    when(request.cookies.toMap).thenReturn(Map[String, String]())
    KoskiSession.suoritusjakoKatsominenUser(request)
  }

  def createSuoritusjako[A](body: Array[Byte], hetu: String = suoritusjakoHetu, authenticate: Boolean = true)(f: => A): A = {
    post("api/suoritusjako", body = body, headers = (if (authenticate) kansalainenLoginHeaders(hetu) else Nil) ++ jsonContent)(f)
  }

  def getSuoritusjako[A](secret: String)(f: => A): A = {
    post("api/suoritusjako/editor", JsonSerializer.writeWithRoot(SuoritusjakoRequest(secret)), headers = jsonContent)(f)
  }

  def getSuoritusjakoOppija(secret: String): Oppija = {
    KoskiApplicationForTests.suoritusjakoService.get(secret)(mockKoskiSession).flatMap(_.warningsToLeft).right.get
  }

  def getSuoritusjakoDescriptors[A](hetu: String = suoritusjakoHetu, authenticate: Boolean = true)(f: => A): A = {
    get("api/suoritusjako", headers = (if (authenticate) kansalainenLoginHeaders(hetu) else Nil) ++ jsonContent)(f)
  }

  def updateSuoritusjako[A](body: Array[Byte], hetu: String = suoritusjakoHetu, authenticate: Boolean = true)(f: => A): A = {
    post("api/suoritusjako/update", body = body, headers = (if (authenticate) kansalainenLoginHeaders(hetu) else Nil) ++ jsonContent)(f)
  }

  def deleteSuoritusjako[A](body: Array[Byte], hetu: String = suoritusjakoHetu, authenticate: Boolean = true)(f: => A): A = {
    post("api/suoritusjako/delete", body = body, headers = (if (authenticate) kansalainenLoginHeaders(hetu) else Nil) ++ jsonContent)(f)
  }

  def parseOppija(): Oppija = {
    JsonSerializer.parse[Oppija](response.body)
  }

  def verifySuoritusIds(oppija: Oppija, expectedSuoritusIds: List[SuoritusIdentifier]): Unit = {
    val actualSuoritusIds = oppija.opiskeluoikeudet.flatMap(oo =>
      oo.suoritukset.map(s => SuoritusIdentifier(
        oo.lähdejärjestelmänId.flatMap(_.id),
        Some(oo.oppilaitos.get.oid),
        s.tyyppi.koodiarvo,
        s.koulutusmoduuli.tunniste.koodiarvo
      ))
    )

    actualSuoritusIds should contain theSameElementsAs expectedSuoritusIds
  }

  def verifySuoritusjakoUpdate(expectedExpirationDate: LocalDate): Unit = {
    val actualExpirationDate = JsonSerializer.parse[SuoritusjakoUpdateResponse](response.body).expirationDate
    actualExpirationDate shouldEqual expectedExpirationDate
  }

  private case class AlmostSameTimestamp(secret: String, expirationDate: LocalDate, timestamp: Timestamp) {
    override def equals(that: Any): Boolean = {
      that match {
        case that: AlmostSameTimestamp => {
          secret == that.secret &&
          expirationDate == that.expirationDate &&
          Math.abs(timestamp.getTime() - that.timestamp.getTime()) < 10000
        }
        case _ => false
      }
    }
  }

  def verifySuoritusjakoDescriptors(expectedSuoritusjaot: List[Suoritusjako]): Unit = {
    val actualSuoritusjaot = JsonSerializer.parse[List[Suoritusjako]](response.body)
    val expected = expectedSuoritusjaot.map { s => AlmostSameTimestamp(s.secret, s.expirationDate, s.timestamp) }
    val actual = actualSuoritusjaot.map { s => AlmostSameTimestamp(s.secret, s.expirationDate, s.timestamp) }
    actual should contain theSameElementsAs expected
  }
}
