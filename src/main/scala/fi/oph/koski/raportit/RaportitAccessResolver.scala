package fi.oph.koski.raportit

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema._

import scala.collection.JavaConverters._

object RaportitAccessResolver {
  def apply(application: KoskiApplication): RaportitAccessResolver = {
    RaportitAccessResolver(application.organisaatioRepository, application.raportointiDatabase, application.config)
  }
}

case class RaportitAccessResolver(organisaatioRepository: OrganisaatioRepository, raportointiDatabase: RaportointiDatabase, config: Config) {

  def checkAccess(oid: Organisaatio.Oid)(implicit session: KoskiSession): Either[HttpStatus, Organisaatio.Oid] = {
    if (!(session.hasRaportitAccess && session.hasReadAccess(oid))) {
      Left(KoskiErrorCategory.forbidden.organisaatio())
    } else {
      Right(oid)
    }
  }

  def kyselyOiditOrganisaatiolle(organisaatioOid: Organisaatio.Oid): Set[Organisaatio.Oid] = {
    organisaatioRepository.getOrganisaatio(organisaatioOid)
      .flatMap(childOidsIfKoulutustoimija)
      .getOrElse(Set.empty[Oid])
  }

  def availableRaportit(organisaatioOid: Organisaatio.Oid)(implicit session: KoskiSession): Set[Organisaatio.Oid] = {
    val organisaatio = organisaatioRepository.getOrganisaatio(organisaatioOid)
    val isKoulutustoimija = organisaatio.map(_.isInstanceOf[Koulutustoimija]).getOrElse(false)

    organisaatio
      .flatMap(childOidsIfKoulutustoimija)
      .map(raportointiDatabase.oppilaitostenKoulutusmuodot)
      .map(_.flatMap(raportitKoulutusmuodolle(_, isKoulutustoimija)))
      .map(_.filter(checkAccessIfAccessIsLimited(_)))
      .getOrElse(Set.empty[Oid])
  }

  private def childOidsIfKoulutustoimija(organisaatio: OrganisaatioWithOid) = organisaatio match {
    case koulutustoimija: Koulutustoimija => organisaatioRepository.getChildOids(koulutustoimija.oid)
    case oppilaitos: Oppilaitos  => Some(Set(oppilaitos.oid))
    case toimipiste: Toimipiste => Some(Set(toimipiste.oid))
    case _ => None
  }

  private def raportitKoulutusmuodolle(koulutusmuoto: String, isKoulutustoimija: Boolean) = koulutusmuoto match {
    case "ammatillinenkoulutus" if !isKoulutustoimija => Seq("opiskelijavuositiedot", "suoritustietojentarkistus", "ammatillinenosittainensuoritustietojentarkistus")
    case "perusopetus" => Seq("perusopetuksenvuosiluokka")
    case _ => Seq.empty
  }

  private def checkAccessIfAccessIsLimited(raportinNimi: String)(implicit session: KoskiSession) = {
    val rajatutRaportit = config.getConfigList("raportit.rajatut")
    val conf = rajatutRaportit.asScala.find(_.getString("name") == raportinNimi)
    conf match {
      case Some(c) => c.getStringList("whitelist").contains(session.oid)
      case _ => true
    }
  }
}
