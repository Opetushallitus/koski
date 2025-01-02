import React, { useCallback, useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { CommonProps } from '../CommonProps'
import {
  mapOptionLabels,
  OptionList,
  Select,
  SelectOption
} from '../controls/Select'
import { TestIdLayer } from '../../appstate/useTestId'
import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'

export type KoodistoSelectProps<T extends string> = CommonProps<{
  koodistoUri: T
  koodiarvot?: string[]
  addNewText?: string | LocalizedString
  onSelect: (
    tunniste: Koodistokoodiviite<T> | undefined,
    isNew: boolean
  ) => void
  onRemove?: (tunniste: Koodistokoodiviite<T>) => void
  filter?: (tunniste: Koodistokoodiviite<T>) => boolean
  testId: string | number
  value?: string
  zeroValueOption?: boolean
  inlineOptions?: boolean
  format?: (k: Koodistokoodiviite) => string
}>

export function KoodistoSelect<T extends string>(
  props: KoodistoSelectProps<T>
) {
  const koodisto = useKoodisto(props.koodistoUri, props.koodiarvot)
  const { filter, zeroValueOption } = props
  const options: OptionList<Koodistokoodiviite<T>> = useMemo(() => {
    const koodistoOptions = pipe(
      koodisto || [],
      A.map((tunniste) => ({
        key: tunniste.koodiviite.koodiarvo,
        label: t(tunniste.koodiviite.nimi),
        value: tunniste.koodiviite,
        removable: true
      })),
      A.filter((entry) => (filter ? filter(entry.value) : true)),
      mapOptionLabels((option) =>
        option.value && props.format ? props.format(option.value) : option.label
      )
    )

    const zeroValue = {
      key: 'Ei valintaa',
      label: t('Ei valintaa'),
      value: undefined
    }

    return zeroValueOption ? [zeroValue, ...koodistoOptions] : koodistoOptions
  }, [koodisto, zeroValueOption, filter, props])

  const { onSelect } = props
  const onChangeCB = useCallback(
    (option?: SelectOption<Koodistokoodiviite<T>>) => {
      onSelect(option?.value, false)
    },
    [onSelect]
  )

  return (
    <TestIdLayer id="addOsasuoritus">
      <Select
        inlineOptions={props.inlineOptions}
        placeholder={props.addNewText}
        options={options}
        hideEmpty
        onChange={onChangeCB}
        testId="select"
        value={props.value}
      />
    </TestIdLayer>
  )
}
