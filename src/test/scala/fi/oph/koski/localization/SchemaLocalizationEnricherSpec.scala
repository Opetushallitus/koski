package fi.oph.koski.localization

import fi.oph.koski.schema.{Finnish, LocalizedString}
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SchemaLocalizationEnricherSpec extends AnyFreeSpec with Matchers {
  private val localizations: Map[String, LocalizedString] = Map(
    "Tila" -> Finnish("Tila", Some("Status"), Some("State")),
    "description:Opiskeluoikeuden voimassaolo" -> Finnish("Opiskeluoikeuden voimassaolo", Some("Studierättens giltighet"), None)
  )
  private val enricher = new SchemaLocalizationEnricher(localizations)

  "translationFor" - {
    "builds an Otsikko entry from a title key" in {
      enricher.translationFor(List(("Tila", "Tila")), Nil) shouldBe Some(
        JObject("Otsikko" -> JArray(List(JString("fi: Tila"), JString("sv: Status"), JString("en: State"))))
      )
    }

    "omits missing languages and merges title + description" in {
      val result = enricher.translationFor(
        List(("Tila", "Tila")),
        List(("description:Opiskeluoikeuden voimassaolo", "Opiskeluoikeuden voimassaolo"))
      )
      result shouldBe Some(JObject(
        "Otsikko" -> JArray(List(JString("fi: Tila"), JString("sv: Status"), JString("en: State"))),
        "Kuvaus" -> JArray(List(JString("fi: Opiskeluoikeuden voimassaolo"), JString("sv: Studierättens giltighet")))
      ))
    }

    "returns None when nothing resolves" in {
      enricher.translationFor(List(("Unknown", "Unknown")), Nil) shouldBe None
    }
  }
}
