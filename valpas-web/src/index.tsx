import React from "react"
import ReactDOM from "react-dom"
import { enableFeature, Feature } from "./state/featureFlags"
import "./style/index.less"
import { ValpasApp } from "./views/ValpasApp"

declare global {
  interface Window {
    environment?: string
    virkailija_raamit_set_to_load?: boolean
    opintopolkuVirkailijaUrl?: string
    enableFeature?: (feature: Feature) => void
  }
}

async function main() {
  ReactDOM.render(<ValpasApp />, document.getElementById("app"))
}

main()

window.enableFeature = enableFeature
