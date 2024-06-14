import React from 'react'
import { UusiOpiskeluoikeusDialogState } from '../UusiOpiskeluoikeusForm'
import { PerusopetusFields } from './PerusopetusFields'
import { PerusopetukseenValmistavaFields } from './PerusopetukseenValmistavaFields'

export type OppimääräFieldsProps = {
  state: UusiOpiskeluoikeusDialogState
}

export const OppimääräFields = (props: OppimääräFieldsProps) => {
  switch (props.state.opiskeluoikeus.value?.koodiarvo) {
    case 'perusopetus':
      return <PerusopetusFields {...props} />
    case 'perusopetukseenvalmistavaopetus':
      return <PerusopetukseenValmistavaFields {...props} />
    default:
      return null
  }
}
