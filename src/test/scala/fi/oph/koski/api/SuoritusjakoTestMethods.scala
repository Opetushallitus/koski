package fi.oph.koski.api

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, SuoritusjakoRequest}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.HttpStatus

trait SuoritusjakoTestMethods extends LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {

  val suoritusjakoHetu = "180497-112F"

  def putSuoritusjako[A](body: Array[Byte], hetu: String = suoritusjakoHetu)(f: => A): A = {
    put("api/suoritusjako", body = body, headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)
  }

  def getSuoritusjako[A](secret: String)(f: => A): A = {
    post("api/suoritusjako/editor", JsonSerializer.writeWithRoot(SuoritusjakoRequest(secret)), headers = jsonContent)(f)
  }

  def getSuoritusjakoOppija(secret: String): Oppija = {
    KoskiApplicationForTests.suoritusjakoService.get(secret).right.get
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
