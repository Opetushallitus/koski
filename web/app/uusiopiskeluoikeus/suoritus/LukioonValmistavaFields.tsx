import React, { useEffect } from 'react'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from './SuoritusFields'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'luva',
  koodistoUri: 'suorituksentyyppi'
})

export const LukioonValmistavaFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])
  return <DialogPerusteSelect state={props.state} default="OPH-4958-2020" />
}
