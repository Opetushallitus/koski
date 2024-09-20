import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'
import { PropsWithOnlyChildren } from '../../util/react'

export const VirkailijaOnly: React.FC<PropsWithOnlyChildren> = (
  props
) => {
  const user = useVirkailijaUser()
  return user === null ? null : <>{props.children}</>
}
