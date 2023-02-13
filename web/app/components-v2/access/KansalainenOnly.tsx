import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'

export const KansalainenOnly: React.FC<React.PropsWithChildren> = (props) => {
  const user = useVirkailijaUser()
  return user === null ? <>{props.children}</> : null
}
