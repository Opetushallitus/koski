import React from "react"
import { BrowserRouter as Router, Route, Switch } from "react-router-dom"
import { fetchOrganisaatiot } from "../api/api"
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

export default (props: ValpasAppProps) => {
  if (redirectToLoginReturnUrl()) {
    return null
  }

  const organisaatiot = useApiOnce(fetchOrganisaatiot)

  return isSuccess(organisaatiot) ? (
    <Router basename={process.env.PUBLIC_URL}>
      <Page id="valpas-app">
        <Switch>
          <Route exact path="/oppijat">
            <MainNavigation
              selected="hakutilanne"
              options={navOptions}
              onChange={() => null}
            />
            <PerusopetusView />
          </Route>
          <Route path="/oppijat/:oid" component={OppijaView} />
          <Route>
            <HomeView user={props.user} organisaatiot={organisaatiot.data} />
          </Route>
        </Switch>
      </Page>
    </Router>
  ) : null
}
