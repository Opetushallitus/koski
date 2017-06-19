package fi.oph.koski.editor

import fi.oph.koski.json.Json
import fi.oph.koski.localization.DefaultLocalizations
import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema._
import org.scalatest.{FreeSpec, Matchers}

class SchemaLocalizationSpec extends FreeSpec with Matchers {
  "Koski schema texts" - {
    "Have default localizations" in {
      val newStuff = findMissingLocalizedTextsInSchema
      if (newStuff.nonEmpty) {
        println("Missing localized strings found in Koski schema. Copy these into /localization/default-texts.json")
        println(Json.writePretty(newStuff))

        fail(newStuff.size +  " missing schema localizations. Copy the above JSON snippet into /localization/default-texts.json")
      }
    }
  }

  private def findMissingLocalizedTextsInSchema = {
    val keys = allSchemas(EditorSchema.schema)(KoskiSchema.schemaFactory, collection.mutable.Set.empty[String]).collect { case s: ClassSchema => s.properties.map(ObjectModelBuilder.propertyTitle) }.flatten.toSet
    val keyValueMap = keys.zip(keys).toMap
    keyValueMap -- DefaultLocalizations.defaultFinnishTexts.keys
  }

  private def allSchemas(schema: Schema)(implicit factory: SchemaFactory, classesCovered: collection.mutable.Set[String]): List[Schema] = schema match {
    case s: ClassRefSchema => allSchemas(s.resolve(factory))
    case s: SchemaWithClassName if classesCovered.contains(s.fullClassName) =>
      Nil
    case s: SchemaWithClassName =>
      classesCovered.add(s.fullClassName)
      s match {
        case s: ClassSchema =>
          s :: s.properties.map(_.schema).flatMap(allSchemas)
        case s: AnyOfSchema => s :: s.alternatives.flatMap(allSchemas)
      }
    case s: OptionalSchema => s :: allSchemas(s.itemSchema)
    case s: ListSchema => s :: allSchemas(s.itemSchema)
    case s: ElementSchema => List(s)
  }

}

