import fi.oph.koski.cache.CacheServlet
import fi.oph.koski.config.{KoskiApplication, RunMode}
import fi.oph.koski.documentation.{DocumentationApiServlet, DocumentationServlet, KoodistoServlet}
import fi.oph.koski.editor.{EditorKooditServlet, EditorServlet}
import fi.oph.koski.elasticsearch.ElasticSearchServlet
import fi.oph.koski.etk.ElaketurvakeskusServlet
import fi.oph.koski.fixture.FixtureServlet
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

    mount("/", new IndexServlet)
    mount("/omattiedot", new OmatTiedotHtmlServlet)
    mount("/login", new VirkailijaLoginPageServlet)
    mount("/login/oppija", new OppijaLoginPageServlet)
    mount("/pulssi", new PulssiHtmlServlet)
    mount("/documentation", new RedirectServlet("/dokumentaatio", true))
    mount("/dokumentaatio", new DocumentationServlet)
    mount("/eisuorituksia", new EiSuorituksiaServlet)
    mount("/opinnot", new SuoritusjakoHtmlServlet)
    mount("/virhesivu", new VirhesivuServlet)
    mount("/api/documentation", new DocumentationApiServlet)
    mount("/api/editor", new EditorServlet)
    mount("/api/editor/koodit", new EditorKooditServlet)
    mount("/api/elaketurvakeskus", new ElaketurvakeskusServlet)
    mount("/api/elasticsearch", new ElasticSearchServlet)
    mount("/api/healthcheck", new HealthCheckApiServlet)
    mount("/api/status", new StatusApiServlet)
    mount("/api/henkilo", new HenkilötiedotServlet)
    mount("/api/koodisto", new KoodistoServlet)
    mount("/api/omattiedot", new OmatTiedotServlet)
    mount("/api/suoritusjako", new SuoritusjakoServlet)
    mount("/api/suoritusjakoV2", new SuoritusjakoServletV2)
    mount("/api/opiskeluoikeus", new OpiskeluoikeusServlet)
    mount("/api/opiskeluoikeus/perustiedot", new OpiskeluoikeudenPerustiedotServlet)
    mount("/api/opiskeluoikeus/validate", new OpiskeluoikeusValidationServlet)
    mount("/api/opiskeluoikeus/historia", new KoskiHistoryServlet)
    mount("/api/opiskeluoikeus/suostumuksenperuutus", new SuostumuksenPeruutusServlet)
    mount("/api/oppija", new OppijaServlet)
    mount("/api/v2/oppija", new OppijaServletV2)
    mount("/api/oppilaitos", new OppilaitosServlet)
    mount("/api/oppivelvollisuustieto", new OppivelvollisuustietoServlet)
    mount("/api/organisaatio", new OrganisaatioServlet)
    mount("/api/permission", new PermissionCheckServlet)
    mount("/api/pulssi", new PulssiServlet)
    mount("/api/preferences", new PreferencesServlet)
    mount("/api/tiedonsiirrot", new TiedonsiirtoServlet)
    mount("/api/tutkinnonperusteet", new TutkinnonPerusteetServlet)
    mount("/api/localization", new KoskiSpecificLocalizationServlet)
    mount("/api/raportit", new RaportitServlet)
    mount("/api/raportointikanta", new RaportointikantaServlet)
    mount("/api/sure", new SureServlet)
    mount("/api/luovutuspalvelu", new LuovutuspalveluServlet)
    mount("/api/luovutuspalvelu/valvira", new ValviraServlet)
    mount("/api/luovutuspalvelu/kela", new KelaServlet)
    mount("/api/luovutuspalvelu/migri", new MigriServlet)
    mount("/api/luovutuspalvelu/ytl", new YtlServlet)
    mount("/api/palveluvayla", new PalveluvaylaServlet)
    mount("/api/luovutuspalvelu/haku", new TilastokeskusServlet)
    mount("/api/omadata/oppija", new ApiProxyServlet)
    mount("/api/omadata", new MyDataServlet)
    mount("/api/omaopintopolkuloki", new OmaOpintoPolkuLokiServlet)
    mount("/api/ytrkoesuoritukset", new YtrKoesuoritusApiServlet)
    mount("/omadata", new MyDataReactServlet)
    mount("/koesuoritus", new YtrKoesuoritusServlet)
    mount("/healthcheck", new HealthCheckHtmlServlet)
    mount("/user", new UserServlet)
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/user/login", new LocalLoginServlet)
    }
    mount("/user/logout", new KoskiSpecificLogoutServlet)
    mount("/user/redirect", new LogoutRedirectServlet)
    mount("/cas", new CasServlet)
    mount("/cas/valpas", new ValpasOppijaCasServlet)
    mount("/cache", new CacheServlet)

    mount("/valpas/localization", new ValpasBootstrapServlet)
    mount("/valpas/api", new ValpasRootApiServlet)
    mount("/valpas/api/kuntailmoitus", new ValpasKuntailmoitusApiServlet)
    mount("/valpas/api/luovutuspalvelu/kela", new ValpasKelaServlet)
    mount("/valpas/api/rouhinta", new ValpasRouhintaApiServlet)
    mount("/valpas/api/kansalainen", new ValpasKansalainenApiServlet)
    mount("/valpas/api/luovutuspalvelu/ytl", new ValpasYtlServlet)
    mount("/valpas/logout", new ValpasLogoutServlet)
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/valpas/login", new LocalLoginServlet)
    }
    if (application.config.getString("opintopolku.virkailija.url") == "mock") {
      mount("/valpas/test", new ValpasTestApiServlet)
    }

    Futures.await(initTasks) // await for all initialization tasks to complete

    if (application.fixtureCreator.shouldUseFixtures) {
      context.mount(new FixtureServlet, "/fixtures")
      timed("Loading fixtures")(application.fixtureCreator.resetFixtures(reloadRaportointikanta = true))
    }
  }

  private def generateRaportointikanta(application: KoskiApplication): Unit = {
    val service = new RaportointikantaService(application)

    // HOTFIX: Pakotetaan full reload päälle väliaikaisesti
    //service.loadRaportointikantaAndExit(RunMode.fullReload)
    service.loadRaportointikantaAndExit(true)
  }

  override def destroy(context: ServletContext): Unit = ()
}
