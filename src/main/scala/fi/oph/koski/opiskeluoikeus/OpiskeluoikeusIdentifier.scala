package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{KoodiViite, LähdejärjestelmäId, Opiskeluoikeus}
object OpiskeluOikeusIdentifier {
  def apply(oppijaOid: String, opiskeluOikeus: Opiskeluoikeus): OpiskeluOikeusIdentifier = opiskeluOikeus.id match {
    case Some(id) => PrimaryKey(id)
    case _ => new IdentifyingSetOfFields(oppijaOid, opiskeluOikeus)
  }
}

case class IdentifyingSetOfFields(oppijaOid: String, oppilaitosOrganisaatio: String, paikallinenId: Option[LähdejärjestelmäId], tyyppi: String) extends OpiskeluOikeusIdentifier {
  def this(oppijaOid: String, opiskeluOikeus: Opiskeluoikeus) = {
    this(oppijaOid, opiskeluOikeus.oppilaitos.oid, opiskeluOikeus.lähdejärjestelmänId, opiskeluOikeus.tyyppi.koodiarvo)
  }
}
case class PrimaryKey(id: Int) extends OpiskeluOikeusIdentifier

sealed trait OpiskeluOikeusIdentifier