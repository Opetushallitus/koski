import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

export const IBTutkintoFields = (props: SuoritusFieldsProps) => {
  return (
    <>
      {t('Oppimäärä')}
      <DialogPäätasonSuoritusSelect
        state={props.state}
        default="ibtutkinto"
        testId="oppimäärä"
      />
    </>
  )
}
