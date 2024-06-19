import React, { useEffect } from 'react'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'tuvakoulutuksensuoritus',
  koodistoUri: 'suorituksentyyppi'
})

export const TutkintokoulutukseenValmentavaFields = (
  props: SuoritusFieldsProps
) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return (
    <>
      {props.state.tuvaJärjestämislupa.visible && (
        <>
          {t('Järjestämislupa')}
          <DialogKoodistoSelect
            state={props.state.tuvaJärjestämislupa}
            koodistoUri="tuvajarjestamislupa"
            testId="järjestämislupa"
          />
        </>
      )}

      <DialogPerusteSelect state={props.state} default="OPH-1488-2021" />
    </>
  )
}
