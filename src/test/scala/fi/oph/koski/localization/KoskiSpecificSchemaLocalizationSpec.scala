package fi.oph.koski.localization

import fi.oph.koski.TestEnvironment
import fi.oph.koski.editor.EditorSchema
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.KoskiSchema
import fi.oph.scalaschema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KoskiSpecificSchemaLocalizationSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Koski schema texts" - {
    "Have default localizations" in {
      val newStuff: Set[(ClassSchema, String, String)] = findMissingLocalizedTextsInSchema
      if (newStuff.nonEmpty) {
        println("Missing localized strings found in Koski schema. Copy these into /localization/koski-default-texts.json")

        val missingKeysAndValues: Map[String, String] = newStuff.map { case (className, key, value) => (key, value)}.toMap

        println(JsonSerializer.writeWithRoot(missingKeysAndValues, pretty = true))

        println("Missing properties by class: " + newStuff.map { case (schema, key, title) => schema.simpleName + "." + key }.toList.mkString("\n"))

        fail(s"${missingKeysAndValues.size} missing schema localization(s). Copy the above JSON snippet into /localization/default-texts.json")
      }
    }
  }

  private def findMissingLocalizedTextsInSchema: Set[(ClassSchema, String, String)] = {
    val propertyTitles: Set[(ClassSchema, String, String)] = allSchemas(EditorSchema.schema)(KoskiSchema.schemaFactory, collection.mutable.Set.empty[String]).collect {
      case s: ClassSchema => KoskiSpecificSchemaLocalization.allLocalizableParts(s).map{ case (key, text) => (s, key, text)}
    }.flatten.toSet
    val existingKeys = new DefaultLocalizations(new KoskiLocalizationConfig().defaultFinnishTextsResourceFilename).defaultFinnishTexts.keys.toSet

    propertyTitles.filterNot{ case (schema, key, title) => existingKeys.contains(key) }
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
        case _ => ???
      }
    case s: OptionalSchema => s :: allSchemas(s.itemSchema)
    case s: ListSchema => s :: allSchemas(s.itemSchema)
    case s: MapSchema => s :: allSchemas(s.itemSchema)
    case s: ElementSchema => List(s)
  }
}
