package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioHierarkia, OrganisaatioHierarkiaJaKayttooikeusrooli}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotKey
import fi.oph.koski.valpas.log.ValpasAuditLog.{auditLogHenkilöHaku, auditLogOppijaKatsominen, auditLogOppilaitosKatsominen, auditLogOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppilaitos
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasrepository.UusiOppivelvollisuudenKeskeytys
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s.JValue

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService
  private lazy val oppijaService = application.valpasOppijaService
  private lazy val oppijaSearchService = application.valpasOppijaSearchService

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

    val organisaatiokohtaiset = organisaatioService.omatOrganisaatiotJaKayttooikeusroolit

    globaalit ++ organisaatiokohtaiset
  }

  get("/oppijat/:organisaatio") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      oppijaService.getOppijatSuppeatTiedot(oppilaitosOid)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppija/:oid") {
    renderEither(
      oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(params("oid"))
        .tap(result => auditLogOppijaKatsominen(result.oppija.henkilö.oid))
    )
  }

  get("/henkilohaku/maksuttomuus/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilö(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS, query)
        .tap(auditLogHenkilöHaku(query))
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

  post("/oppija/ovkeskeytys") {
    withJsonBody { (body: JValue) => {
      val keskeytys = application
        .validatingAndResolvingExtractor
        .extract[UusiOppivelvollisuudenKeskeytys](strictDeserialization)(body)

      val result = keskeytys
        .flatMap(oppijaService.addOppivelvollisuudenKeskeytys)
        .tap(_ => keskeytys.tap(auditLogOppivelvollisuudenKeskeytys))

      renderEither(result)
    } } (parseErrorHandler = handleUnparseableJson)
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    haltWithStatus(status)
  }
}
