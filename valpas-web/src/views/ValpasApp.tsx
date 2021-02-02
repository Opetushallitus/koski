import React from "react"
import { BrowserRouter as Router, Route, Switch } from "react-router-dom"
import { fetchOrganisaatiot } from "../api/api"
import { isSuccessful, useApiOnce } from "../api/apiHooks"
import { Page } from "../components/containers/Page"
import { MainNavigation } from "../components/navigation/MainNavigation"
import { t } from "../i18n/i18n"
import { redirectToLoginReturnUrl } from "../state/auth"
import { User } from "../state/types"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"
import { HomeView } from "./HomeView"

export type ValpasAppProps = {
  user: User
}

const navOptions = [
  {
    key: "hakutilanne",
    display: t("ylänavi__hakutilanne"),
  },
]

export default (props: ValpasAppProps) => {
  if (redirectToLoginReturnUrl()) {
    return null
  }

  const organisaatiot = useApiOnce(fetchOrganisaatiot)

  return isSuccessful(organisaatiot) ? (
    <Router basename={process.env.PUBLIC_URL}>
      <Page id="valpas-app">
        <Switch>
          <Route path="/perusopetus">
            <MainNavigation
              selected="hakutilanne"
              options={navOptions}
              onChange={() => null}
            />
            <PerusopetusView />
          </Route>
          <Route>
            <HomeView user={props.user} organisaatiot={organisaatiot.data} />
          </Route>
        </Switch>
      </Page>
    </Router>
  ) : null
}
