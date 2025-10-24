package fi.oph.koski.schema.policy

import fi.oph.scalaschema.Metadata
import fi.oph.koski.schema.annotation.DeserializeOnly

object SerializationPolicy {
  def shouldOmit(metadata: List[Metadata]): Boolean =
    metadata.exists(_.isInstanceOf[DeserializeOnly])
}
