package fi.oph.koski.json


import fi.oph.koski.schema
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeudenTila
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import fi.oph.scalaschema.annotation.SyntheticProperty
import org.json4s.JValue

import java.time.LocalDate

object ExampleSchema {
  val schemaJson: JValue = SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[AlkamispäiväMuuttunut]).asInstanceOf[ClassSchema])
}

trait SynteettinenAlkupäivällinen {
  def tila: AmmatillinenOpiskeluoikeudenTila
  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] = this.tila.opiskeluoikeusjaksot.headOption.map(_.alku)
}

case class AlkamispäiväMuuttunut(
  tila: AmmatillinenOpiskeluoikeudenTila,
) extends SynteettinenAlkupäivällinen {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä.map(_.plusDays(1))
}
