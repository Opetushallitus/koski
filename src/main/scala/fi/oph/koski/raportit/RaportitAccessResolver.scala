package fi.oph.koski.raportit

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.raportointikanta.RaportointiDatabase
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{Koulutustoimija, Oppilaitos, Organisaatio}

import scala.collection.JavaConverters._

object RaportitAccessResolver {
  def apply(application: KoskiApplication): RaportitAccessResolver = {
    RaportitAccessResolver(application.organisaatioRepository, application.raportointiDatabase, application.config)
  }
}

case class RaportitAccessResolver(organisaatioRepository: OrganisaatioRepository, raportointiDatabase: RaportointiDatabase, config: Config) {

  def checkAccess(oid: Organisaatio.Oid)(implicit session: KoskiSession): Either[HttpStatus, Organisaatio.Oid] = {
    if (!session.hasRaportitAccess) {
      Left(KoskiErrorCategory.forbidden.organisaatio())
    } else if (!session.hasReadAccess(oid)) {
      Left(KoskiErrorCategory.forbidden.organisaatio())
    } else {
      Right(oid)
    }
  }

  def availableRaportit(organisaatioOid: Organisaatio.Oid)(implicit session: KoskiSession): Set[String] = {
    val organisaatiOidit: Option[Set[Oid]] = organisaatioRepository.getOrganisaatio(organisaatioOid).map {
      case koulutustoimija: Koulutustoimija  => organisaatioRepository.getChildOids(koulutustoimija.oid).getOrElse(Set.empty)
      case oppilaitos: Oppilaitos => Set(oppilaitos.oid)
      case _ => Set.empty[Oid]
    }

    organisaatiOidit
      .map(raportointiDatabase.oppilaitostenKoulutusmuodot)
      .map(_.flatMap(raportitKoulutusmuodolle))
      .map(_.filter(checkAccessIfAccessIsLimited(_)))
      .getOrElse(Set.empty)
  }

  private def raportitKoulutusmuodolle(koulutusmuoto: String) = koulutusmuoto match {
    case "ammatillinenkoulutus" => Seq("opiskelijavuositiedot", "suoritustietojentarkistus", "ammatillinenosittainensuoritustietojentarkistus")
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
