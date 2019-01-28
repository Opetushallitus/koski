package fi.oph.koski.json

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.{Henkilö, KoskiSchema, Oppija}
import fi.oph.scalaschema.{ClassSchema, Metadata, Property, SerializationContext}
import org.json4s.JValue

import scala.collection.immutable

case class SensitiveDataFilter(user: SensitiveDataAllowed) {
  private implicit val u = user

  def filterSensitiveData(s: ClassSchema, p: Property) = if (sensitiveHidden(p.metadata)) Nil else List(p)

  def serializationContext = SerializationContext(KoskiSchema.schemaFactory, filterSensitiveData)

  def rowSerializer: ((Henkilö, immutable.Seq[OpiskeluoikeusRow])) => JValue = {
    def ser(tuple: (Henkilö, immutable.Seq[OpiskeluoikeusRow])) = {
      JsonSerializer.serialize(Oppija(tuple._1, tuple._2.map(_.toOpiskeluoikeus)))
    }
    ser
  }

  def sensitiveHidden(metadata: List[Metadata]): Boolean = metadata.exists {
    case SensitiveData(allowedRoles) => !user.sensitiveDataAllowed(allowedRoles)
    case _ => false
  }
}

trait SensitiveDataAllowed {
  def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean
}

object SensitiveDataAllowed {
  lazy val SystemUser = new SensitiveDataAllowed { def sensitiveDataAllowed(requiredRoles: Set[Role]) = true }
}
