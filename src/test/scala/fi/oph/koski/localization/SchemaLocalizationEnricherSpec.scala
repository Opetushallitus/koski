package fi.oph.koski.localization

import fi.oph.koski.schema.{Finnish, KoskiSchema, LocalizedString}
import fi.oph.scalaschema.annotation.Title
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JsonAST.{JObject, JString}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SchemaLocalizationEnricherSpec extends AnyFreeSpec with Matchers {
  private val localizations: Map[String, LocalizedString] = Map(
    "Tila" -> Finnish("Tila", Some("Status"), Some("State")),
    "description:Opiskeluoikeuden voimassaolo" -> Finnish("Opiskeluoikeuden voimassaolo", Some("Studierättens giltighet"), None)
  )
  private val enricher = new SchemaLocalizationEnricher(localizations)

  "translationFor groups by language, fi included" - {
    "builds a title entry for each language" in {
      enricher.translationFor(List(("Tila", "Tila")), Nil) shouldBe Some(JObject(
        "fi" -> JObject("title" -> JString("Tila")),
        "sv" -> JObject("title" -> JString("Status")),
        "en" -> JObject("title" -> JString("State"))
      ))
    }

    "omits missing languages and includes title + description" in {
      enricher.translationFor(
        List(("Tila", "Tila")),
        List(("description:Opiskeluoikeuden voimassaolo", "Opiskeluoikeuden voimassaolo"))
      ) shouldBe Some(JObject(
        "fi" -> JObject("title" -> JString("Tila"), "description" -> JString("Opiskeluoikeuden voimassaolo")),
        "sv" -> JObject("title" -> JString("Status"), "description" -> JString("Studierättens giltighet")),
        "en" -> JObject("title" -> JString("State"))
      ))
    }

    "returns None when nothing resolves" in {
      enricher.translationFor(List(("Unknown", "Unknown")), Nil) shouldBe None
    }
  }

  "enrich" - {
    "injects language-grouped translations into matching property nodes" in {
      val enriched = enrichedTestSchema
      (enriched \ "properties" \ "tila" \ "translation" \ "fi" \ "title") shouldBe JString("Tila")
      (enriched \ "properties" \ "tila" \ "translation" \ "sv" \ "title") shouldBe JString("Status")
      (enriched \ "properties" \ "tila" \ "translation" \ "en" \ "title") shouldBe JString("State")
    }

    "translates the class title on the class node" in {
      val enriched = enrichedTestSchema
      (enriched \ "translation" \ "fi" \ "title") shouldBe JString("Tila")
      (enriched \ "translation" \ "sv" \ "title") shouldBe JString("Status")
    }
  }

  private def enrichedTestSchema = {
    val schema = KoskiSchema.createSchema(classOf[EnricherTestOppija]).asInstanceOf[ClassSchema]
    enricher.enrich(schema, SchemaToJson.toJsonSchema(schema))
  }
}

@Title("Tila") case class EnricherTestOppija(tila: String)
