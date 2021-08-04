import React from "react"
import { Redirect, Route, Switch } from "react-router-dom"
import { FeatureFlagEnabler } from "~state/featureFlags"
import {
  SuorittaminenOppivelvollisetView,
  SuorittaminenOppivelvollisetViewWithoutOrgOid,
} from "~views/suorittaminen/oppivelvolliset/SuorittaminenOppivelvollisetView"
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
import {
  createHakutilannePathWithoutOrg,
  hakutilannePathWithOrg,
  hakutilannePathWithoutOrg,
  kunnanHetuhakuPath,
  kuntailmoitusPath,
  kuntailmoitusPathWithOrg,
  käyttöoikeusPath,
  maksuttomuusPath,
  oppijaPath,
  rootPath,
  suorittaminenHetuhakuPath,
  suorittaminenPath,
  suorittaminenPathWithOrg,
} from "../state/paths"
import { SuorittaminenHetuhaku } from "../views/suorittaminen/hetuhaku/SuorittaminenHetuhaku"
import { AccessRightsView } from "./AccessRightsView"
import { ErrorView, NotFoundView } from "./ErrorView"
import {
  HakutilanneView,
  HakutilanneViewWithoutOrgOid,
} from "./hakutilanne/HakutilanneView"
import { HomeView } from "./HomeView"
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
        <Route exact path={`${basePath}/suorittamisenvalvontalista`}>
          <FeatureFlagEnabler
            features={["suorittamisenvalvontalista"]}
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
          path={suorittaminenPathWithOrg(basePath)}
          render={(routeProps) => (
            <SuorittaminenOppivelvollisetView
              redirectUserWithoutAccessTo={rootPath(basePath)}
              {...routeProps}
            />
          )}
        />
        <Route exact path={suorittaminenHetuhakuPath(basePath)}>
          <SuorittaminenHetuhaku />
        </Route>
        <Route exact path={maksuttomuusPath(basePath)}>
          <MaksuttomuusView />
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
          <KuntaHetuhaku />
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
