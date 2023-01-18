import React from 'react'
import { ConstraintsProvider } from './constraints'
import { GlobalErrorProvider } from './globalErrors'
import { KoodistoProvider } from './koodisto'
import { OrganisaatioHierarkiaProvider } from './organisaatioHierarkia'

export const AppStateProvider: React.FC<React.PropsWithChildren> = (props) => (
  <GlobalErrorProvider>
    <KoodistoProvider>
      <OrganisaatioHierarkiaProvider>
        <ConstraintsProvider>{props.children}</ConstraintsProvider>
      </OrganisaatioHierarkiaProvider>
    </KoodistoProvider>
  </GlobalErrorProvider>
)
