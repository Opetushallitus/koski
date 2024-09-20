import React from 'react'
import { useBaconProperty } from '../util/hooks'
import { userP } from '../util/user'
import { GlobalErrorProvider } from './globalErrors'
import { UserProvider } from './user'
import { PropsWithOnlyChildren } from '../util/react'

export type OmatTiedotAppStateProviderProps = PropsWithOnlyChildren

export const OmatTiedotAppStateProvider: React.FC<
  OmatTiedotAppStateProviderProps
> = (props) => {
  const user = useBaconProperty(userP)
  return (
    <GlobalErrorProvider>
      <UserProvider user={user} isKansalainen>
        {props.children}
      </UserProvider>
    </GlobalErrorProvider>
  )
}
