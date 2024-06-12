import React from 'react'
import { UusiOpiskeluoikeusDialogState } from '../UusiOpiskeluoikeusDialog'
import { PerusopetusFields } from './PerusopetusFields'

export type OppimääräFieldsProps = {
  state: UusiOpiskeluoikeusDialogState
}

export const OppimääräFields = (props: OppimääräFieldsProps) => {
  switch (props.state.opiskeluoikeus.value?.koodiarvo) {
    case 'perusopetus':
      return <PerusopetusFields {...props} />
    default:
      return null
  }
}
