import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { PerusopetuksenOppiaineSelect } from './PerusopetusFields'
import { SuoritusFieldsProps } from './SuoritusFields'

export const AikuistenPerusopetusFields = (props: SuoritusFieldsProps) => (
  <>
    <label>
      {t('Oppimäärä')}
      <DialogPäätasonSuoritusSelect
        state={props.state}
        default="aikuistenperusopetuksenoppimaara"
        testId="oppimäärä"
      />
    </label>

    {props.state.oppiaine.visible && (
      <PerusopetuksenOppiaineSelect
        state={props.state}
        koulutusmoduuliClassName="AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine"
      />
    )}

    <DialogPerusteSelect state={props.state} default="OPH-1280-2017" />
  </>
)
