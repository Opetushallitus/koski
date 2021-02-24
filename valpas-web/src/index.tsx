import React from "react"
import ReactDOM from "react-dom"
import "./style/index.less"

declare global {
  interface Window {
    environment?: string
    virkailija_raamit_set_to_load?: boolean
    opintopolkuVirkailijaUrl?: string
  }
}

async function main() {
  const ValpasApp = React.lazy(() => import("./views/ValpasApp"))

  ReactDOM.render(
    <React.Suspense fallback={<></>}>
      <ValpasApp />
    </React.Suspense>,
    document.getElementById("app")
  )
}

main()
