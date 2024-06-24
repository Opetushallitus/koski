import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

export const AikuistenPerusopetusFields = (props: SuoritusFieldsProps) => (
  <>
    {t('Oppimäärä')}
    <DialogPäätasonSuoritusSelect
      state={props.state}
      default="aikuistenperusopetuksenoppimaara"
      testId="oppimäärä"
    />

    <DialogPerusteSelect state={props.state} default="OPH-1280-2017" />
  </>
)
