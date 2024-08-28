import React from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { SuoritusFieldsProps } from '.'
import { SelectOption } from '../../components-v2/controls/Select'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'

export const IBTutkintoFields = (props: SuoritusFieldsProps) => {
  return (
    <label>
      {t('Oppimäärä')}
      <DialogPäätasonSuoritusSelect
        state={props.state}
        extraOptions={extraOptions}
        default="ibtutkinto"
        testId="oppimäärä"
      />
    </label>
  )
}

const extraOptions: Array<
  SelectOption<Koodistokoodiviite<'suorituksentyyppi'>>
> = [
  {
    key: 'suorituksentyyppi_preiboppimaara2019',
    label: 'Pre-IB 2019',
    value: Koodistokoodiviite({
      koodiarvo: 'preiboppimaara2019',
      koodistoUri: 'suorituksentyyppi',
      nimi: Finnish({ fi: 'Pre-IB 2019' })
    })
  }
]
