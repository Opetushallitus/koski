import React from 'react'
import { ConstraintsProvider } from './constraints'
import { GlobalErrorProvider } from './globalErrors'
import { KoodistoProvider } from './koodisto'

export const AppStateProvider: React.FC<React.PropsWithChildren> = (props) => (
  <GlobalErrorProvider>
    <KoodistoProvider>
      <ConstraintsProvider>{props.children}</ConstraintsProvider>
    </KoodistoProvider>
  </GlobalErrorProvider>
)
