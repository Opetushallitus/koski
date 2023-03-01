import React from 'react'
import { useKansalainenUser, useVirkailijaUser } from '../../appstate/user'

export const KansalainenOnly: React.FC<React.PropsWithChildren> = (props) => {
  const user = useKansalainenUser()
  return user ? <>{props.children}</> : null
}
