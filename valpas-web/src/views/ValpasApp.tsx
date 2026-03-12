import React from "react"
import {
  BrowserRouter as Router,
  Route,
  RouteComponentProps,
  Switch,
} from "react-router-dom"
import HealthView from "../views/HealthView"
import { NotFoundView } from "./ErrorView"
import {
  kansalainenEiOpintopolussaPath,
  kansalainenLoginVirhePath,
  kansalainenOmatTiedotPath,
} from "../state/kansalainenPaths"

export const ValpasApp = () => {
  const VirkailijaApp = React.lazy(() => import("./VirkailijaApp"))
  const KansalainenApp = React.lazy(() => import("./KansalainenApp"))

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
          path={[
            kansalainenOmatTiedotPath.route(),
            kansalainenLoginVirhePath.route(),
            kansalainenEiOpintopolussaPath.route(),
          ]}
          render={() => (
            <React.Suspense fallback={<></>}>
              <KansalainenApp basePath="/" />
            </React.Suspense>
          )}
        />
        <Route
          exact
          path="/"
          render={() => (
            <React.Suspense fallback={<></>}>
              <KansalainenApp basePath="/" />
            </React.Suspense>
          )}
        />
        <Route>
          <NotFoundView />
        </Route>
      </Switch>
    </Router>
  )
}
