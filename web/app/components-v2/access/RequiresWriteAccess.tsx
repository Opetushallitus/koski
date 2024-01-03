import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'

export type RequiresWriteAccessProps = React.PropsWithChildren<{
  opiskeluoikeus: Opiskeluoikeus
}>

export const RequiresWriteAccess: React.FC<RequiresWriteAccessProps> = (
  props
) =>
  useVirkailijaUser()?.hasWriteAccess &&
  props.opiskeluoikeus.lähdejärjestelmänId === undefined ? (
    <>{props.children}</>
  ) : null
