import React, { useEffect } from "react"
import { Route } from "react-router-dom"
import { CurrentUser, isLoggedIn } from "../state/auth"
import { runningLocally } from "../utils/environment"

type RaamitProps = {
  user: CurrentUser
}

export const Raamit = (props: RaamitProps) => {
  const localRaamitEnabled =
    runningLocally() && !process.env.VIRKAILIJA_RAAMIT_HOST
  return (
    <Route path="/virkailija">
      {localRaamitEnabled ? (
        <LocalRaamit user={props.user} />
      ) : (
        isLoggedIn(props.user) && <VirkailijaRaamitLoader />
      )}
    </Route>
  )
}

export const KansalainenRaamit = (props: RaamitProps) => {
  const localRaamitEnabled = runningLocally() && !process.env.OPPIJA_RAAMIT_HOST
  return localRaamitEnabled ? (
    <LocalRaamit kansalainen user={props.user} />
  ) : null
}

const VirkailijaRaamitLoader = () => {
  useEffect(loadExternalRaamitScript, [])
  return null
}

const LocalRaamit = React.lazy(
  () => import("../components/navigation/LocalRaamit")
)

let externalRaamitLoadInitiated = false

const loadExternalRaamitScript = () => {
  if (!externalRaamitLoadInitiated) {
    externalRaamitLoadInitiated = true
    const script = document.createElement("script")
    script.src = "/virkailija-raamit/apply-raamit.js"
    document.head.appendChild(script)
  }
}
