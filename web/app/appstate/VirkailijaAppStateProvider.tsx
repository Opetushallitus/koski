import React from 'react'
import { UserWithAccessRights } from '../types/fi/oph/koski/koskiuser/UserWithAccessRights'
import { ConstraintsProvider } from './constraints'
import { GlobalErrorProvider } from './globalErrors'
import { KoodistoProvider } from './koodisto'
import { OrganisaatioHierarkiaProvider } from './organisaatioHierarkia'
import { PerusteProvider } from './peruste'
import { PreferencesProvider } from './preferences'
import { UserProvider } from './user'
import { OpiskeluoikeusProvider } from './opiskeluoikeus'
import { OphThemeProvider } from '@opetushallitus/oph-design-system/theme'

export type AppStateProviderProps = React.PropsWithChildren<{
  user: UserWithAccessRights
}>

export const VirkailijaAppStateProvider: React.FC<AppStateProviderProps> = (
  props
) => (
  <OphThemeProvider lang="fi" variant="oph">
    <GlobalErrorProvider>
      <UserProvider user={props.user} isKansalainen={false}>
        <PerusteProvider>
          <KoodistoProvider>
            <OrganisaatioHierarkiaProvider>
              <PreferencesProvider>
                <ConstraintsProvider>
                  <OpiskeluoikeusProvider>
                    {props.children}
                  </OpiskeluoikeusProvider>
                </ConstraintsProvider>
              </PreferencesProvider>
            </OrganisaatioHierarkiaProvider>
          </KoodistoProvider>
        </PerusteProvider>
      </UserProvider>
    </GlobalErrorProvider>
  </OphThemeProvider>
)
