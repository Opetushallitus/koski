package fi.oph.koski.opiskeluoikeus
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LähdejärjestelmäId, Organisaatio}

object OpiskeluoikeusIdentifier {

  def apply(
    oppijaOid: String,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): OpiskeluoikeusIdentifier = {
    (opiskeluoikeus.oid, opiskeluoikeus.lähdejärjestelmänId, opiskeluoikeus.oppilaitos) match {
      case (Some(oid), _, _) => OpiskeluoikeusByOid(oid)
      case (_, Some(lähdejärjestelmäId), oppilaitos) => OppijaOidJaLähdejärjestelmänId(
        oppijaOid,
        lähdejärjestelmäId,
        oppilaitos.map(_.oid)
      )
      case _ => OppijaOidOrganisaatioJaTyyppi(
        oppijaOid,
        opiskeluoikeus.getOppilaitos.oid,
        opiskeluoikeus.koulutustoimija.map(_.oid),
        opiskeluoikeus.tyyppi.koodiarvo,
        opiskeluoikeus.suoritukset.headOption.map(_.koulutusmoduuli.tunniste.koodiarvo),
        opiskeluoikeus.suoritukset.headOption.map(_.tyyppi.koodiarvo),
        None
      )
    }
  }
}

sealed trait OpiskeluoikeusIdentifier

case class OppijaOidJaLähdejärjestelmänId(oppijaOid: String, lähdejärjestelmäId: LähdejärjestelmäId, oppilaitosOid: Option[Organisaatio.Oid]) extends OpiskeluoikeusIdentifier
case class OppijaOidOrganisaatioJaTyyppi(oppijaOid: String,
                                         oppilaitosOrganisaatio: String,
                                         koulutustoimija: Option[String],
                                         opiskeluoikeudenTyyppi: String,
                                         päätasonSuorituksenKoulutusmoduulinTyyppi: Option[String],
                                         päätasonSuorituksenSuoritustyyppi: Option[String],
                                         lähdejärjestelmäId: Option[LähdejärjestelmäId]) extends OpiskeluoikeusIdentifier
case class OpiskeluoikeusByOid(oid: String) extends OpiskeluoikeusIdentifier
