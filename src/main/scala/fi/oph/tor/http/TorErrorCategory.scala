package fi.oph.tor.http

import fi.oph.tor.http.TorErrorCategory.badRequest.format._

// TODO: organize/review error codes

object TorErrorCategory {
  val children = List(badRequest, forbidden, notFound, conflict, internalError)

  object notFound extends CategoryWithDefaultText("notFound", 404, "Not found") // TODO: alikategoriat

  object internalError extends CategoryWithDefaultText("internalError", 500, "Internal server error")

  object forbidden extends ErrorCategory("forbidden", 403) {
    val organisaatio = subcategory("organisaatio")
  }

  object conflict extends ErrorCategory("conflict", 409)  {
    val versionumero = subcategory("versionumero")
    val hetu = subcategory("hetu")
  }

  object badRequest extends ErrorCategory("badRequest", 400) {
    class Format extends ErrorCategory(badRequest, "format") {
      val number = subcategory("number")
      val json = subcategory("json")
      val pvm = subcategory("date")
    }
    val format = new Format

    class Validation extends ErrorCategory(badRequest, "validation") {
      class QueryParam extends ErrorCategory(Validation.this, "queryParam") {
        val unknown = subcategory("unknown")
        val tooShort = subcategory("tooShort")
      }
      val queryParam = new QueryParam

      val jsonSchema = subcategory("jsonSchema")
      val zeroLength = subcategory("zeroLength")

      class Organisaatio extends ErrorCategory(Validation.this, "organisaatio") {
        val tuntematon = subcategory("tuntematon")
        val vääränTyyppinen = subcategory("vääränTyyppinen")
      }
      val organisaatio = new Organisaatio

      class Henkilötiedot extends ErrorCategory(Validation.this, "henkilötiedot") {
        val puuttelliset = subcategory("puutteelliset")
        val hetu = subcategory("hetu")
        val virheellinenOid = subcategory("virheellinenOid")
      }
      val henkilötiedot = new Henkilötiedot

      val date = subcategory("date")

      class Koodisto extends ErrorCategory(Validation.this, "koodisto") {
        val tuntematonKoodi = subcategory("tuntematonKoodi")
      }
      val koodisto = new Koodisto

      class Rakenne extends ErrorCategory(Validation.this, "rakenne") {
        val tuntematonTutkinnonOsa = subcategory("tuntematonTutkinnonOsa")
        val suoritustapaPuuttuu = subcategory("suoritustapaPuuttuu")
        val diaariPuuttuu = subcategory("diaariPuuttuu", "Diaarinumero puuttuu")
        val tuntematonDiaari = subcategory("tuntematonDiaari")
        val tuntematonOsaamisala = subcategory("tuntematonOsaamisala")
      }
      val rakenne = new Rakenne
    }

    val validation = new Validation
  }
}