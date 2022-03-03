package fi.oph.koski.valpas

import fi.oph.koski.http.ErrorCategory

object ValpasErrorCategory {

  object badRequest extends ErrorCategory("badRequest", 400, "Epäkelpo syöte") {
    class Validation extends ErrorCategory(badRequest, "validation", "Syötteen validointi epäonnistui.") {
      val kuntailmoituksenOppijaOid = subcategory("kuntailmoituksenOppijaOid", "Kuntailmoituksen oppijan validointi epäonnistui.")
      val kuntailmoituksenKohde = subcategory("kuntailmoituksenKohde", "Kuntailmoituksen kohteen validointi epäonnistui.")
      val kuntailmoituksenTekijä = subcategory("kuntailmoituksenTekijä", "Kuntailmoituksen tekijän validointi epäonnistui.")
      val kuntailmoituksenIlmoituspäivä = subcategory("kuntailmoituksenIlmoituspäivä", "Kuntailmoituksia ei voi tehdä ennen lain voimaantuloa 1.8.2021")
      val epävalidiHenkilöhakutermi = subcategory("epävalidiHenkilöhakutermi", "Hakutermi ei ollut validi suomalainen henkilötunnus tai oppijatunnus")
      val jsonSchema = subcategory("jsonSchema", "JSON-schema -validointi epäonnistui. Paluuviestin sisällä virheilmoitukset JSON-muodossa.")
      val epävalidiUuid = subcategory("epävalidiUuid", "UUID ei ollut validi")
      val virheellinenPäivämäärä = subcategory("virheellinenPäivämäärä", "Virheellinen päivämäärä")
    }
    val validation = new Validation
    val requestTooLarge = subcategory("requestTooLarge", "Liian suuri kysely")
  }

  object forbidden extends ErrorCategory("forbidden", 403, "Käyttäjällä ei ole oikeuksia tietoihin") {
    val oppija = subcategory("oppija", "Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin")
    val oppijat = subcategory("oppijat", "Käyttäjällä ei ole oikeuksia oppijoiden tietoihin")
    val opiskeluoikeus = subcategory("opiskeluoikeus", "Käyttäjällä ei ole oikeuksia opiskeluoikeuden tietoihin")
    val organisaatio = subcategory("organisaatio", "Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin")
    val ilmoitus = subcategory("ilmoitus", "Käyttäjällä ei ole oikeuksia ilmoitukseen")
    val toiminto = subcategory("toiminto", "Käyttäjällä ei ole oikeuksia toimintoon")
  }

  object notFound extends ErrorCategory("notFound", 404, "Not found") {
    val oppijaaEiLöydyTaiEiOikeuksia = subcategory("oppijaaEiLöydyTaiEiOikeuksia", "Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")
    val oppijaEiOppivelvollisuuslainPiirissä = subcategory("oppijaaEiLöydy", "Oppijaa ei löydy, koska hän ei ole oppivelvollisuuslain piirissä.")
    val oppijaaEiLöydyOpintopolusta = subcategory("oppijaaEiLöydyOpintopolusta", "Oppijaa ei löydy Opintopolusta.")
  }

  object internalError extends ErrorCategory("internalError", 500, "Palvelinvirhe")

  object notImplemented extends ErrorCategory("notImplemented", 501, "Ei toteutettu") {
    val kuntailmoituksenMuokkaus = subcategory("kuntailmoituksenMuokkaus", "Kuntailmoitusta ei voi muokata")
  }

  object badGateway extends ErrorCategory("badGateway", 502, "Virhe yhdyskäytävässä tai ulkoisessa palvelussa") {
    val sure = subcategory("sure", "Suoritusrekisterin palauttama hakukoostetieto oli viallinen.")
  }

  object unavailable extends ErrorCategory("unavailable", 503, "Palvelu ei ole juuri nyt käytettävissä") {
    val sure = subcategory("sure", "Hakukoosteita ei juuri nyt saada haettua suoritusrekisteristä. Yritä myöhemmin uudelleen.")
  }
}

