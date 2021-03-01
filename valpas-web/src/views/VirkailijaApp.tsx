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
import { Raamit } from "./Raamit"

const featureFlagName = "valpas-feature"
const featureFlagEnabledValue = "enabled"

const FeatureFlagEnabler = (props: { virkailijaBasePath: string }) => {
  window.localStorage.setItem(featureFlagName, featureFlagEnabledValue)
  return <Redirect to={props.virkailijaBasePath} />
}

const runningLocally = window.environment == "local"

const isFeatureFlagEnabled = () =>
  runningLocally ||
  window.localStorage.getItem(featureFlagName) === featureFlagEnabledValue

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
      {isFeatureFlagEnabled() && (
        <>
          <Route exact path={`${basePath}/oppijat`}>
            <MainNavigation
              selected="hakutilanne"
              options={navOptions}
              onChange={() => null}
            />
            <PerusopetusView />
          </Route>
          <Route
            exact
            path={`${basePath}/oppijat/:oid`}
            component={OppijaView}
          />
          <Route exact path={`${basePath}/`}>
            <HomeView
              user={user}
              organisaatiotJaKayttooikeusroolit={
                organisaatiotJaKayttooikeusroolit.data
              }
            />
          </Route>
        </>
      )}
      <Route component={NotFoundView} />
    </Switch>
  )
}

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
      <Raamit user={user} />
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
