import React, { useEffect } from 'react'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { SuoritusFieldsProps } from '.'

const saksa = Koodistokoodiviite({
  koodistoUri: 'kieli',
  koodiarvo: 'DE'
})

export const DiaTutkintoFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.suorituskieli.set(saksa), [])

  return (
    <label>
      {t('Oppimäärä')}
      <DialogPäätasonSuoritusSelect
        state={props.state}
        default="diatutkintovaihe"
        testId="oppimäärä"
      />
    </label>
  )
}
