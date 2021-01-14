import React from "react"
import ReactDOM from "react-dom"
import { getAuthState } from "./state/auth"
import "./style/index.less"

async function main() {
  if (getAuthState().loggedIn) {
    const { ValpasApp } = await import("./views/ValpasApp")
    render(ValpasApp)
  } else {
    const { LoginApp } = await import("./views/LoginApp")
    render(LoginApp)
  }
}

function render(View: React.ComponentType<{}>) {
  ReactDOM.render(<View />, document.getElementById("app"))
}

main()
