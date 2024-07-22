import React, { useEffect } from 'react'
import { t } from '../../i18n/i18n'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { SuoritusFieldsProps } from './SuoritusFields'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'ebtutkinto',
  koodistoUri: 'suorituksentyyppi'
})

export const EBTutkintoFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return (
    <label>
      {t('Curriculum')}
      <DialogKoodistoSelect
        state={props.state.curriculum}
        koodistoUri="europeanschoolofhelsinkicurriculum"
        default="2023"
        testId="curriculum"
      />
    </label>
  )
}
