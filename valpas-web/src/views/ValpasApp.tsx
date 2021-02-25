import React from "react"
import {
  BrowserRouter as Router,
  Route,
  RouteComponentProps,
  Switch,
} from "react-router-dom"
import { NotFoundView } from "../views/ErrorView"
import HealthView from "../views/HealthView"

export const ValpasApp = () => {
  const VirkailijaApp = React.lazy(() => import("./VirkailijaApp"))

  return (
    <Router basename={process.env.PUBLIC_URL}>
      <Switch>
        <Route exact path="/health" component={HealthView} />
        <Route
          path="/virkailija"
          render={({ match: { path } }: RouteComponentProps) => (
            <React.Suspense fallback={<></>}>
              <VirkailijaApp path={path} />
            </React.Suspense>
          )}
        />
        <Route component={NotFoundView} />
      </Switch>
    </Router>
  )
}
