package fi.oph.koski.schema.filter

import fi.oph.koski.schema.policy.SerializationPolicy
import fi.oph.scalaschema.{ClassSchema, Schema, SchemaWithClassName}

object SchemaFilters {
  /**
   * Applies SerializationPolicy rules recursively to a schema tree.
   */
  def applySerializationPolicy(schema: Schema): Schema =
    schema.mapItems {
      case cs: ClassSchema =>
        val filteredDefs = cs.definitions.map(d =>
          applySerializationPolicy(d).asInstanceOf[SchemaWithClassName]
        )
        cs.copy(
          properties = cs.properties.filterNot(p =>
            SerializationPolicy.shouldOmit(p.metadata)
          ),
          definitions = filteredDefs
        )
      case other => other
    }
}
