import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { SuoritusFieldsProps } from '.'

// Vuosiluokan suoritukset lisätään editorissa, ei tässä dialogissa (kuten
// manner-Suomen perusopetuksessa). Perustetta ei valita lainkaan: Ahvenanmaan
// ops ei ole ePerusteissa, joten diaarinumero kirjataan vakiona.
const piilotettavatPtsTyypit = ['ahvenanmaanperusopetuksenvuosiluokka']

export const AhvenanmaanPerusopetusFields = (props: SuoritusFieldsProps) => (
  <label>
    {t('Oppimäärä')}
    <DialogPäätasonSuoritusSelect
      state={props.state}
      default="ahvenanmaanperusopetuksenoppimaara"
      hiddenOptions={piilotettavatPtsTyypit}
      testId="oppimäärä"
    />
  </label>
)
