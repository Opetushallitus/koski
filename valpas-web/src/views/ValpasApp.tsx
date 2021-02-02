import React from "react"
import { BrowserRouter as Router, Switch, Route } from "react-router-dom"
import { Page } from "../components/containers/Page"
import { MainNavigation } from "../components/navigation/MainNavigation"
import { t } from "../i18n/i18n"
import { redirectToLoginReturnUrl, User } from "../state/auth"
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

  return (
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
            <HomeView user={props.user} />
          </Route>
        </Switch>
      </Page>
    </Router>
  )
}
