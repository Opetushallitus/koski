import React from "react"
import { Redirect, Route, Switch } from "react-router"
import { Page } from "../components/containers/Page"
import { LoadingModal } from "../components/icons/Spinner"
import { getCurrentKansalainenUser, hasValpasAccess } from "../state/auth"
import { BasePathProvider, useBasePath } from "../state/basePath"
import { User } from "../state/common"
import { FeatureFlagEnabler, isFeatureFlagEnabled } from "../state/featureFlags"
import { KansalainenContextProvider } from "../state/kansalainenContext"
import {
  kansalainenEiOpintopolussaPath,
  kansalainenLoginVirhePath,
  kansalainenOmatTiedotPath,
} from "../state/kansalainenPaths"
import { Login, useUserLogin } from "../state/login"
import { NotFoundView } from "./ErrorView"
import {
  KansalainenEiTietojaOpintopolussaView,
  KansalainenLoginErrorView,
} from "./kansalainen/kansalainenErrors"
import { KansalainenLandingView } from "./kansalainen/KansalainenLandingView"
import { KansalainenOmatJaHuollettavienTiedotView } from "./kansalainen/tiedot/KansalainenOmatJaHuollettavienTiedotView"
import { KansalainenRaamit } from "./Raamit"

const PublicKansalainenRoutes = () => {
  const basePath = useBasePath()
  return (
    <Switch>
      <Route exact path={kansalainenLoginVirhePath.route(basePath)}>
        <KansalainenLoginErrorView />
      </Route>
      <Route exact path={kansalainenEiOpintopolussaPath.route(basePath)}>
        <KansalainenEiTietojaOpintopolussaView />
      </Route>
      <Route exact path={basePath}>
        <KansalainenLandingView />
      </Route>
      <Route>
        <Login />
      </Route>
    </Switch>
  )
}

type ProtectedOppijaRoutesProps = {
  user: User
}

const ProtectedKansalainenRoutes = (props: ProtectedOppijaRoutesProps) => {
  const basePath = useBasePath()
  return (
    <Switch>
      <Route exact path="/enable/beta">
        <FeatureFlagEnabler
          features={["kansalainenBeta"]}
          redirectTo={kansalainenOmatTiedotPath.href(basePath)}
        />
      </Route>
      <Route exact path={basePath}>
        <Redirect to={kansalainenOmatTiedotPath.href(basePath)} />
      </Route>
      <Route exact path={kansalainenOmatTiedotPath.route(basePath)}>
        {isFeatureFlagEnabled("kansalainenBeta") ? (
          <KansalainenOmatJaHuollettavienTiedotView user={props.user} />
        ) : (
          <Page>Kansalaisen näkymä ei ole vielä käytössä</Page>
        )}
      </Route>
      <Route>
        <NotFoundView />
      </Route>
    </Switch>
  )
}

export type KansalainenAppProps = {
  basePath: string
}

export const KansalainenApp = (props: KansalainenAppProps) => {
  const user = useUserLogin(getCurrentKansalainenUser)
  return user ? (
    <BasePathProvider value={props.basePath}>
      <KansalainenContextProvider>
        <KansalainenRaamit user={user} />
        {hasValpasAccess(user) ? (
          <ProtectedKansalainenRoutes user={user} />
        ) : (
          <PublicKansalainenRoutes />
        )}
      </KansalainenContextProvider>
    </BasePathProvider>
  ) : (
    <LoadingModal />
  )
}

export default KansalainenApp
