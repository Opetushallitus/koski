import React, { useEffect, useMemo } from 'react'
import { usePeruste } from '../../appstate/peruste'
import { Select, perusteToOption } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OppimääräFieldsProps } from './OppimaaraFields'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'perusopetuksenlisaopetus',
  koodistoUri: 'suorituksentyyppi'
})

export const PerusopetuksenLisäopetusFields = (props: OppimääräFieldsProps) => {
  const perusteet = usePeruste(päätasonSuoritus.koodiarvo)
  const perusteOptions = useMemo(
    () => perusteet?.map(perusteToOption) || [],
    [perusteet]
  )

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
