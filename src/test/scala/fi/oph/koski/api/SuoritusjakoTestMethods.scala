package fi.oph.koski.api

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.SuoritusIdentifier

trait SuoritusjakoTestMethods extends LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  def putSuoritusjako[A](body: Array[Byte])(f: => A): A = {
    put("api/suoritusjako", body = body, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent)(f)
  }

  def getSuoritusjako[A](uuid: String)(f: => A): A = {
    get(s"api/suoritusjako/$uuid", headers = kansalainenLoginHeaders("180497-112F"))(f)
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
