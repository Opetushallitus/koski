package fi.oph.koski.json

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.scalaschema._
import org.json4s.JValue

class SensitiveDataFilter(user: FilteringCriteria) {
  implicit private val u = user

  def shouldFilter(p: Property): Boolean = false

  def sensitiveHidden(metadata: List[Metadata]): Boolean = metadata.exists {
    case SensitiveData(allowedRoles) => !user.sensitiveDataAllowed(allowedRoles)
    case _ => false
  }

  def filterData(s: ClassSchema, p: Property): List[Property] =
    if (sensitiveHidden(p.metadata) || shouldFilter(p)) Nil else List(p)

  def serializationContext = SerializationContext(KoskiSchema.schemaFactory, filterData)

  def rowSerializer: ((Henkilö, Seq[OpiskeluoikeusRow])) => JValue = {
    def ser(tuple: (Henkilö, Seq[OpiskeluoikeusRow])) = {
      JsonSerializer.serialize(Oppija(tuple._1, tuple._2.map(_.toOpiskeluoikeus)))
    }
    ser
  }
}

object SensitiveDataFilter {
  // TODO: tähän pitää kehittää jotain fiksua
  def apply(user: FilteringCriteria): SensitiveDataFilter = {
    val viranomaisOrgs = user.viranomaisOrganisaatiot.map(_.oid)
    if (viranomaisOrgs.contains(MockOrganisaatiot.kela)) {
      KelaFilter(user)
    } else if (viranomaisOrgs.contains(MockOrganisaatiot.migri)) {
      MigriFilter(user)
    } else {
      new SensitiveDataFilter(user)
    }
  }
}

case class MigriFilter(user: FilteringCriteria) extends SensitiveDataFilter(user) {
  override def shouldFilter(p: Property): Boolean = false
}

trait FilteringCriteria {
  def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean
  def viranomaisOrganisaatiot: Set[OrganisaatioWithOid]
}

object FilteringCriteria {
  lazy val SystemUser = new FilteringCriteria {
    override def sensitiveDataAllowed(requiredRoles: Set[Role]) = true
    override def viranomaisOrganisaatiot: Set[OrganisaatioWithOid] = Set()
  }
}
