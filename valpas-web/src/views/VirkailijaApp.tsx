import React from "react"
import { Redirect, Route, Switch } from "react-router-dom"
import { fetchYlatasonOrganisaatiotJaKayttooikeusroolit } from "../api/api"
import { useApiOnce } from "../api/apiHooks"
import { isSuccess } from "../api/apiUtils"
import { LoadingModal } from "../components/icons/Spinner"
import { t } from "../i18n/i18n"
import { KäyttöoikeusroolitProvider } from "../state/accessRights"
import {
  CurrentUser,
  getCurrentUser,
  getLogin,
  hasValpasAccess,
  isLoggedIn,
  redirectToLoginReturnUrl,
  storeLoginReturnUrl,
} from "../state/auth"
import { BasePathProvider, useBasePath } from "../state/basePath"
import { FeatureFlagEnabler } from "../state/featureFlags"
import {
  createHakutilannePathWithoutOrg,
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg,
  hakutilannePathWithOrg,
  hakutilannePathWithoutOrg,
  kunnanHetuhakuPath,
  kuntailmoitusPath,
  kuntailmoitusPathWithOrg,
  käyttöoikeusPath,
  maksuttomuusPath,
  nivelvaiheenHakutilannePathWithOrg,
  nivelvaiheenHakutilannePathWithoutOrg,
  oppijaPath,
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
  HakeutumisenKunnalleIlmoitetutView,
  HakeutumisenKunnalleIlmoitetutViewWithoutOrgOid,
} from "./kunnalleilmoitetut/HakeutumisenKunnalleIlmoitetutView"
import {
  SuorittamisenKunnalleIlmoitetutView,
  SuorittamisenKunnalleIlmoitetutViewWithoutOrgOid,
} from "./kunnalleilmoitetut/SuorittamisenKunnalleilmoitetutView"
import { KuntaHetuhaku } from "./kunta/hetuhaku/KuntaHetuhaku"
import {
  KuntailmoitusView,
  KuntailmoitusViewWithoutOrgOid,
} from "./kunta/kuntailmoitus/KuntailmoitusView"
import { MaksuttomuusView } from "./maksuttomuus/MaksuttomuusView"
import { OppijaView } from "./oppija/OppijaView"
import { Raamit } from "./Raamit"

const VirkailijaRoutes = () => {
  const basePath = useBasePath()

  const organisaatiotJaKayttooikeusroolit = useApiOnce(
    fetchYlatasonOrganisaatiotJaKayttooikeusroolit
  )

  if (!isSuccess(organisaatiotJaKayttooikeusroolit)) {
    return <LoadingModal />
  }

  return (
    <KäyttöoikeusroolitProvider value={organisaatiotJaKayttooikeusroolit.data}>
      <Switch>
        <Route exact path={`${basePath}/pilotti2021`}>
          <Redirect to={createHakutilannePathWithoutOrg(basePath)} />
        </Route>
        <Route exact path={`${basePath}/rouhinta`}>
          <FeatureFlagEnabler
            features={["rouhinta"]}
            redirectTo={createHakutilannePathWithoutOrg(basePath)}
          />
        </Route>
        <Route
          exact
          path={hakutilannePathWithoutOrg(basePath)}
          render={(routeProps) => (
            <HakutilanneViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={nivelvaiheenHakutilannePathWithoutOrg(basePath)}
          render={(routeProps) => (
            <NivelvaiheenHakutilanneViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg(basePath)}
          render={(routeProps) => (
            <HakeutumisenKunnalleIlmoitetutViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={hakutilannePathWithOrg(basePath)}
          render={(routeProps) => (
            <HakutilanneView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={nivelvaiheenHakutilannePathWithOrg(basePath)}
          render={(routeProps) => (
            <NivelvaiheenHakutilanneView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg(basePath)}
          render={(routeProps) => (
            <HakeutumisenKunnalleIlmoitetutView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={oppijaPath(basePath)}
          render={(routeProps) => (
            <OppijaView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={suorittamisvalvonnanKunnalleIlmoitetutPathWithoutOrg(basePath)}
          render={(routeProps) => (
            <SuorittamisenKunnalleIlmoitetutViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={suorittaminenPath(basePath)}
          render={(routeProps) => (
            <SuorittaminenOppivelvollisetViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={suorittamisvalvonnanKunnalleIlmoitetutPathWithOrg(basePath)}
          render={(routeProps) => (
            <SuorittamisenKunnalleIlmoitetutView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={suorittaminenPathWithOrg(basePath)}
          render={(routeProps) => (
            <SuorittaminenOppivelvollisetView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route exact path={suorittaminenHetuhakuPath(basePath)}>
          <SuorittaminenHetuhaku
            redirectUserWithoutAccessTo={rootPath(basePath)}
          />
        </Route>
        <Route exact path={maksuttomuusPath(basePath)}>
          <MaksuttomuusView redirectUserWithoutAccessTo={rootPath(basePath)} />
        </Route>
        <Route
          exact
          path={kuntailmoitusPath(basePath)}
          render={(routeProps) => (
            <KuntailmoitusViewWithoutOrgOid
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route
          exact
          path={kuntailmoitusPathWithOrg(basePath)}
          render={(routeProps) => (
            <KuntailmoitusView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route exact path={kunnanHetuhakuPath(basePath)}>
          <KuntaHetuhaku redirectUserWithoutAccessTo={rootPath(basePath)} />
        </Route>
        <Route exact path={käyttöoikeusPath(basePath)}>
          <AccessRightsView />
        </Route>
        <Route exact path={rootPath(basePath)}>
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

  const config = getLogin()

  if (config.type === "external") {
    config.redirectToVirkailijaLogin()
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
  const [user, setUser] = React.useState<CurrentUser | null>(null)
  React.useEffect(() => {
    ;(async () => {
      setUser(await getCurrentUser())
    })()
  }, [])

  if (!user) {
    return <LoadingModal />
  }

  if (isLoggedIn(user) && redirectToLoginReturnUrl()) {
    return <LoadingModal />
  }

  return (
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
  )
}

export default VirkailijaApp
