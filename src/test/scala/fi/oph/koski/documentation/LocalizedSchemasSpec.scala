package fi.oph.koski.documentation

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JNothing, JObject}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LocalizedSchemasSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val localizedSchemas =
    new LocalizedSchemas(KoskiApplicationForTests.koskiLocalizationRepository)(KoskiApplicationForTests.cacheManager)

  "koski-oppija-schema.json is enriched with sv/en translations on many nodes, excluding fi" in {
    val ts = translations(localizedSchemas("koski-oppija-schema.json"))
    ts.size should be > 50
    ts.exists(t => (t \ "sv" \ "title") != JNothing || (t \ "sv" \ "description") != JNothing) shouldBe true
    ts.exists(t => (t \ "en" \ "title") != JNothing || (t \ "en" \ "description") != JNothing) shouldBe true
    ts.forall(t => (t \ "fi") == JNothing) shouldBe true
  }

  "all registered viewer schemas are enriched with translations" - {
    val schemaNames = List(
      "kela-oppija-schema.json",
      "migri-oppija-schema.json",
      "valpas-internal-laaja-schema.json",
      "omadata-oauth2-kaikki-tiedot-oppija-schema.json"
    )
    schemaNames.foreach { name =>
      name in {
        localizedSchemas.contains(name) shouldBe true
        translations(localizedSchemas(name)).size should be > 10
      }
    }
  }

  private def translations(json: JValue): List[JValue] = json match {
    case JObject(fields) =>
      fields.flatMap {
        case ("translation", t) => t :: translations(t)
        case (_, v) => translations(v)
      }
    case JArray(items) => items.flatMap(translations)
    case _ => Nil
  }
}
