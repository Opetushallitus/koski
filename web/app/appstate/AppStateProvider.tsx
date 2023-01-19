import React from 'react'
import { ConstraintsProvider } from './constraints'
import { GlobalErrorProvider } from './globalErrors'
import { KoodistoProvider } from './koodisto'
import { OrganisaatioHierarkiaProvider } from './organisaatioHierarkia'
import { PreferencesProvider } from './preferences'

export const AppStateProvider: React.FC<React.PropsWithChildren> = (props) => (
  <GlobalErrorProvider>
    <KoodistoProvider>
      <OrganisaatioHierarkiaProvider>
        <PreferencesProvider>
          <ConstraintsProvider>{props.children}</ConstraintsProvider>
        </PreferencesProvider>
      </OrganisaatioHierarkiaProvider>
    </KoodistoProvider>
  </GlobalErrorProvider>
)
