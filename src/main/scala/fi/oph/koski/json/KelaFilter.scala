package fi.oph.koski.json

import fi.oph.koski.schema._
import fi.oph.scalaschema._

case class KelaFilter(user: FilteringCriteria) extends SensitiveDataFilter(user) {
  private val unallowedClasses = List(
    classOf[Arviointi],
    classOf[Vahvistaja]
  )

  override def shouldFilter(p: Property): Boolean = {
    SchemaUtil.getClazz(p.schema).exists { propertyClass =>
      unallowedClasses.exists(_.isAssignableFrom(propertyClass))
    }
  }
}
