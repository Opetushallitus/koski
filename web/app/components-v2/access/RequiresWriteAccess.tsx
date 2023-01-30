import React from 'react'
import { useUser } from '../../appstate/user'

export const RequiresWriteAccess: React.FC<React.PropsWithChildren> = (props) =>
  useUser()?.hasWriteAccess ? <>{props.children}</> : null
