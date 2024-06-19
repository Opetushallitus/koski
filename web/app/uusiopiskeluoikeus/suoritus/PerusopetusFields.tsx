import React from 'react'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { usePäätasonSuoritustyypit } from '../state/hooks'
import { SuoritusFieldsProps } from './SuoritusFields'

const piilotettavatPtsTyypit = ['perusopetuksenvuosiluokka']

export const PerusopetusFields = (props: SuoritusFieldsProps) => {
  const options = usePäätasonSuoritustyypit(props.state).filter(
    (opt) => !piilotettavatPtsTyypit.includes(opt.value!.koodiarvo)
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

      <DialogPerusteSelect state={props.state} default="105/011/2014" />
    </>
  )
}
