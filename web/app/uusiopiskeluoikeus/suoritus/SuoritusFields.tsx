import React from 'react'
import { UusiOpiskeluoikeusDialogState } from '../UusiOpiskeluoikeusForm'
import { PerusopetusFields } from './PerusopetusFields'
import { PerusopetukseenValmistavaFields } from './PerusopetukseenValmistavaFields'
import { PerusopetuksenLisäopetusFields } from './PerusopetuksenLisaopetusFields'
import { AikuistenPerusopetusFields } from './AikuistenPerusopetusFields'
import { EsiopetusFields } from './EsiopetusFields'
import { TutkintokoulutukseenValmentavaFields } from './TutkintokoulutukseenValmentavaFields'

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
    case 'aikuistenperusopetus':
      return <AikuistenPerusopetusFields {...props} />
    case 'esiopetus':
      return <EsiopetusFields {...props} />
    case 'tuva':
      return <TutkintokoulutukseenValmentavaFields {...props} />
    default:
      return null
  }
}
