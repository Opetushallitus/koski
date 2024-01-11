package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioHierarkia, OrganisaatioHierarkiaJaKayttooikeusrooli}
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.UuidUtils
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotKey
import fi.oph.koski.valpas.hakeutumisvalvonta.ValpasHakeutumisvalvontaService
import fi.oph.koski.valpas.log.ValpasAuditLog._
import fi.oph.koski.valpas.opiskeluoikeusrepository.{HakeutumisvalvontaTieto, ValpasOppilaitos}
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.suorittamisenvalvonta.ValpasSuorittamisenValvontaService
import fi.oph.koski.valpas.valpasrepository.{OppivelvollisuudenKeskeytyksenMuutos, UusiOppivelvollisuudenKeskeytys}
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s.JValue

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService
  private lazy val oppijaLaajatTiedotService = application.valpasOppijaLaajatTiedotService
  private lazy val oppijaSearchService = application.valpasOppijaSearchService
  private lazy val oppivelvollisuudenKeskeytysService = application.valpasOppivelvollisuudenKeskeytysService
  private lazy val raportitService = new RaportitService(application)

  private val hakeutumisvalvontaService = new ValpasHakeutumisvalvontaService(application)
  private val suorittamisenValvontaService = new ValpasSuorittamisenValvontaService(application)

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
      hakeutumisvalvontaService.getOppijatSuppeatTiedot(
        oppilaitosOid,
        HakeutumisvalvontaTieto.Perusopetus,
      )
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/paivitysaika") {
    raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika
  }

  post("/oppijat/:organisaatio/hakutiedot") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    withJsonBody { (body: JValue) => {
      val oppijat = application
        .validatingAndResolvingExtractor
        .extract[Oppijalista](strictDeserialization)(body)

      renderEither(oppijat.flatMap(o =>
        hakeutumisvalvontaService.getOppijatSuppeatTiedot(
          oppilaitosOid,
          HakeutumisvalvontaTieto.Perusopetus,
          haeHakutilanteet = o.oppijaOids,
        ).tap(_ => auditLogOppilaitosKatsominenHakutiedoilla(oppilaitosOid, o.oppijaOids))
      ))
    } } (parseErrorHandler = handleUnparseableJson)
  }

  get("/oppijat-nivelvaihe/:organisaatio") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      hakeutumisvalvontaService.getOppijatSuppeatTiedot(
        oppilaitosOid,
        HakeutumisvalvontaTieto.Nivelvaihe,
      )
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  post("/oppijat-nivelvaihe/:organisaatio/hakutiedot") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    withJsonBody { (body: JValue) => {
      val oppijat = application
        .validatingAndResolvingExtractor
        .extract[Oppijalista](strictDeserialization)(body)

      renderEither(oppijat.flatMap(o =>
        hakeutumisvalvontaService.getOppijatSuppeatTiedot(
          oppilaitosOid,
          HakeutumisvalvontaTieto.Nivelvaihe,
          haeHakutilanteet = o.oppijaOids,
        ).tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
      ))
    } } (parseErrorHandler = handleUnparseableJson)
  }

  get("/oppijat/:organisaatio/ilmoitukset") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      hakeutumisvalvontaService.getKunnalleTehdytIlmoituksetSuppeatTiedot(oppilaitosOid)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppijat-suorittaminen/:organisaatio") {
    val oppilaitosOid: ValpasOppilaitos.Oid = params("organisaatio")
    renderEither(
      suorittamisenValvontaService.getOppijatSuppeatTiedot(oppilaitosOid)
        .tap(_ => auditLogOppilaitosKatsominen(oppilaitosOid))
    )
  }

  get("/oppija/:oid") {
    renderEither(
      oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(params("oid"))
        .tap(result => auditLogOppijaKatsominen(result.oppija.henkilö.oid))
    )
  }

  get("/henkilohaku/maksuttomuus/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilöMaksuttomuus(query)
        .map(_.cleanUpForUserSearch)
        .tap(auditLogHenkilöHaku(query))
    )
  }

  get("/henkilohaku/suorittaminen/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilöSuorittaminen(query)
        .map(_.cleanUpForUserSearch)
        .tap(auditLogHenkilöHaku(query))
    )
  }

  get("/henkilohaku/kunta/:query") {
    val query = params("query")
    renderEither(
      oppijaSearchService.findHenkilöKunta(query)
        .map(_.cleanUpForUserSearch)
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
    oppijaLaajatTiedotService.setMuuHaku(key, value)
  }

  post("/oppija/ovkeskeytys") {
    withJsonBody { (body: JValue) => {
      val keskeytys = application
        .validatingAndResolvingExtractor
        .extract[UusiOppivelvollisuudenKeskeytys](strictDeserialization)(body)

      val result = keskeytys
        .flatMap(oppivelvollisuudenKeskeytysService.addOppivelvollisuudenKeskeytys)
        .tap(_ => keskeytys.tap(auditLogOppivelvollisuudenKeskeytys))

      renderEither(result)
    } } (parseErrorHandler = handleUnparseableJson)
  }

  put("/oppija/ovkeskeytys") {
    withJsonBody { (body: JValue) => {
      val keskeytys = application
        .validatingAndResolvingExtractor
        .extract[OppivelvollisuudenKeskeytyksenMuutos](strictDeserialization)(body)

      val result = keskeytys
        .flatMap(oppivelvollisuudenKeskeytysService.updateOppivelvollisuudenKeskeytys)
        .tap(k => auditLogOppivelvollisuudenKeskeytysUpdate(k._1.oppijaOid, k._1.tekijäOrganisaatioOid))
        .map(k => k._2)

      renderEither(result)
    } } (parseErrorHandler = handleUnparseableJson)
  }

  delete("/oppija/ovkeskeytys/:uuid") {
    val result = UuidUtils.optionFromString(params("uuid"))
      .toRight(ValpasErrorCategory.badRequest.validation.epävalidiUuid())
      .flatMap(oppivelvollisuudenKeskeytysService.deleteOppivelvollisuudenKeskeytys)
      .tap(k => auditLogOppivelvollisuudenKeskeytysDelete(k._1.oppijaOid, k._1.tekijäOrganisaatioOid))
      .map(k => k._2)

    renderEither(result)
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    haltWithStatus(status)
  }
}

case class Oppijalista(
  oppijaOids: List[String],
)
