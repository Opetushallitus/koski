import { Observable } from 'baconjs'
import React from 'react'
import { UserWithAccessRights } from '../types/fi/oph/koski/koskiuser/UserWithAccessRights'
import { ConstraintsProvider } from './constraints'
import { GlobalErrorProvider } from './globalErrors'
import { KoodistoProvider } from './koodisto'

export type OmatTiedotAppStateProviderProps = React.PropsWithChildren<{
  userP: Observable<UserWithAccessRights>
}>

export const OmatTiedotAppStateProvider: React.FC<
  OmatTiedotAppStateProviderProps
> = (props) => (
  <GlobalErrorProvider>
    <KoodistoProvider>
      <ConstraintsProvider>{props.children}</ConstraintsProvider>
    </KoodistoProvider>
  </GlobalErrorProvider>
)
