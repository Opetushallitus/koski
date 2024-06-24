import React from 'react'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { PerusopetusFields } from './PerusopetusFields'
import { PerusopetukseenValmistavaFields } from './PerusopetukseenValmistavaFields'
import { PerusopetuksenLisäopetusFields } from './PerusopetuksenLisaopetusFields'
import { AikuistenPerusopetusFields } from './AikuistenPerusopetusFields'
import { EsiopetusFields } from './EsiopetusFields'
import { TutkintokoulutukseenValmentavaFields } from './TutkintokoulutukseenValmentavaFields'
import { MuuKuinSäänneltyKoulutusFields } from './MuuKuinSaanneltyKoulutusFields'
import { TaiteenPerusopetusFields } from './TaiteenPerusopetusFields'
import { VapaaSivistystyöFields } from './VapaaSivistystyoFields'
import { LukioonValmistavaFields } from './LukioonValmistavaFields'
import { LukioKoulutusFields } from './LukiokoulutusFields'
import { AmmatillinenKoulutusFields } from './AmmatillinenKoulutusFields'
import { EuropeanSchoolOfHelsinkiFields } from './EuropeanSchoolOfHelsinkiFields'
import { DiaTutkintoFields } from './DiaTutkintoFields'
import { IBTutkintoFields } from './IBTutkintoFields'
import { EBTutkintoFields } from './EBTutkintoFields'

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
    case 'luva':
      return <LukioonValmistavaFields {...props} />
    case 'lukiokoulutus':
      return <LukioKoulutusFields {...props} />
    case 'ammatillinenkoulutus':
      return <AmmatillinenKoulutusFields {...props} />
    case 'europeanschoolofhelsinki':
      return <EuropeanSchoolOfHelsinkiFields {...props} />
    case 'ebtutkinto':
      return <EBTutkintoFields {...props} />
    case 'diatutkinto':
      return <DiaTutkintoFields {...props} />
    case 'ibtutkinto':
      return <IBTutkintoFields {...props} />
    default:
      return null
  }
}
