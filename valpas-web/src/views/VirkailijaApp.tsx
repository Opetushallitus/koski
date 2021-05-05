import React from "react"
import { Route, Switch } from "react-router-dom"
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
  hasValpasAccess,
  isLoggedIn,
  redirectToLoginReturnUrl,
  storeLoginReturnUrl,
} from "../state/auth"
import { BasePathProvider, useBasePath } from "../state/basePath"
import { User } from "../state/common"
import { FeatureFlagEnabler, isFeatureFlagEnabled } from "../state/featureFlags"
import {
  createHakutilannePathWithoutOrg,
  hakutilannePathWithOrg,
  hakutilannePathWithoutOrg,
  oppijaPath,
  rootPath,
} from "../state/paths"
import { ErrorView, NotFoundView } from "./ErrorView"
import {
  HakutilanneView,
  HakutilanneViewWithoutOrgOid,
} from "./hakutilanne/HakutilanneView"
import { HomeView } from "./HomeView"
import { OppijaView } from "./oppija/OppijaView"
import { Raamit } from "./Raamit"

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
        <FeatureFlagEnabler
          feature="valpas"
          redirectTo={createHakutilannePathWithoutOrg(basePath)}
        />
      </Route>
      <Route exact path={`${basePath}/feature/ilmoittaminen`}>
        <FeatureFlagEnabler
          feature="ilmoittaminen"
          redirectTo={createHakutilannePathWithoutOrg(basePath)}
        />
      </Route>
      {isFeatureFlagEnabled("valpas") && (
        <Route
          exact
          path={hakutilannePathWithoutOrg(basePath)}
          render={(routeProps) => (
            <HakutilanneViewWithoutOrgOid
              kayttooikeusroolit={organisaatiotJaKayttooikeusroolit.data}
              {...routeProps}
            />
          )}
        />
      )}
      {isFeatureFlagEnabled("valpas") && (
        <Route
          exact
          path={hakutilannePathWithOrg(basePath)}
          render={(routeProps) => (
            <HakutilanneView
              kayttooikeusroolit={organisaatiotJaKayttooikeusroolit.data}
              {...routeProps}
            />
          )}
        />
      )}
      {isFeatureFlagEnabled("valpas") && (
        <Route exact path={oppijaPath(basePath)} component={OppijaView} />
      )}
      <Route exact path={rootPath(basePath)}>
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
          <Page id="virkailija-app">
            <VirkailijaRoutes user={user} />
          </Page>
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
