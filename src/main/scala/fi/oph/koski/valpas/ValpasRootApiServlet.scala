package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioHierarkia, OrganisaatioHierarkiaJaKayttooikeusrooli}
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService
  private lazy val oppijaService = new ValpasOppijaService(application)

  get("/user") {
    session.user
  }

  get("/organisaatiot-ja-kayttooikeusroolit") {
    val globaalit = session.globalKäyttöoikeudet.toList.flatMap(_.globalPalveluroolit.map(palvelurooli =>
      OrganisaatioHierarkiaJaKayttooikeusrooli(OrganisaatioHierarkia(Opetushallitus.organisaatioOid, Opetushallitus.nimi, List.empty, List.empty), palvelurooli.rooli)
    )).sortBy(r => (r.organisaatioHierarkia.nimi.get(session.lang), r.kayttooikeusrooli))

    val organisaatiokohtaiset = organisaatioService.omatOrganisaatiotJaKayttooikeusroolit.map(o => o.copy(organisaatioHierarkia = o.organisaatioHierarkia.copy(children = List())))

    globaalit ++ organisaatiokohtaiset
  }

  get("/oppijat") {
    haltWithStatus(ValpasErrorCategory.forbidden.oppijat())
    // TODO: Ei vielä loppuun asti toteutettu
    //    renderEither(
    //      oppijaService.getOppijat
    //      .toRight(ValpasErrorCategory.forbidden.oppijat()))
  }

  get("/oppija/:oid") {
    renderEither(
      oppijaService.getOppija(params("oid"))
      .toRight(ValpasErrorCategory.forbidden.oppija()))
  }
}
