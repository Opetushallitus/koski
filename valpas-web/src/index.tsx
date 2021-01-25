import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import { getCurrentUser, hasValpasAccess, isLoggedIn } from "./state/auth"
import { t } from "./i18n/i18n"

declare global {
  interface Window {
    environment: string | undefined
  }
}

const runningLocally = window.environment == "local"

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
      {runningLocally && <LocalRaamit user={user} />}
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
