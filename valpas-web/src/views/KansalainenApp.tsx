import React from "react"
import { Redirect, Route, Switch } from "react-router"
import { LoadingModal } from "../components/icons/Spinner"
import { getCurrentKansalainenUser, hasValpasAccess } from "../state/auth"
import { BasePathProvider, useBasePath } from "../state/basePath"
import { User } from "../state/common"
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
import { KansalainenOmatTiedotView } from "./kansalainen/KansalainenOmatTiedotView"
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
      <Route exact path={basePath}>
        <Redirect to={kansalainenOmatTiedotPath.href(basePath)} />
      </Route>
      <Route exact path={kansalainenOmatTiedotPath.route(basePath)}>
        <KansalainenOmatTiedotView user={props.user} />
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
      <KansalainenRaamit user={user} />
      {hasValpasAccess(user) ? (
        <ProtectedKansalainenRoutes user={user} />
      ) : (
        <PublicKansalainenRoutes />
      )}
    </BasePathProvider>
  ) : (
    <LoadingModal />
  )
}

export default KansalainenApp
