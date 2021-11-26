import React from "react"
import {
  BrowserRouter as Router,
  Route,
  RouteComponentProps,
  Switch,
} from "react-router-dom"
import HealthView from "../views/HealthView"

export const ValpasApp = () => {
  const VirkailijaApp = React.lazy(() => import("./VirkailijaApp"))
  const OppijaApp = React.lazy(() => import("./OppijaApp"))

  return (
    <Router basename={process.env.PUBLIC_URL}>
      <Switch>
        <Route exact path="/health" component={HealthView} />
        <Route
          path="/virkailija"
          render={({ match: { path } }: RouteComponentProps) => (
            <React.Suspense fallback={<></>}>
              <VirkailijaApp basePath={path} />
            </React.Suspense>
          )}
        />
        <Route
          render={({ match: { path } }: RouteComponentProps) => (
            <React.Suspense fallback={<></>}>
              <OppijaApp basePath={path} />
            </React.Suspense>
          )}
        />
      </Switch>
    </Router>
  )
}
