import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'

export type RequiresWriteAccessProps = React.PropsWithChildren<{
  opiskeluoikeus: Opiskeluoikeus
}>

export const useWriteAccess = (opiskeluoikeus: Opiskeluoikeus): boolean =>
  Boolean(useVirkailijaUser()?.hasWriteAccess) &&
  opiskeluoikeus.lähdejärjestelmänId === undefined

export const RequiresWriteAccess: React.FC<RequiresWriteAccessProps> = (
  props
) => (useWriteAccess(props.opiskeluoikeus) ? <>{props.children}</> : null)
