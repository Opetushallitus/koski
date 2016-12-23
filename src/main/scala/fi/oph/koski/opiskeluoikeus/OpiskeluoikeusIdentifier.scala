package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{LähdejärjestelmäId, Opiskeluoikeus}

object OpiskeluoikeusIdentifier {
  def apply(oppijaOid: String, opiskeluoikeus: Opiskeluoikeus): OpiskeluoikeusIdentifier = (opiskeluoikeus.id, opiskeluoikeus.lähdejärjestelmänId) match {
    case (Some(id), _) => PrimaryKey(id)
    case (_, Some(lähdejärjestelmäId)) => OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId)
    case _ => OppijaOidOrganisaatioJaTyyppi(oppijaOid, opiskeluoikeus.oppilaitos.oid, opiskeluoikeus.tyyppi.koodiarvo, None)
  }
}

sealed trait OpiskeluoikeusIdentifier

case class OppijaOidJaLähdejärjestelmänId(oppijaOid: String, lähdejärjestelmäId: LähdejärjestelmäId) extends OpiskeluoikeusIdentifier
case class OppijaOidOrganisaatioJaTyyppi(oppijaOid: String, oppilaitosOrganisaatio: String, tyyppi: String, lähdejärjestelmäId: Option[LähdejärjestelmäId]) extends OpiskeluoikeusIdentifier
case class PrimaryKey(id: Int) extends OpiskeluoikeusIdentifier