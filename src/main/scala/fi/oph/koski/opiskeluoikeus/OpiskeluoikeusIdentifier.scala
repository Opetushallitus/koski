package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LähdejärjestelmäId}

object OpiskeluoikeusIdentifier {
  def apply(oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): OpiskeluoikeusIdentifier = (opiskeluoikeus.oid, opiskeluoikeus.lähdejärjestelmänId) match {
    case (Some(oid), _) => OpiskeluoikeusByOid(oid)
    case (_, Some(lähdejärjestelmäId)) => OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId)
    case _ => OppijaOidOrganisaatioJaTyyppi(oppijaOid, opiskeluoikeus.getOppilaitos.oid, opiskeluoikeus.tyyppi.koodiarvo, None)
  }
}

sealed trait OpiskeluoikeusIdentifier

case class OppijaOidJaLähdejärjestelmänId(oppijaOid: String, lähdejärjestelmäId: LähdejärjestelmäId) extends OpiskeluoikeusIdentifier
case class OppijaOidOrganisaatioJaTyyppi(oppijaOid: String, oppilaitosOrganisaatio: String, tyyppi: String, lähdejärjestelmäId: Option[LähdejärjestelmäId]) extends OpiskeluoikeusIdentifier
case class OpiskeluoikeusByOid(oid: String) extends OpiskeluoikeusIdentifier
