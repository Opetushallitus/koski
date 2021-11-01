import React from "react"
import { Redirect } from "react-router-dom"
import {
  hakeutumisenValvontaAllowed,
  kuntavalvontaAllowed,
  maksuttomuudenValvontaAllowed,
  suorittamisenValvontaAllowed,
  useKäyttöoikeusroolit,
} from "../state/accessRights"
import { useBasePath } from "../state/basePath"
import {
  hakutilannePathWithoutOrg,
  kuntailmoitusPath,
  maksuttomuusPath,
  suorittaminenPath,
} from "../state/paths"
import { AccessRightsView } from "./AccessRightsView"

export const HomeView = () => {
  const redirect = useRedirectPath()
  return redirect ? <Redirect to={redirect} /> : <AccessRightsView />
}

const useRedirectPath = (): string | null => {
  const basePath = useBasePath()
  const roles = useKäyttöoikeusroolit()

  if (kuntavalvontaAllowed(roles)) {
    return kuntailmoitusPath.href(basePath)
  }

  if (hakeutumisenValvontaAllowed(roles)) {
    return hakutilannePathWithoutOrg.href(basePath)
  }

  if (maksuttomuudenValvontaAllowed(roles)) {
    return maksuttomuusPath.href(basePath)
  }

  if (suorittamisenValvontaAllowed(roles)) {
    return suorittaminenPath.route(basePath)
  }

  return null
}
