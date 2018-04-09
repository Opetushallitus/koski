package fi.oph.koski.api

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, SuoritusjakoRequest}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.KoskiSession
import org.mockito.Mockito.{mock, when, RETURNS_DEEP_STUBS}
import org.mockito.Matchers.anyObject
import org.scalatra.servlet.RichRequest

trait SuoritusjakoTestMethods extends LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {

  val suoritusjakoHetu = "180497-112F"
  private val mockKoskiSession = {
    val request = mock(classOf[RichRequest])
    when(request.header("User-Agent")).thenReturn(Some("MockUserAgent/1.0"))
    when(request.header("HTTP_X_FORWARDED_FOR")).thenReturn(Some("10.1.2.3"))
    KoskiSession.suoritusjakoKatsominenUser(request)
  }

  def putSuoritusjako[A](body: Array[Byte], hetu: String = suoritusjakoHetu)(f: => A): A = {
    put("api/suoritusjako", body = body, headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)
  }

  def getSuoritusjako[A](secret: String)(f: => A): A = {
    post("api/suoritusjako/editor", JsonSerializer.writeWithRoot(SuoritusjakoRequest(secret)), headers = jsonContent)(f)
  }

  def getSuoritusjakoOppija(secret: String): Oppija = {
    KoskiApplicationForTests.suoritusjakoService.get(secret)(mockKoskiSession).right.get
  }

  def parseOppija(): Oppija = {
    JsonSerializer.parse[Oppija](response.body)
  }

  def verifySuoritusIds(oppija: Oppija, expectedSuoritusIds: List[SuoritusIdentifier]): Unit = {
    val actualSuoritusIds = oppija.opiskeluoikeudet.flatMap(oo =>
      oo.suoritukset.map(s => SuoritusIdentifier(
        oo.oppilaitos.get.oid,
        s.tyyppi.koodiarvo,
        s.koulutusmoduuli.tunniste.koodiarvo
      ))
    )

    actualSuoritusIds should contain theSameElementsAs expectedSuoritusIds
  }
}
