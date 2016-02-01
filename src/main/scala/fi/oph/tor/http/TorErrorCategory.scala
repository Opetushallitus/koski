package fi.oph.tor.http

// TODO: organize/review error codes
// TODO: unit test to ensure consistency of naming and structure

object TorErrorCategory {
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
    object format extends ErrorCategory(badRequest, "format")  {
      val number = subcategory("number")
      val json = subcategory("json")
      val pvm = subcategory("date")
    }
    object validation extends ErrorCategory(badRequest, "validation") {
      object queryParam extends ErrorCategory(validation, "queryParam") {
        val unknown = subcategory("unknown")
        val tooShort = subcategory("tooShort")
      }
      val jsonSchema = subcategory("jsonSchema")
      val zeroLength = subcategory("zeroLength")

      object organisaatio extends ErrorCategory(validation, "organisaatio") {
        val tuntematon = subcategory("tuntematon")
        val vääränTyyppinen = subcategory("vääränTyyppinen")
      }
      object henkilötiedot extends ErrorCategory(validation, "henkilötiedot") {
        val puuttelliset = subcategory("puutteelliset")
        val hetu = subcategory("hetu")
        val virheellinenOid = subcategory("virheellinenOid")
      }
      val date = subcategory("date")

      object koodisto extends ErrorCategory(validation, "koodisto") {
        val tuntematonKoodi = subcategory("tuntematonKoodi")
      }

      object rakenne extends ErrorCategory(validation, "rakenne") {
        val tuntematonTutkinnonOsa = subcategory("tuntematonTutkinnonOsa")
        val suoritustapaPuuttuu = subcategory("suoritustapaPuuttuu")
        val diaariPuuttuu = subcategory("diaariPuuttuu", "Diaarinumero puuttuu")
        val tuntematonDiaari = subcategory("tuntematonDiaari")
        val tuntematonOsaamisala = subcategory("tuntematonOsaamisala")
      }
    }
  }
}