import React from "react"
import { Redirect, Route, Switch } from "react-router-dom"
import { fetchYlatasonOrganisaatiotJaKayttooikeusroolit } from "../api/api"
import { useApiOnce } from "../api/apiHooks"
import { isSuccess } from "../api/apiUtils"
import { LoadingModal } from "../components/icons/Spinner"
import { t } from "../i18n/i18n"
import { KäyttöoikeusroolitProvider } from "../state/accessRights"
import {
  getCurrentVirkailijaUser,
  getVirkailijaLogin,
  hasValpasAccess,
  isLoggedIn,
  storeLoginReturnUrl,
} from "../state/auth"
import { BasePathProvider, useBasePath } from "../state/basePath"
import { useUserLogin } from "../state/login"
import {
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg,
  hakutilannePathWithOrg,
  hakutilannePathWithoutOrg,
  kunnalleIlmoitetutPathWithOrg,
  kunnalleIlmoitetutPathWithoutOrg,
  kunnanHetuhakuPath,
  kuntailmoitusPath,
  kuntailmoitusPathWithOrg,
  kuntarouhintaPathWithOid,
  kuntarouhintaPathWithoutOid,
  käyttöoikeusPath,
  maksuttomuusPath,
  nivelvaiheenHakutilannePathWithOrg,
  nivelvaiheenHakutilannePathWithoutOrg,
  oppijaPath,
  PathDeclaration,
  rootPath,
  suorittaminenHetuhakuPath,
  suorittaminenPath,
  suorittaminenPathWithOrg,
  suorittamisvalvonnanKunnalleIlmoitetutPathWithOrg,
  suorittamisvalvonnanKunnalleIlmoitetutPathWithoutOrg,
} from "../state/paths"
import { SuorittaminenHetuhaku } from "../views/suorittaminen/hetuhaku/SuorittaminenHetuhaku"
import {
  SuorittaminenOppivelvollisetView,
  SuorittaminenOppivelvollisetViewWithoutOrgOid,
} from "../views/suorittaminen/oppivelvolliset/SuorittaminenOppivelvollisetView"
import { AccessRightsView } from "./AccessRightsView"
import { ErrorView, NotFoundView } from "./ErrorView"
import {
  HakutilanneView,
  HakutilanneViewWithoutOrgOid,
} from "./hakutilanne/HakutilanneView"
import {
  NivelvaiheenHakutilanneView,
  NivelvaiheenHakutilanneViewWithoutOrgOid,
} from "./hakutilanne/NivelvaiheenHakutilanneView"
import { HomeView } from "./HomeView"
import {
  YhdistettyKunnalleIlmoitetutView,
  YhdistettyKunnalleIlmoitetutViewWithoutOrgOid,
} from "./kunnalleilmoitetut/YhdistettyKunnalleIlmoitetutView"
import { KuntaHetuhaku } from "./kunta/hetuhaku/KuntaHetuhaku"
import {
  KuntailmoitusView,
  KuntailmoitusViewWithoutOrgOid,
} from "./kunta/kuntailmoitus/KuntailmoitusView"
import {
  KuntarouhintaView,
  KuntarouhintaViewWithoutOrg,
} from "./kunta/kuntarouhinta/KuntarouhintaView"
import { MaksuttomuusView } from "./maksuttomuus/MaksuttomuusView"
import { OppijaView } from "./oppija/OppijaView"
import { Raamit } from "./Raamit"
import { FeatureFlagEnabler } from "../state/featureFlags"

const redirects: Array<[PathDeclaration<any>, PathDeclaration<any>]> = [
  [
    hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg,
    kunnalleIlmoitetutPathWithoutOrg,
  ],
  [
    hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
    kunnalleIlmoitetutPathWithoutOrg,
  ],
  [
    suorittamisvalvonnanKunnalleIlmoitetutPathWithoutOrg,
    kunnalleIlmoitetutPathWithoutOrg,
  ],
  [
    suorittamisvalvonnanKunnalleIlmoitetutPathWithOrg,
    kunnalleIlmoitetutPathWithoutOrg,
  ],
]

const VirkailijaRoutes = () => {
  const basePath = useBasePath()

  const organisaatiotJaKayttooikeusroolit = useApiOnce(
    fetchYlatasonOrganisaatiotJaKayttooikeusroolit,
  )

  if (!isSuccess(organisaatiotJaKayttooikeusroolit)) {
    return <LoadingModal />
  }

  return (
    <KäyttöoikeusroolitProvider value={organisaatiotJaKayttooikeusroolit.data}>
      <Switch>
        {redirects.map(([from, to], index) => (
          <Route key={index} exact path={from.route(basePath)}>
            <Redirect to={to.href(basePath)} />
          </Route>
        ))}
        <Route exact path={`${basePath}/enable/kuntailmoitus-mitätöinti`}>
          <FeatureFlagEnabler
            features={["kuntailmoitusMitätöinti"]}
            redirectTo={rootPath.href(basePath)}
          />
        </Route>
        <Route
          exact
          path={hakutilannePathWithoutOrg.route(basePath)}
          render={(routeProps) => (
            <HakutilanneViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={nivelvaiheenHakutilannePathWithoutOrg.route(basePath)}
          render={(routeProps) => (
            <NivelvaiheenHakutilanneViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath.route(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={kunnalleIlmoitetutPathWithoutOrg.route(basePath)}
          render={(routeProps) => (
            <YhdistettyKunnalleIlmoitetutViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={kunnalleIlmoitetutPathWithOrg.route(basePath)}
          render={(routeProps) => (
            <YhdistettyKunnalleIlmoitetutView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />

        <Route
          exact
          path={hakutilannePathWithOrg.route(basePath)}
          render={(routeProps) => (
            <HakutilanneView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={nivelvaiheenHakutilannePathWithOrg.route(basePath)}
          render={(routeProps) => (
            <NivelvaiheenHakutilanneView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={oppijaPath.route(basePath)}
          render={(routeProps) => (
            <OppijaView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={suorittaminenPath.route(basePath)}
          render={(routeProps) => (
            <SuorittaminenOppivelvollisetViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={suorittaminenPathWithOrg.route(basePath)}
          render={(routeProps) => (
            <SuorittaminenOppivelvollisetView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route exact path={suorittaminenHetuhakuPath.route(basePath)}>
          <SuorittaminenHetuhaku
            redirectUserWithoutAccessTo={rootPath.href(basePath)}
          />
        </Route>
        <Route exact path={maksuttomuusPath.route(basePath)}>
          <MaksuttomuusView
            redirectUserWithoutAccessTo={rootPath.href(basePath)}
          />
        </Route>
        <Route
          exact
          path={kuntailmoitusPath.route(basePath)}
          render={(routeProps) => (
            <KuntailmoitusViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={kuntailmoitusPathWithOrg.route(basePath)}
          render={(routeProps) => (
            <KuntailmoitusView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route exact path={kunnanHetuhakuPath.route(basePath)}>
          <KuntaHetuhaku
            redirectUserWithoutAccessTo={rootPath.href(basePath)}
          />
        </Route>
        <Route
          exact
          path={kuntarouhintaPathWithOid.route(basePath)}
          render={(routeProps) => (
            <KuntarouhintaView
              redirectUserWithoutAccessTo={rootPath.href(basePath)}
              {...routeProps}
            />
          )}
        ></Route>
        <Route exact path={kuntarouhintaPathWithoutOid.route(basePath)}>
          <KuntarouhintaViewWithoutOrg
            redirectUserWithoutAccessTo={rootPath.href(basePath)}
          />
        </Route>
        <Route exact path={käyttöoikeusPath.route(basePath)}>
          <AccessRightsView />
        </Route>
        <Route exact path={rootPath.route(basePath)}>
          <HomeView />
        </Route>
        <Route component={NotFoundView} />
      </Switch>
    </KäyttöoikeusroolitProvider>
  )
}

const Login = () => {
  React.useEffect(() => {
    storeLoginReturnUrl(location.href)
  }, [])

  const login = getVirkailijaLogin()

  if (login.type === "external") {
    login.redirectToExternalLogin()
    return null
  }

  const LocalLoginApp = React.lazy(() => import("../views/LoginApp"))
  return (
    <React.Suspense fallback={<></>}>
      <LocalLoginApp />
    </React.Suspense>
  )
}

type VirkailijaAppProps = {
  basePath: string
}

const VirkailijaApp = ({ basePath }: VirkailijaAppProps) => {
  const user = useUserLogin(getCurrentVirkailijaUser)

  return user ? (
    <BasePathProvider value={basePath}>
      <Raamit user={user} />
      {isLoggedIn(user) ? (
        hasValpasAccess(user) ? (
          <div id="virkailija-app">
            <VirkailijaRoutes />
          </div>
        ) : (
          <ErrorView
            title={t("login__ei_valpas-oikeuksia_otsikko")}
            message={t("login__ei_valpas-oikeuksia_viesti")}
          />
        )
      ) : (
        <Login />
      )}
    </BasePathProvider>
  ) : (
    <LoadingModal />
  )
}

export default VirkailijaApp
