import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'

export const RequiresWriteAccess: React.FC<React.PropsWithChildren> = (props) =>
  useVirkailijaUser()?.hasWriteAccess ? <>{props.children}</> : null
