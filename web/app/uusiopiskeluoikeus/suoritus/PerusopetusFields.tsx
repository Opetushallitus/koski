import React from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { usePäätasonSuoritustyypit } from '../UusiOpiskeluoikeusForm'
import { SuoritusFieldsProps } from './SuoritusFields'

const piilotettavatPtsTyypit = ['perusopetuksenvuosiluokka']

export const PerusopetusFields = (props: SuoritusFieldsProps) => {
  const options = usePäätasonSuoritustyypit(props.state).filter(
    (opt) => !piilotettavatPtsTyypit.includes(opt.value!.koodiarvo)
  )
  const perusteOptions = usePerusteSelectOptions(
    props.state.päätasonSuoritus.value?.koodiarvo
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
