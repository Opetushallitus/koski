package fi.oph.koski.api

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.Suoritus
import fi.oph.koski.koskiuser.MockUsers.paakayttaja

trait PaatasonSuoritusTestMethods extends LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  def deletePäätasonSuoritus[A](opiskeluoikeusOid: String, versionumero: Int, suoritus: Suoritus)(f: => A): A = {
    val suoritusBody = JsonSerializer.writeWithRoot(suoritus)

    post(s"api/opiskeluoikeus/$opiskeluoikeusOid/$versionumero/delete-paatason-suoritus",
      body = suoritusBody,
      headers = authHeaders(paakayttaja) ++ jsonContent
    )(f)
  }
}
