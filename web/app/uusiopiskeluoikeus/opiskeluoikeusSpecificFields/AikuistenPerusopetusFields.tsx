import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { SuoritusFieldsProps } from '.'
import { YleissivistäväOppiaineSelect } from '../components/YleissivistäväOppiaineSelect'

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
      <YleissivistäväOppiaineSelect
        state={props.state}
        koulutusmoduuliClassName="AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine"
      />
    )}

    <DialogPerusteSelect state={props.state} default="OPH-1280-2017" />
  </>
)
