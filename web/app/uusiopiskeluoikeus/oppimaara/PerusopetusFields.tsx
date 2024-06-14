import React, { useMemo } from 'react'
import { Select, perusteToOption } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { usePäätasonSuoritustyypit } from '../UusiOpiskeluoikeusForm'
import { OppimääräFieldsProps } from './OppimaaraFields'
import { usePeruste } from '../../appstate/peruste'

const piilotettavatPtsTyypit = ['perusopetuksenvuosiluokka']

export const PerusopetusFields = (props: OppimääräFieldsProps) => {
  const options = usePäätasonSuoritustyypit(props.state).filter(
    (opt) => !piilotettavatPtsTyypit.includes(opt.value!.koodiarvo)
  )
  const perusteet = usePeruste(props.state.päätasonSuoritus.value?.koodiarvo)
  const perusteOptions = useMemo(
    () => perusteet?.map(perusteToOption) || [],
    [perusteet]
  )

  return (
    <>
      {t('Oppimäärä')}
      <Select
        options={options}
        initialValue="suorituksentyyppi_perusopetuksenoppimaara"
        value={
          props.state.päätasonSuoritus.value &&
          koodistokoodiviiteId(props.state.päätasonSuoritus.value)
        }
        onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
        testId="oppimäärä"
      />

      {props.state.peruste.visible && (
        <>
          {t('Peruste')}
          <Select
            options={perusteOptions}
            initialValue="104/011/2014"
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
