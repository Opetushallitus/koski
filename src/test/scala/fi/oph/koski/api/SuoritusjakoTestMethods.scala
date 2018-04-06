package fi.oph.koski.api

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.SuoritusIdentifier

trait SuoritusjakoTestMethods extends LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  def putSuoritusjako[A](body: Array[Byte], hetu: String = "180497-112F")(f: => A): A = {
    put("api/suoritusjako", body = body, headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)
  }

  def getSuoritusjako[A](secret: String, hetu: String = "180497-112F")(f: => A): A = {
    get(s"api/suoritusjako/$secret", headers = kansalainenLoginHeaders(hetu))(f)
  }

  def parseOppija() = {
    JsonSerializer.parse[Oppija](response.body)
  }

  def verifySuoritusIds(expectedSuoritusIds: List[SuoritusIdentifier]): Unit = {
    val actualSuoritusIds = parseOppija().opiskeluoikeudet.flatMap(oo =>
      oo.suoritukset.map(s => SuoritusIdentifier(
        oo.oppilaitos.get.oid,
        s.tyyppi.koodiarvo,
        s.koulutusmoduuli.tunniste.koodiarvo
      ))
    )

    actualSuoritusIds should contain theSameElementsAs expectedSuoritusIds
  }
}
