package fi.oph.koski.valpas

import fi.oph.koski.http.ErrorCategory

object ValpasErrorCategory {
  object forbidden extends ErrorCategory("forbidden", 403, "Käyttäjällä ei ole oikeuksia tietoihin") {
    val oppija = subcategory("oppija", "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin")
    val oppijat = subcategory("oppijat", "Käyttäjällä ei ole oikeuksia oppijoiden tietoihin")
  }
}
