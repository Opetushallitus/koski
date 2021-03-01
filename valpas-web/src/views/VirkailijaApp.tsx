import React from "react"
import { Redirect, Route, Switch } from "react-router-dom"
import { fetchYlatasonOrganisaatiotJaKayttooikeusroolit } from "../api/api"
import { useApiOnce } from "../api/apiHooks"
import { isSuccess } from "../api/apiUtils"
import { Page } from "../components/containers/Page"
import { MainNavigation } from "../components/navigation/MainNavigation"
import { t } from "../i18n/i18n"
import {
  CurrentUser,
  getCurrentUser,
  getLogin,
  hasValpasAccess,
  isLoggedIn,
  redirectToLoginReturnUrl,
  storeLoginReturnUrl,
} from "../state/auth"
import { User } from "../state/types"
import { ErrorView, NotFoundView } from "../views/ErrorView"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"
import { HomeView } from "./HomeView"
import { OppijaView } from "./oppija/OppijaView"

const featureFlagName = "valpas-feature"
const featureFlagEnabledValue = "enabled"

const FeatureFlagEnabler = (props: { virkailijaBasePath: string }) => {
  window.localStorage.setItem(featureFlagName, featureFlagEnabledValue)
  return <Redirect to={props.virkailijaBasePath} />
}

const runningLocally = window.environment == "local"

class FeatureRoute extends Route {
  public render() {
    const featureEnabled =
      window.localStorage.getItem(featureFlagName) === featureFlagEnabledValue
    if (featureEnabled || runningLocally) {
      return <Route {...this.props} />
    } else {
      return null
    }
  }
}

type VirkailijaRoutesProps = {
  basePath: string
  user: User
}

const VirkailijaRoutes = ({ basePath, user }: VirkailijaRoutesProps) => {
  const navOptions = [
    {
      key: "hakutilanne",
      display: t("ylänavi__hakutilanne"),
    },
  ]

  const organisaatiotJaKayttooikeusroolit = useApiOnce(
    fetchYlatasonOrganisaatiotJaKayttooikeusroolit
  )

  if (!isSuccess(organisaatiotJaKayttooikeusroolit)) {
    return null
  }

  return (
    <Switch>
      <Route exact path={`${basePath}/hunter2`}>
        <FeatureFlagEnabler virkailijaBasePath={basePath} />
      </Route>
      <FeatureRoute exact path={`${basePath}/oppijat`}>
        <MainNavigation
          selected="hakutilanne"
          options={navOptions}
          onChange={() => null}
        />
        <PerusopetusView />
      </FeatureRoute>
      <FeatureRoute
        exact
        path={`${basePath}/oppijat/:oid`}
        component={OppijaView}
      />
      <FeatureRoute exact path={`${basePath}/`}>
        <HomeView
          user={user}
          organisaatiotJaKayttooikeusroolit={
            organisaatiotJaKayttooikeusroolit.data
          }
        />
      </FeatureRoute>
      <Route component={NotFoundView} />
    </Switch>
  )
}

const LocalRaamit = React.lazy(
  () => import("../components/navigation/LocalRaamit")
)

const Login = () => {
  React.useEffect(() => {
    storeLoginReturnUrl()
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
    return null
  }

  if (redirectToLoginReturnUrl()) {
    return null
  }

  return (
    <>
      {runningLocally && !window.virkailija_raamit_set_to_load && (
        <LocalRaamit user={user} />
      )}
      {hasValpasAccess(user) ? (
        <Page id="virkailija-app">
          <VirkailijaRoutes user={user} basePath={basePath} />
        </Page>
      ) : isLoggedIn(user) ? (
        <ErrorView
          title={t("login__ei_valpas-oikeuksia_otsikko")}
          message={t("login__ei_valpas-oikeuksia_viesti")}
        />
      ) : (
        <Login />
      )}
    </>
  )
}

export default VirkailijaApp
