import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

export const EBTutkintoFields = (props: SuoritusFieldsProps) => (
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
