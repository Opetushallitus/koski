import React from "react"
import { Redirect, Route, Switch, useLocation } from "react-router-dom"
import { fetchYlatasonOrganisaatiotJaKayttooikeusroolit } from "../api/api"
import { useApiOnce } from "../api/apiHooks"
import { isSuccess } from "../api/apiUtils"
import { Page } from "../components/containers/Page"
import { LoadingModal } from "../components/icons/Spinner"
import { t } from "../i18n/i18n"
import {
  CurrentUser,
  getCurrentUser,
  getLogin,
  getLoginRedirectPath,
  hasValpasAccess,
  isLoggedIn,
  storeLoginReturnUrl,
} from "../state/auth"
import { BasePathProvider, useBasePath } from "../state/basePath"
import { User } from "../state/types"
import { ErrorView, NotFoundView } from "../views/ErrorView"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"
import { HomeView } from "./HomeView"
import { OppijaView } from "./oppija/OppijaView"

const featureFlagName = "valpas-feature"
const featureFlagEnabledValue = "enabled"

const FeatureFlagEnabler = () => {
  const basePath = useBasePath()
  window.localStorage.setItem(featureFlagName, featureFlagEnabledValue)
  return <Redirect to={`${basePath}/oppijat`} />
}

const runningLocally = window.environment == "local"

const isFeatureFlagEnabled = () =>
  runningLocally ||
  window.localStorage.getItem(featureFlagName) === featureFlagEnabledValue

type VirkailijaRoutesProps = {
  user: User
}

const VirkailijaRoutes = ({ user }: VirkailijaRoutesProps) => {
  const basePath = useBasePath()

  const organisaatiotJaKayttooikeusroolit = useApiOnce(
    fetchYlatasonOrganisaatiotJaKayttooikeusroolit
  )

  if (!isSuccess(organisaatiotJaKayttooikeusroolit)) {
    return <LoadingModal />
  }

  return (
    <Switch>
      <Route exact path={`${basePath}/pilotti2021`}>
        <FeatureFlagEnabler />
      </Route>
      {isFeatureFlagEnabled() && (
        <Route exact path={`${basePath}/oppijat`}>
          <PerusopetusView
            kayttooikeusroolit={organisaatiotJaKayttooikeusroolit.data}
          />
        </Route>
      )}
      {isFeatureFlagEnabled() && (
        <Route exact path={`${basePath}/oppijat/:oid`} component={OppijaView} />
      )}
      <Route exact path={`${basePath}/`}>
        <HomeView
          user={user}
          organisaatiotJaKayttooikeusroolit={
            organisaatiotJaKayttooikeusroolit.data
          }
        />
      </Route>
      <Route component={NotFoundView} />
    </Switch>
  )
}

const LocalRaamit = React.lazy(
  () => import("../components/navigation/LocalRaamit")
)

const Login = () => {
  const location = useLocation()

  React.useEffect(() => {
    storeLoginReturnUrl(location.pathname)
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

  const redirectPath = getLoginRedirectPath()

  return (
    <BasePathProvider value={basePath}>
      {runningLocally && !window.virkailija_raamit_set_to_load && (
        <LocalRaamit user={user} />
      )}
      {hasValpasAccess(user) ? (
        redirectPath ? (
          <Redirect to={redirectPath} />
        ) : (
          <Page id="virkailija-app">
            <VirkailijaRoutes user={user} />
          </Page>
        )
      ) : isLoggedIn(user) ? (
        <ErrorView
          title={t("login__ei_valpas-oikeuksia_otsikko")}
          message={t("login__ei_valpas-oikeuksia_viesti")}
        />
      ) : (
        <Login />
      )}
    </BasePathProvider>
  )
}

export default VirkailijaApp
