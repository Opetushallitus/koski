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
  ) : (
    <OppijaRaamitLoader />
  )
}

const VirkailijaRaamitLoader = () => {
  useEffect(loadExternalVirkailijaRaamitScript, [])
  return null
}

const OppijaRaamitLoader = () => {
  useEffect(loadExternalOppijaRaamitScript, [])
  return null
}

const LocalRaamit = React.lazy(
  () => import("../components/navigation/LocalRaamit"),
)

let externalRaamitLoadInitiated = false

const loadExternalRaamitScript = (source: string) => () => {
  if (!externalRaamitLoadInitiated) {
    externalRaamitLoadInitiated = true
    const script = document.createElement("script")
    script.id = "apply-raamit"
    script.src = source
    document.head.appendChild(script)
  }
}

const loadExternalVirkailijaRaamitScript = loadExternalRaamitScript(
  "/virkailija-raamit/apply-raamit.js",
)
const loadExternalOppijaRaamitScript = loadExternalRaamitScript(
  "/oppija-raamit/js/apply-raamit.js",
)
