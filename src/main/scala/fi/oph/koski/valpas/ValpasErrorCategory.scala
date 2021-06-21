package fi.oph.koski.valpas

import fi.oph.koski.http.ErrorCategory

object ValpasErrorCategory {
  object validation extends ErrorCategory("validation", 400, "Syötteen validointi epäonnistui.") {
    val kuntailmoituksenKohde = subcategory("kuntailmoituksenKohde", "Kuntailmoituksen kohteen validointi epäonnistui.")
    val kuntailmoituksenTekijä = subcategory("kuntailmoituksenTekijä", "Kuntailmoituksen tekijän validointi epäonnistui.")
    val kuntailmoituksenIlmoituspäivä = subcategory("kuntailmoituksenIlmoituspäivä", "Kuntailmoituksia ei voi tehdä ennen lain voimaantuloa 1.8.2021")
    val epävalidiHenkilöhakutermi = subcategory("epävalidiHenkilöhakutermi", "Hakutermi ei ollut validi suomalainen henkilötunnus tai oppijatunnus")
  }

  object forbidden extends ErrorCategory("forbidden", 403, "Käyttäjällä ei ole oikeuksia tietoihin") {
    val oppija = subcategory("oppija", "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin")
    val oppijat = subcategory("oppijat", "Käyttäjällä ei ole oikeuksia oppijoiden tietoihin")
    val opiskeluoikeus = subcategory("opiskeluoikeus", "Käyttäjällä ei ole oikeuksia opiskeluoikeuden tietoihin")
    val organisaatio = subcategory("organisaatio", "Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin")
    val ilmoitus = subcategory("ilmoitus", "Käyttäjällä ei ole oikeuksia ilmoitukseen")
    val toiminto = subcategory("toiminto", "Käyttäjällä ei ole oikeuksia toimintoon")
  }

  object internalError extends ErrorCategory("internalError", 500, "Internal server error")

  object notImplemented extends ErrorCategory("notImplemented", 501, "Not implemented") {
    val kuntailmoituksenMuokkaus = subcategory("kuntailmoituksenMuokkaus", "Kuntailmoitusta ei voi muokata")
  }

  object badGateway extends ErrorCategory("badGateway", 502, "Bad gateway") {
    val sure = subcategory("sure", "Suoritusrekisterin palauttama hakukoostetieto oli viallinen.")
  }

  object unavailable extends ErrorCategory("unavailable", 503, "Service unavailable") {
    val sure = subcategory("sure", "Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
  }
}

