package fi.oph.koski.documentation

import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.SyntheticProperty

case class Example(name: String, description: String, data: Oppija, statusCode: Int = 200) {
  @SyntheticProperty
  def link: String = s"/koski/api/documentation/examples/$name.json"
}