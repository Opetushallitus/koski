import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import { getCurrentUser, User } from "./state/auth"

async function main() {
  const user = await getCurrentUser()
  if (user) {
    showApp(user)
  } else {
    showLogin()
  }
}

async function showLogin() {
  const { LoginApp } = await import("./views/LoginApp")
  ReactDOM.render(
    <LoginApp onLogin={showApp} />,
    document.getElementById("app")
  )
}

async function showApp(_user: User) {
  const { ValpasApp } = await import("./views/ValpasApp")
  ReactDOM.render(<ValpasApp />, document.getElementById("app"))
}

main()
