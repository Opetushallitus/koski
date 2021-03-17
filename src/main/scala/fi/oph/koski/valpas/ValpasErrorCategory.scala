package fi.oph.koski.valpas

import fi.oph.koski.http.ErrorCategory

object ValpasErrorCategory {
  object forbidden extends ErrorCategory("forbidden", 403, "Käyttäjällä ei ole oikeuksia tietoihin") {
    val oppija = subcategory("oppija", "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin")
    val oppijat = subcategory("oppijat", "Käyttäjällä ei ole oikeuksia oppijoiden tietoihin")
    val organisaatio = subcategory("oppijat", "Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin")
  }

  object internalError extends ErrorCategory("internalError", 500, "Internal server error")

  object unavailable extends ErrorCategory("unavailable", 503, "Service unavailable") {
    val sure = subcategory("sure", "Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
  }
}
