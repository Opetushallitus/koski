import React, { useEffect } from "react"
import { Route } from "react-router-dom"
import { CurrentUser } from "../state/auth"

const runningLocally = window.environment == "local"
const localRaamitEnabled = runningLocally && !process.env.VIRKAILIJA_RAAMIT_HOST

type RaamitProps = {
  user: CurrentUser
}

export const Raamit = (props: RaamitProps) => {
  return (
    <Route path="/virkailija">
      <VirkailijaRaamitLoader {...props} />
    </Route>
  )
}

const VirkailijaRaamitLoader = (props: RaamitProps) => {
  useEffect(loadExternalRaamitScript, [])
  return localRaamitEnabled ? <LocalRaamit user={props.user} /> : null
}

const LocalRaamit = React.lazy(
  () => import("../components/navigation/LocalRaamit")
)

let externalRaamitLoadInitiated = false

const loadExternalRaamitScript = () => {
  if (!externalRaamitLoadInitiated && !localRaamitEnabled) {
    externalRaamitLoadInitiated = true
    const script = document.createElement("script")
    script.src = "/virkailija-raamit/apply-raamit.js"
    document.head.appendChild(script)
  }
}
