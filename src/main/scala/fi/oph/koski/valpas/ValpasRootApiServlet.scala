package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.{AuditLog, KoskiMessageField}
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioHierarkia, OrganisaatioHierarkiaJaKayttooikeusrooli}
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotKey
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
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaService.getOppijatSuppeatTiedot(oppilaitosOid)
        .map(withAuditLogOppilaitostenKatsominen(oppilaitosOid))
    )
  }

  get("/oppija/:oid") {
    renderEither(
      oppijaService.getOppijaHakutilanteillaLaajatTiedot(params("oid"))
        .map(withAuditLogOppijaKatsominen)
    )
  }

  put("/oppija/:oid/set-muu-haku") {
    val oppijaOid = params("oid")
    val ooOid = getStringParam("opiskeluoikeusOid")
    val oppilaitosOid = getStringParam("oppilaitosOid")
    val value = getBooleanParam("value")

    val key = OpiskeluoikeusLisätiedotKey(
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = ooOid,
      oppilaitosOid = oppilaitosOid
    )
    oppijaService.setMuuHaku(key, value)
  }

  private def withAuditLogOppijaKatsominen
    (result: OppijaHakutilanteillaLaajatTiedot)
    (implicit session: ValpasSession)
  : OppijaHakutilanteillaLaajatTiedot = {
    auditLogOppijaKatsominen(result.oppija.henkilö.oid)
    result
  }

  private def withAuditLogOppilaitostenKatsominen[T]
    (oppilaitosOid: ValpasOppilaitos.Oid)(result: T)(implicit session: ValpasSession)
  : T = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      Map(KoskiMessageField.juuriOrganisaatio -> oppilaitosOid)
    ))
    result
  }
}
