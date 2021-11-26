import React from "react"
import { Route, Switch } from "react-router"
import { BasePathProvider } from "../state/basePath"
import { NotFoundView } from "./ErrorView"

const OppijaRoutes = () => (
  <Switch>
    <Route exact path="/">
      <div style={{ padding: 50 }}>Tähän tulee oppijanäkymä.</div>
    </Route>
    <Route>
      <NotFoundView />
    </Route>
  </Switch>
)

export type OppijaAppProps = {
  basePath: string
}

export const OppijaApp = (props: OppijaAppProps) => (
  <BasePathProvider value={props.basePath}>
    <OppijaRoutes />
  </BasePathProvider>
)

export default OppijaApp
