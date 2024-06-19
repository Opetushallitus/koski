import React from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { usePäätasonSuoritustyypit } from '../UusiOpiskeluoikeusForm'
import { SuoritusFieldsProps } from './SuoritusFields'
import { DialogPerusteSelect } from '../DialogPerusteSelect'

export const AikuistenPerusopetusFields = (props: SuoritusFieldsProps) => {
  const options = usePäätasonSuoritustyypit(props.state)

  return (
    <>
      {t('Oppimäärä')}
      <Select
        options={options}
        initialValue="suorituksentyyppi_aikuistenperusopetuksenoppimaara"
        value={
          props.state.päätasonSuoritus.value &&
          koodistokoodiviiteId(props.state.päätasonSuoritus.value)
        }
        onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
        testId="oppimäärä"
      />

      <DialogPerusteSelect state={props.state} default="OPH-1280-2017" />
    </>
  )
}
