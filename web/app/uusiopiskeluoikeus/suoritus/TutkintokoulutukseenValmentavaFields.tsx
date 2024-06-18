import React, { useEffect } from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogKoodistoSelect } from '../DialogKoodistoSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'tuvakoulutuksensuoritus',
  koodistoUri: 'suorituksentyyppi'
})

export const TutkintokoulutukseenValmentavaFields = (
  props: SuoritusFieldsProps
) => {
  const perusteOptions = usePerusteSelectOptions(päätasonSuoritus.koodiarvo)

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

      {props.state.peruste.visible && (
        <>
          {t('Peruste')}
          <Select
            options={perusteOptions}
            initialValue="OPH-1488-2021"
            value={props.state.peruste.value?.koodiarvo}
            onChange={(opt) => props.state.peruste.set(opt?.value)}
            // disabled={perusteOptions.length < 2}
            testId="peruste"
          />
        </>
      )}
    </>
  )
}
