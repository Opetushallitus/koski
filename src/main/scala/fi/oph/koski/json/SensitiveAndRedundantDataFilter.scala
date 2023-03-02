package fi.oph.koski.json

import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.schema.annotation.{RedundantData, SensitiveData}
import fi.oph.koski.schema.{Henkilö, KoskiSchema, Oppija}
import fi.oph.scalaschema.{ClassSchema, Metadata, Property, SerializationContext}
import org.json4s.JValue

import scala.collection.immutable

case class SensitiveAndRedundantDataFilter(user: SensitiveDataAllowed) {
  private implicit val u = user

  def filterSensitiveData(s: ClassSchema, p: Property) = if (shouldHideField(p.metadata)) Nil else List(p)

  def serializationContext = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)

  def rowSerializer: ((Henkilö, immutable.Seq[KoskiOpiskeluoikeusRow])) => JValue = {
    def ser(tuple: (Henkilö, immutable.Seq[KoskiOpiskeluoikeusRow])) = {
      JsonSerializer.serialize(Oppija(tuple._1, tuple._2.map(_.toOpiskeluoikeusUnsafe(user))))
    }
    ser
  }

  def shouldHideField(metadata: List[Metadata]): Boolean =
    metadata.exists {
    case SensitiveData(allowedRoles) => !user.sensitiveDataAllowed(allowedRoles)
    case RedundantData() => true
    case _ => false
  }
}

trait SensitiveDataAllowed {
  def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean
}

object SensitiveDataAllowed {
  lazy val SystemUser = new SensitiveDataAllowed { def sensitiveDataAllowed(requiredRoles: Set[Role]) = true }
}
