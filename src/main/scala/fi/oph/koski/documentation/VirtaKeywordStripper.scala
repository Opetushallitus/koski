package fi.oph.koski.documentation

import fi.oph.koski.schema.annotation.{VirtaDerived, VirtaSource}
import fi.oph.scalaschema.{ClassSchema, Property, SchemaJsonDecorator}
import org.json4s.JsonAST.{JObject, JString}

// Poistaa Virta-lähdetiedot skeema-JSONista (virta-avain ja kuvaukseen liitetty "(Virta: …)"-lause),
// kun features.virtaSchemaDocumentation on pois päältä. Annotaatiot pysyvät koodissa.
class VirtaKeywordStripper(inner: SchemaJsonDecorator) extends SchemaJsonDecorator {
  override def decorateClass(schema: ClassSchema, json: JObject): JObject = inner.decorateClass(schema, json)

  override def decorateProperty(property: Property, json: JObject): JObject = {
    val clauses = property.metadata.collect {
      case VirtaSource(path, rule) => VirtaSource.clause(path, rule)
      case VirtaDerived(rule) => VirtaSource.clause("johdettu Koskessa", rule)
    }
    val fields = json.obj.filterNot(_._1 == "virta").flatMap {
      case ("description", JString(d)) =>
        Some(clauses.reverse.foldLeft(d)(withoutClause)).filter(_.nonEmpty).map("description" -> JString(_))
      case other => Some(other)
    }
    inner.decorateProperty(property, JObject(fields))
  }

  private def withoutClause(description: String, clause: String): String =
    if (description == clause) ""
    else if (description.endsWith(". " + clause)) description.dropRight(clause.length + 2)
    else if (description.endsWith(" " + clause)) description.dropRight(clause.length + 1)
    else description
}
