import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from './SuoritusFields'

const piilotettavatPtsTyypit = ['perusopetuksenvuosiluokka']

export const PerusopetusFields = (props: SuoritusFieldsProps) => (
  <>
    {t('Oppimäärä')}
    <DialogPäätasonSuoritusSelect
      state={props.state}
      default="perusopetuksenoppimaara"
      hiddenOptions={piilotettavatPtsTyypit}
      testId="oppimäärä"
    />

    <DialogPerusteSelect state={props.state} default="105/011/2014" />
  </>
)
