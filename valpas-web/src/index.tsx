import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import { getCurrentUser, hasValpasAccess, isLoggedIn, User } from "./state/auth"
import { t } from "./i18n/i18n"

async function main() {
  const user = await getCurrentUser()
  if (hasValpasAccess(user)) {
    showApp(user)
  } else if (isLoggedIn(user)) {
    showError(
      t("login__ei_valpas-oikeuksia_otsikko"),
      t("login__ei_valpas-oikeuksia_viesti")
    )
  } else {
    showLogin()
  }
}

async function showLogin() {
  const { LoginApp } = await import("./views/LoginApp")
  ReactDOM.render(<LoginApp onLogin={main} />, document.getElementById("app"))
}

async function showApp(_user: User) {
  const { ValpasApp } = await import("./views/ValpasApp")
  ReactDOM.render(<ValpasApp />, document.getElementById("app"))
}

async function showError(title: string, message: string) {
  const { ErrorView } = await import("./views/ErrorView")
  ReactDOM.render(
    <ErrorView title={title} message={message} />,
    document.getElementById("app")
  )
}

main()
