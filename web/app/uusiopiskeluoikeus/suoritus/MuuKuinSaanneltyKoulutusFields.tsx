import React, { useEffect, useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import {
  Select,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { SuoritusFieldsProps } from './SuoritusFields'
import { koodistokoodiviiteId } from '../../util/koodisto'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'muukuinsaanneltykoulutus',
  koodistoUri: 'suorituksentyyppi'
})

export const MuuKuinSäänneltyKoulutusFields = (props: SuoritusFieldsProps) => {
  const opintokokonaisuudet = useKoodisto('opintokokonaisuudet')
  const opintokokonaisuusOptions = useMemo(
    () =>
      opintokokonaisuudet ? groupKoodistoToOptions(opintokokonaisuudet) : [],
    [opintokokonaisuudet]
  )

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return (
    <>
      {props.state.peruste.visible && (
        <>
          {t('Opintokokonaisuus')}
          <Select
            options={opintokokonaisuusOptions}
            value={
              props.state.opintokokonaisuus.value &&
              koodistokoodiviiteId(props.state.opintokokonaisuus.value)
            }
            onChange={(opt) => props.state.opintokokonaisuus.set(opt?.value)}
            testId="opintokokonaisuus"
          />
        </>
      )}
    </>
  )
}
