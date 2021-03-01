import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"
import { ValpasApp } from "./views/ValpasApp"

declare global {
  interface Window {
    environment?: string
    virkailija_raamit_set_to_load?: boolean
    opintopolkuVirkailijaUrl?: string
  }
}

async function main() {
  ReactDOM.render(<ValpasApp />, document.getElementById("app"))
}

main()
