import React from 'react'
import {
  RadioButtonOption,
  RadioButtons
} from '../components-v2/controls/RadioButtons'
import { UusiOpiskeluoikeusDialogState } from './state'
import { t } from '../i18n/i18n'

export type DialogMaksuttomuusSelectProps = {
  state: UusiOpiskeluoikeusDialogState
}

export const DialogMaksuttomuusSelect = (
  props: DialogMaksuttomuusSelectProps
) => {
  return (
    <RadioButtons
      options={maksuttomuusOptions}
      value={maksuttomuusKey(props.state.maksuton.value)}
      onChange={props.state.maksuton.set}
      testId="maksuton"
    />
  )
}

const maksuttomuusKey = (value?: boolean | null): string | undefined =>
  maksuttomuusOptions.find((o) => o.value === value)?.key

const maksuttomuusOptions: Array<RadioButtonOption<boolean | null>> = [
  {
    key: 'eiOvlPiirissä',
    value: null,
    label: t(
      'Henkilö on syntynyt ennen vuotta 2004 ja ei ole laajennetun oppivelvollisuuden piirissä'
    )
  },
  {
    key: 'maksuton',
    value: true,
    label: t('Koulutus on maksutonta')
  },
  {
    key: 'maksullinen',
    value: false,
    label: t('Koulutus on maksullista')
  }
]
