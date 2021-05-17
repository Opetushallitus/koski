package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LähdejärjestelmäId}

object OpiskeluoikeusIdentifier {
  def apply(oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): OpiskeluoikeusIdentifier = (opiskeluoikeus.oid, opiskeluoikeus.lähdejärjestelmänId) match {
    case (Some(oid), _) => OpiskeluoikeusByOid(oid)
    case (_, Some(lähdejärjestelmäId)) => OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId)
    case _ => OppijaOidOrganisaatioJaTyyppi(oppijaOid,
        opiskeluoikeus.getOppilaitos.oid,
        opiskeluoikeus.koulutustoimija.map(_.oid),
        opiskeluoikeus.tyyppi.koodiarvo,
        opiskeluoikeus.suoritukset.headOption.map(_.koulutusmoduuli.tunniste.koodiarvo),
        None)
  }
}

sealed trait OpiskeluoikeusIdentifier

case class OppijaOidJaLähdejärjestelmänId(oppijaOid: String, lähdejärjestelmäId: LähdejärjestelmäId) extends OpiskeluoikeusIdentifier
case class OppijaOidOrganisaatioJaTyyppi(oppijaOid: String,
                                         oppilaitosOrganisaatio: String,
                                         koulutustoimija: Option[String],
                                         opiskeluoikeudenTyyppi: String,
                                         päätasonSuorituksenKoulutusmoduulinTyyppi: Option[String],
                                         lähdejärjestelmäId: Option[LähdejärjestelmäId]) extends OpiskeluoikeusIdentifier
case class OpiskeluoikeusByOid(oid: String) extends OpiskeluoikeusIdentifier
