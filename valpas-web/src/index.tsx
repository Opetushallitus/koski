import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import {
  getCurrentUser,
  getLogin,
  hasValpasAccess,
  isLoggedIn,
} from "./state/auth"
import { t } from "./i18n/i18n"

declare global {
  interface Window {
    environment: string | undefined
    virkailija_raamit_set_to_load: boolean | undefined
  }
}

const runningLocally = window.environment == "local"

const Login = () => {
  const LocalLoginApp = React.lazy(() => import("./views/LoginApp"))
  const config = getLogin()

  if (config.type === "external") {
    config.redirectToVirkailijaLogin()
    return null
  }

  return (
    <React.Suspense fallback={<></>}>
      <LocalLoginApp onLogin={main} />
    </React.Suspense>
  )
}

async function main() {
  const user = await getCurrentUser()

  const LocalRaamit = React.lazy(
    () => import("./components/navigation/LocalRaamit")
  )

  const ValpasApp = React.lazy(() => import("./views/ValpasApp"))
  const ErrorView = React.lazy(() => import("./views/ErrorView"))

  ReactDOM.render(
    <React.Suspense fallback={<></>}>
      {runningLocally && !window.virkailija_raamit_set_to_load && (
        <LocalRaamit user={user} />
      )}
      {hasValpasAccess(user) ? (
        <ValpasApp />
      ) : isLoggedIn(user) ? (
        <ErrorView
          title={t("login__ei_valpas-oikeuksia_otsikko")}
          message={t("login__ei_valpas-oikeuksia_viesti")}
        />
      ) : (
        <Login />
      )}
    </React.Suspense>,
    document.getElementById("app")
  )
}

main()
