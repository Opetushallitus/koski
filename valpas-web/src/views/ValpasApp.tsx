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
import { redirectToLoginReturnUrl } from "../state/auth"
import { User } from "../state/types"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"
import { HomeView } from "./HomeView"
import { OppijaView } from "./oppija/OppijaView"

export type ValpasAppProps = {
  user: User
}

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

export default (props: ValpasAppProps) => {
  if (redirectToLoginReturnUrl()) {
    return null
  }

  const organisaatiotJaKayttooikeusroolit = useApiOnce(
    fetchYlatasonOrganisaatiotJaKayttooikeusroolit
  )

  return isSuccess(organisaatiotJaKayttooikeusroolit) ? (
    <Router basename={process.env.PUBLIC_URL}>
      <Page id="valpas-app">
        <Switch>
          <Route exact path="/hunter2" component={FeatureFlagEnabler} />
          <FeatureRoute exact path="/oppijat">
            <MainNavigation
              selected="hakutilanne"
              options={navOptions}
              onChange={() => null}
            />
            <PerusopetusView />
          </FeatureRoute>
          <FeatureRoute path="/oppijat/:oid" component={OppijaView} />
          <FeatureRoute>
            <HomeView
              user={props.user}
              organisaatiotJaKayttooikeusroolit={
                organisaatiotJaKayttooikeusroolit.data
              }
            />
          </FeatureRoute>
        </Switch>
      </Page>
    </Router>
  ) : null
}
