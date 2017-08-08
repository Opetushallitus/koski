package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{LähdejärjestelmäId, Opiskeluoikeus}

object OpiskeluoikeusIdentifier {
  def apply(oppijaOid: String, opiskeluoikeus: Opiskeluoikeus): OpiskeluoikeusIdentifier = (opiskeluoikeus.oid, opiskeluoikeus.oid, opiskeluoikeus.lähdejärjestelmänId) match {
    case (_, Some(oid), _) => OpiskeluoikeusOid(oid)
    case (_, _, Some(lähdejärjestelmäId)) => OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId)
    case _ => OppijaOidOrganisaatioJaTyyppi(oppijaOid, opiskeluoikeus.getOppilaitos.oid, opiskeluoikeus.tyyppi.koodiarvo, None)
  }
}

sealed trait OpiskeluoikeusIdentifier

case class OppijaOidJaLähdejärjestelmänId(oppijaOid: String, lähdejärjestelmäId: LähdejärjestelmäId) extends OpiskeluoikeusIdentifier
case class OppijaOidOrganisaatioJaTyyppi(oppijaOid: String, oppilaitosOrganisaatio: String, tyyppi: String, lähdejärjestelmäId: Option[LähdejärjestelmäId]) extends OpiskeluoikeusIdentifier
case class OpiskeluoikeusOid(oid: String) extends OpiskeluoikeusIdentifier