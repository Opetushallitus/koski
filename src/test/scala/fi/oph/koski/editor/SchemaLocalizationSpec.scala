package fi.oph.koski.editor

import fi.oph.koski.json.JsonSerializer
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

        val missingKeys = newStuff.map(_._2).toSet

        println(JsonSerializer.writeWithRoot(missingKeys.zip(missingKeys).toMap, pretty = true))

        println("Missing properties by class: " + newStuff.map { case (schema, title) => schema.simpleName + "." + title }.toList.mkString("\n"))

        fail(missingKeys.size +  " missing schema localization(s). Copy the above JSON snippet into /localization/default-texts.json")
      }
    }
  }

  private def findMissingLocalizedTextsInSchema: Set[(ClassSchema, String)] = {
    val propertyTitles: Set[(ClassSchema, String)] = allSchemas(EditorSchema.schema)(KoskiSchema.schemaFactory, collection.mutable.Set.empty[String]).collect { case s: ClassSchema => s.properties.map(p => (s, p.title)) }.flatten.toSet
    val existingKeys = DefaultLocalizations.defaultFinnishTexts.keys.toSet

    propertyTitles.filterNot{ case (schema, title) => existingKeys.contains(title) }
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
        case s: ReadFlattenedSchema => allSchemas(s.classSchema)
      }
    case s: OptionalSchema => s :: allSchemas(s.itemSchema)
    case s: ListSchema => s :: allSchemas(s.itemSchema)
    case s: ElementSchema => List(s)
  }
}

