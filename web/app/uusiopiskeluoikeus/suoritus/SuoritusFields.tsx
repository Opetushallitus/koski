import React from 'react'
import { UusiOpiskeluoikeusDialogState } from '../UusiOpiskeluoikeusForm'
import { PerusopetusFields } from './PerusopetusFields'
import { PerusopetukseenValmistavaFields } from './PerusopetukseenValmistavaFields'
import { PerusopetuksenLisäopetusFields } from './PerusopetuksenLisaopetusFields'

export type SuoritusFieldsProps = {
  state: UusiOpiskeluoikeusDialogState
}

export const SuoritusFields = (props: SuoritusFieldsProps) => {
  switch (props.state.opiskeluoikeus.value?.koodiarvo) {
    case 'perusopetus':
      return <PerusopetusFields {...props} />
    case 'perusopetukseenvalmistavaopetus':
      return <PerusopetukseenValmistavaFields {...props} />
    case 'perusopetuksenlisaopetus':
      return <PerusopetuksenLisäopetusFields {...props} />
    default:
      return null
  }
}
