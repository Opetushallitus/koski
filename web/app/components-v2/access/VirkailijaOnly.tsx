import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'

export const VirkailijaOnly: React.FC<React.PropsWithChildren> = (props) => {
  const user = useVirkailijaUser()
  return user === null ? null : <>{props.children}</>
}
