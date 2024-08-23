import React, { useEffect } from 'react'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from '.'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'esiopetuksensuoritus',
  koodistoUri: 'suorituksentyyppi'
})

export const EsiopetusFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])
  return <DialogPerusteSelect state={props.state} default="102/011/2014" />
}
