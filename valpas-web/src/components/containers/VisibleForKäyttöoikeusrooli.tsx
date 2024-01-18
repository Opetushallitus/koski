import React, { useMemo } from "react"
import { AccessGuard, useKäyttöoikeusroolit } from "../../state/accessRights"

export type VisibleForKäyttöoikeusrooliProps = {
  rooli: AccessGuard
  children: React.ReactNode
}

export const VisibleForKäyttöoikeusrooli = (
  props: VisibleForKäyttöoikeusrooliProps,
) => {
  const roolit = useKäyttöoikeusroolit()
  const checkRooli = props.rooli
  const visible = useMemo(() => checkRooli(roolit), [checkRooli, roolit])
  return visible ? <>{props.children}</> : null
}
