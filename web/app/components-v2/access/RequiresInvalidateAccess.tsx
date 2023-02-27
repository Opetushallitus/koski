import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'

export const RequiresInvalidateAccess: React.FC<React.PropsWithChildren> = (
  props
) =>
  useVirkailijaUser()?.hasAnyInvalidateAccess ? <>{props.children}</> : null
