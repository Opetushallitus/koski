import React from "react"
import { BrowserRouter as Router, Route, Switch } from "react-router-dom"
import { NotFoundView } from "../views/ErrorView"
import HealthView from "../views/HealthView"
import VirkailijaApp from "../views/VirkailijaApp"

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
