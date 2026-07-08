package fi.oph.koski.documentation

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LocalizedSchemasSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val localizedSchemas =
    new LocalizedSchemas(KoskiApplicationForTests.koskiLocalizationRepository)(KoskiApplicationForTests.cacheManager)

  "koski-oppija-schema.json is enriched with fi/sv/en translations on many nodes" in {
    val lines = translationLines(localizedSchemas("koski-oppija-schema.json"))
    lines.count(_.startsWith("fi: ")) should be > 50
    lines.exists(_.startsWith("sv: ")) shouldBe true
    lines.exists(_.startsWith("en: ")) shouldBe true
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
        translationLines(localizedSchemas(name)).count(_.startsWith("fi: ")) should be > 10
      }
    }
  }

  private def translationLines(json: JValue): List[String] = json match {
    case JObject(fields) =>
      fields.flatMap {
        case ("translation", t) => collectStrings(t)
        case (_, v) => translationLines(v)
      }
    case JArray(items) => items.flatMap(translationLines)
    case _ => Nil
  }

  private def collectStrings(json: JValue): List[String] = json match {
    case JString(s) => List(s)
    case JObject(fields) => fields.flatMap { case (_, v) => collectStrings(v) }
    case JArray(items) => items.flatMap(collectStrings)
    case _ => Nil
  }
}
