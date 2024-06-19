import React from 'react'
import { UusiOpiskeluoikeusDialogState } from '../state'
import { PerusopetusFields } from './PerusopetusFields'
import { PerusopetukseenValmistavaFields } from './PerusopetukseenValmistavaFields'
import { PerusopetuksenLisäopetusFields } from './PerusopetuksenLisaopetusFields'
import { AikuistenPerusopetusFields } from './AikuistenPerusopetusFields'
import { EsiopetusFields } from './EsiopetusFields'
import { TutkintokoulutukseenValmentavaFields } from './TutkintokoulutukseenValmentavaFields'
import { MuuKuinSäänneltyKoulutusFields } from './MuuKuinSaanneltyKoulutusFields'
import { TaiteenPerusopetusFields } from './TaiteenPerusopetusFields'
import { VapaaSivistystyöFields } from './VapaaSivistystyoFields'

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
    case 'muukuinsaanneltykoulutus':
      return <MuuKuinSäänneltyKoulutusFields {...props} />
    case 'taiteenperusopetus':
      return <TaiteenPerusopetusFields {...props} />
    case 'vapaansivistystyonkoulutus':
      return <VapaaSivistystyöFields {...props} />
    default:
      return null
  }
}
