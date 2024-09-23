import React from 'react'
import { useKansalainenUser, useVirkailijaUser } from '../../appstate/user'
import { PropsWithOnlyChildren } from '../../util/react'

export const KansalainenOnly: React.FC<PropsWithOnlyChildren> = (
  props
) => {
  const user = useKansalainenUser()
  return user ? <>{props.children}</> : null
}
