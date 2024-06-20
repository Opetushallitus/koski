import React, { useEffect } from 'react'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { SuoritusFieldsProps } from './SuoritusFields'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'europeanschoolofhelsinkivuosiluokkanursery',
  koodistoUri: 'suorituksentyyppi'
})

export const EuropeanSchoolOfHelsinkiFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return (
    <>
      {t('Curriculum')}
      <DialogKoodistoSelect
        state={props.state.curriculum}
        koodistoUri="europeanschoolofhelsinkicurriculum"
        default="2023"
        testId="curriculum"
      />
    </>
  )
}
