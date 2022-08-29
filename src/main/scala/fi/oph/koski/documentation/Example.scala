package fi.oph.koski.documentation

import fi.oph.koski.henkilo.{HenkilötiedotHetuRequest, HenkilötiedotSearchRequest}
import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.SyntheticProperty

trait ExampleBase {
  val name: String
  val description: String
  val statusCode: Int

  @SyntheticProperty
  def link: String = s"/koski/api/documentation/examples/$name.json"
}


case class Example(name: String, description: String, data: Oppija, statusCode: Int = 200) extends ExampleBase

case class ExampleHetuSearch(name: String, description: String, data: HenkilötiedotHetuRequest, statusCode: Int = 200) extends ExampleBase
