import React, { useEffect } from 'react'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from '.'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'perusopetukseenvalmistavaopetus',
  koodistoUri: 'suorituksentyyppi'
})

export const PerusopetukseenValmistavaFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])
  return <DialogPerusteSelect state={props.state} default="57/011/2015" />
}
