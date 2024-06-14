import React, { useEffect } from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { SuoritusFieldsProps } from './SuoritusFields'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'perusopetuksenlisaopetus',
  koodistoUri: 'suorituksentyyppi'
})

export const PerusopetuksenLisäopetusFields = (props: SuoritusFieldsProps) => {
  const perusteOptions = usePerusteSelectOptions(päätasonSuoritus.koodiarvo)

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return (
    <>
      {props.state.peruste.visible && (
        <>
          {t('Peruste')}
          <Select
            options={perusteOptions}
            initialValue="105/011/2014"
            value={props.state.peruste.value?.koodiarvo}
            onChange={(opt) => props.state.peruste.set(opt?.value)}
            disabled={perusteOptions.length < 2}
            testId="peruste"
          />
        </>
      )}
    </>
  )
}
