package fi.oph.koski.raportit

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.raportit.tuva.TuvaPerusopetuksenOppijamäärätRaportti
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

  def kyselyOiditOrganisaatiolle(organisaatioOid: Organisaatio.Oid): Set[Organisaatio.Oid] = {
    organisaatioRepository.getOrganisaatio(organisaatioOid)
      .flatMap(organisaatioWithOid => organisaatioRepository.getChildOids(organisaatioWithOid.oid))
      .getOrElse(Set.empty[Oid])
  }

  def kyselyOiditOrganisaatiolle(organisaatioOid: Organisaatio.Oid, koulutusmuoto: String): Seq[Organisaatio.Oid] = {
    filterOppilaitosOidsByKoulutusmuoto(kyselyOiditOrganisaatiolle(organisaatioOid).toSeq, koulutusmuoto)
  }

  def mahdollisetRaporttienTyypitOrganisaatiolle(organisaatioHierarkia: OrganisaatioHierarkia, koulutusmuodot: Map[String, Seq[String]])(implicit session: KoskiSpecificSession): Set[RaportinTyyppi] = {
    val isKoulutustoimija = organisaatioHierarkia.toOrganisaatio.isInstanceOf[Koulutustoimija]

    OrganisaatioHierarkia.flatten(List(organisaatioHierarkia))
      .map(_.oid)
      .flatMap(koulutusmuodot.get)
      .flatten
      .toSet
      .flatMap(raportinTyypitKoulutusmuodolle(_, isKoulutustoimija))
      .filter(checkRaporttiAccessIfAccessIsLimited(_))
      .filter(raportti => session.allowedOpiskeluoikeusTyypit.contains(raportti.opiskeluoikeudenTyyppi))
  }

  // TODO: Tarpeeton kun uusi raporttikäli saadaan käyttöön, voi poistaa
  def mahdollisetRaporttienTyypitOrganisaatiolle(organisaatioOid: Organisaatio.Oid)(implicit session: KoskiSpecificSession): Set[RaportinTyyppi] = {
    val organisaatio = organisaatioRepository.getOrganisaatio(organisaatioOid)
    val isKoulutustoimija = organisaatio.exists(_.isInstanceOf[Koulutustoimija])

    organisaatio
      .flatMap(organisaatioWithOid => organisaatioRepository.getChildOids(organisaatioWithOid.oid))
      .map(raportointiDatabase.oppilaitostenKoulutusmuodot)
      .map(_.flatMap(raportinTyypitKoulutusmuodolle(_, isKoulutustoimija)))
      .map(_.filter(checkRaporttiAccessIfAccessIsLimited(_)))
      .map(_.filter(raportti => session.allowedOpiskeluoikeusTyypit.contains(raportti.opiskeluoikeudenTyyppi)))
      .getOrElse(Set.empty[RaportinTyyppi])
  }

  private def filterOppilaitosOidsByKoulutusmuoto(oppilaitosOids: Seq[String], koulutusmuoto: String): Seq[String] = {
    val query =
      sql"""
        select distinct organisaatio_oid
        from r_organisaatio o
        join r_opiskeluoikeus oo
          on o.organisaatio_oid = oo.oppilaitos_oid
          and oo.koulutusmuoto = $koulutusmuoto
        where organisaatio_oid = any($oppilaitosOids)"""
    raportointiDatabase.runDbSync(query.as[Organisaatio.Oid])
  }

  private def raportinTyypitKoulutusmuodolle(koulutusmuoto: String, isKoulutustoimija: Boolean) = koulutusmuoto match {
    case "ammatillinenkoulutus" if !isKoulutustoimija => Seq(
      AmmatillinenOpiskelijavuositiedot,
      AmmatillinenTutkintoSuoritustietojenTarkistus,
      AmmatillinenOsittainenSuoritustietojenTarkistus,
      MuuAmmatillinenKoulutus,
      TOPKSAmmatillinen
    )
    case "perusopetus" => Seq(PerusopetuksenVuosiluokka, PerusopetuksenOppijaMääräRaportti)
    case "perusopetuksenlisaopetus" => Seq(PerusopetuksenLisäopetuksenOppijaMääräRaportti)
    case "lukiokoulutus" if !isKoulutustoimija => Seq(LukionSuoritustietojenTarkistus, LukioDiaIbInternationalESHOpiskelijamaarat, LukioKurssikertyma, LukioOpintopistekertyma)
    case "lukiokoulutus" => Seq(LukioDiaIbInternationalESHOpiskelijamaarat, LukioKurssikertyma, LukioOpintopistekertyma)
    case "ibtutkinto" if !isKoulutustoimija => Seq(LukioDiaIbInternationalESHOpiskelijamaarat, IBSuoritustietojenTarkistus)
    case "ibtutkinto" => Seq(LukioDiaIbInternationalESHOpiskelijamaarat)
    case "diatutkinto" => Seq(LukioDiaIbInternationalESHOpiskelijamaarat)
    case "internationalschool" => Seq(LukioDiaIbInternationalESHOpiskelijamaarat)
    case "europeanschoolofhelsinki" => Seq(LukioDiaIbInternationalESHOpiskelijamaarat)
    case "esiopetus" => Seq(EsiopetuksenRaportti, EsiopetuksenOppijaMäärienRaportti)
    case "aikuistenperusopetus" if !isKoulutustoimija => Seq(AikuistenPerusopetusSuoritustietojenTarkistus, AikuistenPerusopetusOppijaMäärienRaportti, AikuistenPerusopetusKurssikertymänRaportti)
    case "aikuistenperusopetus" => Seq(AikuistenPerusopetusOppijaMäärienRaportti, AikuistenPerusopetusKurssikertymänRaportti)
    case "luva" => Seq(LuvaOpiskelijamaarat)
    case "perusopetukseenvalmistavaopetus" if !isKoulutustoimija => Seq(PerusopetukseenValmistavanOpetuksenTarkistus)
    case "tuva" if !isKoulutustoimija => Seq(TuvaSuoritustietojenTarkistus, TuvaPerusopetuksenOppijaMääräRaportti)
    case "tuva" => Seq(TuvaPerusopetuksenOppijaMääräRaportti)
    case _ => Seq.empty[RaportinTyyppi]
  }

  private def checkRaporttiAccessIfAccessIsLimited(raportti: RaportinTyyppi)(implicit session: KoskiSpecificSession) = {
    val rajatutRaportit = config.getConfigList("raportit.rajatut")
    val conf = rajatutRaportit.asScala.find(_.getString("name") == raportti.toString)
    conf match {
      case Some(c) => c.getStringList("whitelist").contains(session.oid)
      case _ => true
    }
  }
}
