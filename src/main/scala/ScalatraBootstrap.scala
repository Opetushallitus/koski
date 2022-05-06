import fi.oph.koski.cache.CacheServlet
import fi.oph.koski.config.{Environment, KoskiApplication, RunMode}
import fi.oph.koski.documentation.{DocumentationApiServlet, DocumentationServlet, KoodistoServlet}
import fi.oph.koski.editor.{EditorKooditServlet, EditorServlet}
import fi.oph.koski.elasticsearch.ElasticSearchServlet
import fi.oph.koski.etk.ElaketurvakeskusServlet
import fi.oph.koski.fixture.FixtureServlet
import fi.oph.koski.frontendvalvonta.{FrontendValvontaMode, FrontendValvontaRaportointiServlet}
import fi.oph.koski.healthcheck.{HealthCheckApiServlet, HealthCheckHtmlServlet}
import fi.oph.koski.henkilo.HenkilötiedotServlet
import fi.oph.koski.history.KoskiHistoryServlet
import fi.oph.koski.kela.KelaServlet
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.KoskiSpecificLocalizationServlet
import fi.oph.koski.log.Logging
import fi.oph.koski.luovutuspalvelu.{LuovutuspalveluServlet, PalveluvaylaServlet, TilastokeskusServlet}
import fi.oph.koski.migri.MigriServlet
import fi.oph.koski.mydata.{ApiProxyServlet, MyDataReactServlet, MyDataServlet}
import fi.oph.koski.omaopintopolkuloki.OmaOpintoPolkuLokiServlet
import fi.oph.koski.omattiedot.{OmatTiedotHtmlServlet, OmatTiedotServlet}
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusServlet, OpiskeluoikeusValidationServlet}
import fi.oph.koski.oppija.{OppijaServlet, OppijaServletV2}
import fi.oph.koski.oppilaitos.OppilaitosServlet
import fi.oph.koski.oppivelvollisuustieto.OppivelvollisuustietoServlet
import fi.oph.koski.organisaatio.OrganisaatioServlet
import fi.oph.koski.permission.PermissionCheckServlet
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotServlet
import fi.oph.koski.preferences.PreferencesServlet
import fi.oph.koski.pulssi.{PulssiHtmlServlet, PulssiServlet}
import fi.oph.koski.raamit.RaamiProxyServlet
import fi.oph.koski.raportit.RaportitServlet
import fi.oph.koski.raportointikanta.{RaportointikantaService, RaportointikantaServlet}
import fi.oph.koski.servlet._
import fi.oph.koski.sso.{CasServlet, LocalLoginServlet, SSOConfig}
import fi.oph.koski.suoritusjako.{SuoritusjakoServlet, SuoritusjakoServletV2}
import fi.oph.koski.suostumus.SuostumuksenPeruutusServlet
import fi.oph.koski.sure.SureServlet
import fi.oph.koski.tiedonsiirto.TiedonsiirtoServlet
import fi.oph.koski.tutkinto.TutkinnonPerusteetServlet
import fi.oph.koski.util.{Futures, Timing}
import fi.oph.koski.valpas.kela.ValpasKelaServlet
import fi.oph.koski.valpas.valpasuser.ValpasLogoutServlet
import fi.oph.koski.valpas._
import fi.oph.koski.valpas.kansalainen.ValpasKansalainenApiServlet
import fi.oph.koski.valpas.sso.ValpasOppijaCasServlet
import fi.oph.koski.valvira.ValviraServlet
import fi.oph.koski.ytl.YtlServlet
import fi.oph.koski.ytr.{YtrKoesuoritusApiServlet, YtrKoesuoritusServlet}

import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with Timing {
  override def init(context: ServletContext): Unit = try {
    val application = Option(context.getAttribute("koski.application").asInstanceOf[KoskiApplication]).getOrElse(KoskiApplication.apply)

    RunMode.get match {
      case RunMode.NORMAL => initKoskiServices(context)(application)
      case RunMode.GENERATE_RAPORTOINTIKANTA => generateRaportointikanta(application)
    }
  } catch {
    case e: Exception =>
      logger.error(e)("Server startup failed: " + e.getMessage)
      System.exit(1)
  }

  private def initKoskiServices(context: ServletContext)(implicit application: KoskiApplication): Unit = {
    def mount(path: String, handler: Handler): Unit = context.mount(handler, path)

    val initTasks = application.init() // start parallel initialization tasks

    mount("/koski", new IndexServlet)
    mount("/koski/omattiedot", new OmatTiedotHtmlServlet)
    mount("/koski/login", new VirkailijaLoginPageServlet)
    mount("/koski/login/oppija", new OppijaLoginPageServlet)
    mount("/koski/pulssi", new PulssiHtmlServlet)
    mount("/koski/documentation", new RedirectServlet("/koski/dokumentaatio", true))
    mount("/koski/dokumentaatio", new DocumentationServlet)
    mount("/koski/eisuorituksia", new EiSuorituksiaServlet)
    mount("/koski/opinnot", new SuoritusjakoHtmlServlet)
    mount("/koski/virhesivu", new VirhesivuServlet)
    mount("/koski/api/documentation", new DocumentationApiServlet)
    mount("/koski/api/editor", new EditorServlet)
    mount("/koski/api/editor/koodit", new EditorKooditServlet)
    mount("/koski/api/elaketurvakeskus", new ElaketurvakeskusServlet)
    mount("/koski/api/elasticsearch", new ElasticSearchServlet)
    mount("/koski/api/healthcheck", new HealthCheckApiServlet)
    mount("/koski/api/status", new StatusApiServlet)
    mount("/koski/api/henkilo", new HenkilötiedotServlet)
    mount("/koski/api/koodisto", new KoodistoServlet)
    mount("/koski/api/omattiedot", new OmatTiedotServlet)
    mount("/koski/api/suoritusjako", new SuoritusjakoServlet)
    mount("/koski/api/suoritusjakoV2", new SuoritusjakoServletV2)
    mount("/koski/api/opiskeluoikeus", new OpiskeluoikeusServlet)
    mount("/koski/api/opiskeluoikeus/perustiedot", new OpiskeluoikeudenPerustiedotServlet)
    mount("/koski/api/opiskeluoikeus/validate", new OpiskeluoikeusValidationServlet)
    mount("/koski/api/opiskeluoikeus/historia", new KoskiHistoryServlet)
    mount("/koski/api/opiskeluoikeus/suostumuksenperuutus", new SuostumuksenPeruutusServlet)
    mount("/koski/api/oppija", new OppijaServlet)
    mount("/koski/api/v2/oppija", new OppijaServletV2)
    mount("/koski/api/oppilaitos", new OppilaitosServlet)
    mount("/koski/api/oppivelvollisuustieto", new OppivelvollisuustietoServlet)
    mount("/koski/api/organisaatio", new OrganisaatioServlet)
    mount("/koski/api/permission", new PermissionCheckServlet)
    mount("/koski/api/pulssi", new PulssiServlet)
    mount("/koski/api/preferences", new PreferencesServlet)
    mount("/koski/api/tiedonsiirrot", new TiedonsiirtoServlet)
    mount("/koski/api/tutkinnonperusteet", new TutkinnonPerusteetServlet)
    mount("/koski/api/localization", new KoskiSpecificLocalizationServlet)
    mount("/koski/api/raportit", new RaportitServlet)
    mount("/koski/api/raportointikanta", new RaportointikantaServlet)
    mount("/koski/api/sure", new SureServlet)
    mount("/koski/api/luovutuspalvelu", new LuovutuspalveluServlet)
    mount("/koski/api/luovutuspalvelu/valvira", new ValviraServlet)
    mount("/koski/api/luovutuspalvelu/kela", new KelaServlet)
    mount("/koski/api/luovutuspalvelu/migri", new MigriServlet)
    mount("/koski/api/luovutuspalvelu/ytl", new YtlServlet)
    mount("/koski/api/palveluvayla", new PalveluvaylaServlet)
    mount("/koski/api/luovutuspalvelu/haku", new TilastokeskusServlet)
    mount("/koski/api/omadata/oppija", new ApiProxyServlet)
    mount("/koski/api/omadata", new MyDataServlet)
    mount("/koski/api/omaopintopolkuloki", new OmaOpintoPolkuLokiServlet)
    mount("/koski/api/ytrkoesuoritukset", new YtrKoesuoritusApiServlet)
    mount("/koski/omadata", new MyDataReactServlet)
    mount("/koski/koesuoritus", new YtrKoesuoritusServlet)
    mount("/koski/healthcheck", new HealthCheckHtmlServlet)
    mount("/koski/user", new UserServlet)
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/koski/user/login", new LocalLoginServlet)
    }
    mount("/koski/user/logout", new KoskiSpecificLogoutServlet)
    mount("/koski/user/redirect", new LogoutRedirectServlet)
    mount("/koski/cas", new CasServlet)
    mount("/koski/cas/valpas", new ValpasOppijaCasServlet)
    mount("/koski/cache", new CacheServlet)

    mount("/koski/valpas/localization", new ValpasBootstrapServlet)
    mount("/koski/valpas/api", new ValpasRootApiServlet)
    mount("/koski/valpas/api/kuntailmoitus", new ValpasKuntailmoitusApiServlet)
    mount("/koski/valpas/api/luovutuspalvelu/kela", new ValpasKelaServlet)
    mount("/koski/valpas/api/rouhinta", new ValpasRouhintaApiServlet)
    mount("/koski/valpas/api/kansalainen", new ValpasKansalainenApiServlet)
    mount("/koski/valpas/api/luovutuspalvelu/ytl", new ValpasYtlServlet)
    mount("/koski/valpas/logout", new ValpasLogoutServlet)
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/koski/valpas/login", new LocalLoginServlet)
    }
    if (application.config.getString("opintopolku.virkailija.url") == "mock") {
      mount("/koski/valpas/test", new ValpasTestApiServlet)
    }

    if (FrontendValvontaMode(application.config.getString("frontend-valvonta.mode")) != FrontendValvontaMode.DISABLED) {
      mount("/koski/api/frontendvalvonta", new FrontendValvontaRaportointiServlet)
    }

    if (Environment.isLocalDevelopmentEnvironment(application.config) && application.config.hasPath("oppijaRaamitProxy")) {
      val proxyPrefix = "/oppija-raamit"
      mount(proxyPrefix, new RaamiProxyServlet(application.config.getString("oppijaRaamitProxy"), "", application))
    }

    if (Environment.isLocalDevelopmentEnvironment(application.config) && application.config.hasPath("virkailijaRaamitProxy")) {
      val proxyPrefix = "/virkailija-raamit"
      mount(proxyPrefix, new RaamiProxyServlet(application.config.getString("virkailijaRaamitProxy"), "", application))
    }

    Futures.await(initTasks) // await for all initialization tasks to complete

    if (application.fixtureCreator.shouldUseFixtures) {
      context.mount(new FixtureServlet, "/koski/fixtures")
      timed("Loading fixtures")(application.fixtureCreator.resetFixtures(reloadRaportointikanta = true))
    }
  }

  private def generateRaportointikanta(application: KoskiApplication): Unit = {
    val service = new RaportointikantaService(application)
    service.loadRaportointikantaAndExit(fullReload = RunMode.isFullReload)
  }

  override def destroy(context: ServletContext): Unit = ()
}
