import React from 'react'
import { UserWithAccessRights } from '../types/fi/oph/koski/koskiuser/UserWithAccessRights'
import { ConstraintsProvider } from './constraints'
import { GlobalErrorProvider } from './globalErrors'
import { KoodistoProvider } from './koodisto'
import { OrganisaatioHierarkiaProvider } from './organisaatioHierarkia'
import { PreferencesProvider } from './preferences'
import { UserProvider } from './user'

export type AppStateProviderProps = React.PropsWithChildren<{
  user: UserWithAccessRights
}>

export const VirkailijaAppStateProvider: React.FC<AppStateProviderProps> = (
  props
) => (
  <GlobalErrorProvider>
    <UserProvider user={props.user} isKansalainen={false}>
      <KoodistoProvider>
        <OrganisaatioHierarkiaProvider>
          <PreferencesProvider>
            <ConstraintsProvider>{props.children}</ConstraintsProvider>
          </PreferencesProvider>
        </OrganisaatioHierarkiaProvider>
      </KoodistoProvider>
    </UserProvider>
  </GlobalErrorProvider>
)
