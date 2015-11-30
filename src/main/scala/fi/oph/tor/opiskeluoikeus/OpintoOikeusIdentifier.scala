package fi.oph.tor.opiskeluoikeus
import fi.oph.tor.schema.{KoodiViite, LähdejärjestelmäId, OpiskeluOikeus}
object OpiskeluOikeusIdentifier {
  def apply(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus): OpiskeluOikeusIdentifier = opiskeluOikeus.id match {
    case Some(id) => PrimaryKey(id)
    case _ => new IdentifyingSetOfFields(oppijaOid, opiskeluOikeus)
  }
}

case class IdentifyingSetOfFields(oppijaOid: String, oppilaitosOrganisaatio: String, paikallinenId: Option[LähdejärjestelmäId], tunniste: KoodiViite) extends OpiskeluOikeusIdentifier {
  def this(oppijaOid: String, opiskeluOikeus: OpiskeluOikeus) = {
    this(oppijaOid, opiskeluOikeus.oppilaitos.oid, opiskeluOikeus.lähdejärjestelmänId, opiskeluOikeus.suoritus.koulutusmoduulitoteutus.koulutusmoduuli.tunniste)
  }
}
case class PrimaryKey(id: Int) extends OpiskeluOikeusIdentifier

sealed trait OpiskeluOikeusIdentifier