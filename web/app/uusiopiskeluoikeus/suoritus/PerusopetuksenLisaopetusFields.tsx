import React, { useEffect } from 'react'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogPerusteSelect } from '../DialogPerusteSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'perusopetuksenlisaopetus',
  koodistoUri: 'suorituksentyyppi'
})

export const PerusopetuksenLisäopetusFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])
  return <DialogPerusteSelect state={props.state} default="105/011/2014" />
}
