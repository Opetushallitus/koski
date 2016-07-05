package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{LähdejärjestelmäId, Opiskeluoikeus}

object OpiskeluOikeusIdentifier {
  def apply(oppijaOid: String, opiskeluOikeus: Opiskeluoikeus): OpiskeluOikeusIdentifier = (opiskeluOikeus.id, opiskeluOikeus.lähdejärjestelmänId) match {
    case (Some(id), _) => PrimaryKey(id)
    case (_, Some(lähdejärjestelmäId)) => OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId)
    case _ => OppijaOidOrganisaatioJaTyyppi(oppijaOid, opiskeluOikeus.oppilaitos.oid, opiskeluOikeus.tyyppi.koodiarvo, None)
  }
}

sealed trait OpiskeluOikeusIdentifier

case class OppijaOidJaLähdejärjestelmänId(oppijaOid: String, lähdejärjestelmäId: LähdejärjestelmäId) extends OpiskeluOikeusIdentifier
case class OppijaOidOrganisaatioJaTyyppi(oppijaOid: String, oppilaitosOrganisaatio: String, tyyppi: String, lähdejärjestelmäId: Option[LähdejärjestelmäId]) extends OpiskeluOikeusIdentifier
case class PrimaryKey(id: Int) extends OpiskeluOikeusIdentifier