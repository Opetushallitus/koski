import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import {
  getCurrentUser,
  getLogin,
  hasValpasAccess,
  isLoggedIn,
  User,
} from "./state/auth"
import { t } from "./i18n/i18n"

declare global {
  interface Window {
    environment: string | undefined
    virkailija_raamit_set_to_load: boolean | undefined
  }
}

const runningLocally = window.environment == "local"

// TODO: Make external login great again
function showLogin() {
  const config = getLogin()
  switch (config.type) {
    case "local":
      return showLocalLogin()
    case "external":
      return config.redirectToVirkailijaLogin()
  }
}

async function showLocalLogin() {
  const { LoginApp } = await import("./views/LoginApp")
  ReactDOM.render(<LoginApp onLogin={main} />, document.getElementById("app"))
}

async function main() {
  const user = await getCurrentUser()

  const LocalRaamit = React.lazy(
    () => import("./components/navigation/LocalRaamit")
  )

  const LoginApp = React.lazy(() => import("./views/LoginApp"))
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
        <LoginApp onLogin={main} />
      )}
    </React.Suspense>,
    document.getElementById("app")
  )
}

main()
