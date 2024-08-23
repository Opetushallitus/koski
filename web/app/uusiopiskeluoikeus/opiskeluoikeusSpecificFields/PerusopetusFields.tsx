import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { YleissivistäväOppiaineSelect } from '../components/YleissivistäväOppiaineSelect'
import { SuoritusFieldsProps } from '.'

const piilotettavatPtsTyypit = ['perusopetuksenvuosiluokka']

export const PerusopetusFields = (props: SuoritusFieldsProps) => {
  return (
    <>
      <label>
        {t('Oppimäärä')}
        <DialogPäätasonSuoritusSelect
          state={props.state}
          default="perusopetuksenoppimaara"
          hiddenOptions={piilotettavatPtsTyypit}
          testId="oppimäärä"
        />
      </label>

      {props.state.oppiaine.visible && (
        <YleissivistäväOppiaineSelect
          state={props.state}
          koulutusmoduuliClassName="NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine"
        />
      )}

      <DialogPerusteSelect state={props.state} default="104/011/2014" />
    </>
  )
}
