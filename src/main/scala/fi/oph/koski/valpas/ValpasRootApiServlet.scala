package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.{AuditLog, KoskiMessageField}
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioHierarkia, OrganisaatioHierarkiaJaKayttooikeusrooli}
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppilaitos
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.{RequiresValpasSession, ValpasSession}

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService
  private lazy val oppijaService = application.valpasOppijaService

  get("/user") {
    session.user
  }

  get("/organisaatiot-ja-kayttooikeusroolit") {
    val globaalit = session.globalKäyttöoikeudet.toList.flatMap(_.globalPalveluroolit.map(palvelurooli =>
      OrganisaatioHierarkiaJaKayttooikeusrooli(
        OrganisaatioHierarkia(Opetushallitus.organisaatioOid, Opetushallitus.nimi, List.empty, List.empty),
        palvelurooli.rooli
      )
    )).sortBy(r => (r.organisaatioHierarkia.nimi.get(session.lang), r.kayttooikeusrooli))

    val organisaatiokohtaiset = organisaatioService.omatOrganisaatiotJaKayttooikeusroolit.map(o =>
      o.copy(organisaatioHierarkia = o.organisaatioHierarkia.copy(children = List()))
    )

    globaalit ++ organisaatiokohtaiset
  }

  get("/oppijat/:organisaatio") {
    val oppilaitosOids: Set[ValpasOppilaitos.Oid] = Set(params("organisaatio"))
    renderEither(
      oppijaService.getOppijatSuppeatTiedot(oppilaitosOids)
        .map(withAuditLogOppilaitostenKatsominen(oppilaitosOids))
    )
  }

  get("/oppija/:oid") {
    renderEither(
      oppijaService.getOppijaHakutilanteillaLaajatTiedot(params("oid"))
        .map(withAuditLogOppijaKatsominen)
    )
  }

  private def withAuditLogOppijaKatsominen
    (result: OppijaHakutilanteillaLaajatTiedot)
    (implicit session: ValpasSession)
  : OppijaHakutilanteillaLaajatTiedot = {
    auditLogOppijaKatsominen(result.oppija.henkilö.oid)
    result
  }

  private def withAuditLogOppilaitostenKatsominen[T]
    (oppilaitosOids: Set[ValpasOppilaitos.Oid])(result: T)(implicit session: ValpasSession)
  : T = {
    oppilaitosOids.foreach { oppilaitosOid =>
      AuditLog.log(ValpasAuditLogMessage(
        ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
        Map(KoskiMessageField.juuriOrganisaatio -> oppilaitosOid)
      ))
    }
    result
  }
}
