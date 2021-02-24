import React from "react"
import {
  BrowserRouter as Router,
  Redirect,
  Route,
  Switch,
} from "react-router-dom"
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
import ErrorView from "../views/ErrorView"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"
import HealthView from "./HealthView"
import { HomeView } from "./HomeView"
import { OppijaView } from "./oppija/OppijaView"

const navOptions = [
  {
    key: "hakutilanne",
    display: t("ylänavi__hakutilanne"),
  },
]

const featureFlagName = "valpas-feature"
const featureFlagEnabledValue = "enabled"

const FeatureFlagEnabler = () => {
  window.localStorage.setItem(featureFlagName, featureFlagEnabledValue)
  return <Redirect to="/" />
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

const NotFoundView = () => (
  <ErrorView title={t("not_found_title")} message={t("not_found_teksti")} />
)

type VirkailijaRoutesProps = {
  path: string
  user: User
}

const VirkailijaRoutes = ({ path, user }: VirkailijaRoutesProps) => {
  const organisaatiotJaKayttooikeusroolit = useApiOnce(
    fetchYlatasonOrganisaatiotJaKayttooikeusroolit
  )

  if (!isSuccess(organisaatiotJaKayttooikeusroolit)) {
    return null
  }

  return (
    <Switch>
      <Route exact path={`${path}/hunter2`} component={FeatureFlagEnabler} />
      <FeatureRoute exact path={`${path}/oppijat`}>
        <MainNavigation
          selected="hakutilanne"
          options={navOptions}
          onChange={() => null}
        />
        <PerusopetusView />
      </FeatureRoute>
      <FeatureRoute
        exact
        path={`${path}/oppijat/:oid`}
        component={OppijaView}
      />
      <FeatureRoute exact path={`${path}/`}>
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
  storeLoginReturnUrl()
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

const VirkailijaApp = ({ match: { path } }: { match: { path: string } }) => {
  const [user, setUser] = React.useState<CurrentUser | null>(null)
  React.useEffect(() => {
    const fetchUser = async () => {
      setUser(await getCurrentUser())
    }
    fetchUser()
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
          <VirkailijaRoutes user={user} path={path} />
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

export default () => {
  return (
    <Router basename={process.env.PUBLIC_URL}>
      <Switch>
        <Route exact path="/health" component={HealthView} />
        <Route path="/virkailija" component={VirkailijaApp} />
        <Route component={NotFoundView} />
      </Switch>
    </Router>
  )
}
