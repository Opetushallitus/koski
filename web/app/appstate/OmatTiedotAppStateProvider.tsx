import { Observable } from 'baconjs'
import React from 'react'
import { UserWithAccessRights } from '../types/fi/oph/koski/koskiuser/UserWithAccessRights'
import { GlobalErrorProvider } from './globalErrors'

export type OmatTiedotAppStateProviderProps = React.PropsWithChildren<{
  userP: Observable<UserWithAccessRights>
}>

export const OmatTiedotAppStateProvider: React.FC<
  OmatTiedotAppStateProviderProps
> = (props) => <GlobalErrorProvider>{props.children}</GlobalErrorProvider>
