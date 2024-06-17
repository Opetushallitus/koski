import React, { useEffect, useMemo } from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import {
  Select,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { SuoritusFieldsProps } from './SuoritusFields'
import { useKoodisto } from '../../appstate/koodisto'
import { koodistokoodiviiteId } from '../../util/koodisto'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'tuvakoulutuksensuoritus',
  koodistoUri: 'suorituksentyyppi'
})

export const TutkintokoulutukseenValmentavaFields = (
  props: SuoritusFieldsProps
) => {
  const perusteOptions = usePerusteSelectOptions(päätasonSuoritus.koodiarvo)

  const järjestämislupa = useKoodisto('tuvajarjestamislupa')
  const järjestämislupaOptions = useMemo(
    () => (järjestämislupa ? groupKoodistoToOptions(järjestämislupa) : []),
    [järjestämislupa]
  )

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return (
    <>
      {props.state.tuvaJärjestämislupa.visible && (
        <>
          {t('Järjestämislupa')}
          <Select
            options={järjestämislupaOptions}
            value={
              props.state.tuvaJärjestämislupa.value &&
              koodistokoodiviiteId(props.state.tuvaJärjestämislupa.value)
            }
            onChange={(opt) => {
              console.log('onChange', opt)
              return props.state.tuvaJärjestämislupa.set(opt?.value)
            }}
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
