package fi.oph.tor.http

object TorErrorCategory {
  val children = List(ok, badRequest, unauthorized, forbidden, notFound, conflict, unsupportedMediaType, internalError)

  object ok extends ErrorCategory("ok", 200, "Ok") {
    val createdOrUpdated = subcategory("createdOrUpdated", "Päivitys/lisäys onnistui.")
    val searchOk = subcategory("searchOk", "Haku onnistui.")
    val maybeEmptyList = subcategory("maybeEmptyList", "Haku onnistui. Myös silloin kun ei löydy yhtään tulosta ja palautetaan tyhjä lista.")
    val maybeValidationErrorsInContent = subcategory("maybeValidationErrorsInContent", "Haku onnistui. Mahdolliset validointivirheet palautetaan json-vastauksessa.")
  }

  object badRequest extends ErrorCategory("badRequest", 400, "Epäkelpo syöte") {
    class Format extends ErrorCategory(badRequest, "format", "Epäkelpo syötteen formaatti.") {
      val number = subcategory("number", "Numeromuotoisen parametrin parsinta epäonnistui.")
      val json = subcategory("json", "JSON-dokumentin parsinta epäonnistui.")
      val pvm = subcategory("date", "Päivämäärän parsinta epäonnistui.")
    }
    val format = new Format

    class QueryParam extends ErrorCategory(badRequest, "queryParam", "Epäkelpo kyselyparametri") {
      val unknown = subcategory("unknown", "Annettua kyselyparametria ei tueta.")
      val searchTermTooShort = subcategory("searchTermTooShort", "Hakusanan pituus alle 3 merkkiä.")
      val virheellinenOid = subcategory("virheellinenOid", "Henkilö-oidin muoto on virheellinen. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001.")
    }
    val queryParam = new QueryParam

    class Validation extends ErrorCategory(badRequest, "validation", "Syötteen validointi epäonnistui") {
      val jsonSchema = subcategory("jsonSchema", "JSON-schema -validointi epäonnistui. Paluuviestin sisällä virheilmoitukset JSON-muodossa.")
      val tyhjäOpiskeluoikeusLista = subcategory("tyhjäOpiskeluoikeusLista", "Annettiin tyhjä lista opiskeluoikeuksia.")

      class Organisaatio extends ErrorCategory(Validation.this, "organisaatio", "Epäkelpo organisaatio") {
        val tuntematon = subcategory("tuntematon", "Tuntematon organisaatio: oid-tunnisteella ei löytynyt organisaatiota.")
        val vääränTyyppinen = subcategory("vääränTyyppinen", "Organisaatio on väärän tyyppinen. Esimerkiksi oppilaitoksena käytetty organisaatio ei ole oppilaitostyyppinen.")
      }
      val organisaatio = new Organisaatio

      class Henkilötiedot extends ErrorCategory(Validation.this, "henkilötiedot", "Epäkelvot henkilötiedot") {
        val virheelliset = subcategory("puutteelliset", "Henkilötiedot virheelliset henkilöpalvelun tekemien tarkistusten perusteella.")
        val hetu = subcategory("hetu", "Henkilötunnus on virheellinen.")
      }
      val henkilötiedot = new Henkilötiedot

      class Date extends ErrorCategory(Validation.this, "date", "Päivämäärä on oikeassa formaatissa, mutta semanttisesti epäkelpo.") {
        val loppuEnnenAlkua = subcategory("loppuEnnenAlkua", "Annettu (arviointu) loppupäivä on aiemmin kuin alkupäivä.")
        val jaksonLoppupäiväPuuttuu = subcategory("jaksonLoppupäiväPuuttuu", "Ei-viimeiseltä jaksolta puuttuu loppupäivä (esim. opiskeluoikeusjaksot tai läsnäolojaksot).")
        val jaksotEivätMuodostaJatkumoa = subcategory("jaksotEivätMuodostaJatkumoa",
          "Annettu lista jaksoa ei muodosta keskeytymätöntä jatkumoa: päivämääriä puuttuu välistä, tai jaksot päällekkäisiä (esim. opiskeluoikeusjaksot tai läsnäolojaksot).")
      }
      val date = new Date

      class Koodisto extends ErrorCategory(Validation.this, "koodisto", "Koodistoihin liittyvä tarkistusvirhe") {
        val tuntematonKoodi = subcategory("tuntematonKoodi", "Annettua koodia ei löydy koodistosta.")
      }
      val koodisto = new Koodisto

      class Rakenne extends ErrorCategory(Validation.this, "rakenne", "Tutkinnon rakenteeseen liittyvä validointivirhe") {
        val tuntematonTutkinnonOsa = subcategory("tuntematonTutkinnonOsa", "Annettua tutkinnon osaa ei löydy rakenteesta.")
        val suoritustapaPuuttuu = subcategory("suoritustapaPuuttuu", "Tutkinnolta puuttuu suoritustapa. Tutkinnon osasuorituksia ei hyväksytä.")
        val suoritustapaaEiLöydyRakenteesta = subcategory("suoritustapaaEiLöydyRakenteesta", "Suoritustapaa ei löydy tutkinnon rakenteesta")
        val tuntematonDiaari = subcategory("tuntematonDiaari", "Tutkinnon perustetta ei löydy diaarinumerolla.")
        val tuntematonOsaamisala = subcategory("tuntematonOsaamisala", "Annettua osaamisalaa ei löydy tutkinnon rakenteesta.")
      }
      val rakenne = new Rakenne

      class Tila extends ErrorCategory(Validation.this, "tila", "Suorituksen tilaan liittyvä validointivirhe") {
        val vahvistusVäärässäTilassa = subcategory("vahvistusVäärässäTilassa", "Suorituksella on vahvistus, vaikka sen tila ei ole VALMIS")
        val vahvistusPuuttuu = subcategory("vahvistusPuuttuu", "Suoritukselta puuttuu vahvistus, vaikka sen tila on VALMIS")
        val keskeneräinenOsasuoritus = subcategory("keskeneräinenOsasuoritus", "Suorituksen tila on VALMIS, vaikka sisältää osasuorituksen tilassa KESKEN")
      }
      val tila = new Tila
    }
    val validation = new Validation
  }

  object unauthorized extends ErrorCategory("unauthorized", 401, "Käyttäjä ei ole tunnistautunut.")

  object forbidden extends ErrorCategory("forbidden", 403, "Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin.") {
    val organisaatio = subcategory("organisaatio", "Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen).")
  }

  object notFound extends ErrorCategory("notFound", 404, "Not found") {
    val oppijaaEiLöydyTaiEiOikeuksia = subcategory("oppijaaEiLöydyTaiEiOikeuksia", "Oppijaa ei löydy annetulla oidilla tai käyttäjällä ei ole oikeuksia tietojen katseluun.")
    val oppijaaEiLöydy = subcategory("oppijaaEiLöydy", "Oppijaa ei löydy annetulla oidilla.")
    val opiskeluoikeuttaEiLöydyTaiEiOikeuksia = subcategory("opiskeluoikeuttaEiLöydyTaiEiOikeuksia", "Opiskeluoikeutta ei löydy annetulla id:llä tai käyttäjällä ei ole siihen oikeuksia")
    val versiotaEiLöydy = subcategory("versiotaEiLöydy", "Haettua versiota ei löydy")
    val koodistoaEiLöydy = subcategory("koodistoaEiLöydy", "Pyydettyä koodistoa ei löydy.")
    val diaarinumeroaEiLöydy = subcategory("diaarinumeroaEiLöydy", "Tutkinnon rakennetta ei löydy annetulla diaarinumerolla.")
    val todistustaEiLöydy = subcategory("todistustaEiLöydy", "Pyydettyä todistusta ei löydy")
  }

  object conflict extends ErrorCategory("conflict", 409, "Ristiriitainen päivitys")  {
    val versionumero = subcategory("versionumero", "Yritetty päivittää vanhan version päälle; annettu versionumero on erisuuri kuin viimeisin rekisteristä löytyvä.")
    val hetu = subcategory("hetu", "Henkilö on jo lisätty annetulla hetulla.")
  }

  object unsupportedMediaType extends ErrorCategory("unsupportedMediaType", 415, "Unsupported media type") {
    val jsonOnly = subcategory("jsonOnly", "Wrong content type: only application/json content type allowed")
  }

  object internalError extends ErrorCategory("internalError", 500, "Internal server error")
}