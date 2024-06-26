import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

export const InternationalSchoolFields = (props: SuoritusFieldsProps) => {
  return (
    <label>
      {t('Grade')}
      <DialogKoodistoSelect
        state={props.state.internationalSchoolGrade}
        koodistoUri="internationalschoolluokkaaste"
        testId="grade"
      />
    </label>
  )
}
