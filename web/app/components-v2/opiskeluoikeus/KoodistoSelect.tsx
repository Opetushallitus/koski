import React, { useCallback, useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { CommonProps } from '../CommonProps'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { TestIdLayer } from '../../appstate/useTestId'

export type KoodistoSelectProps<T extends string> = CommonProps<{
  koodistoUri: T
  addNewText: string | LocalizedString
  onSelect: (
    tunniste: Koodistokoodiviite<T> | undefined,
    isNew: boolean
  ) => void
  onRemove?: (tunniste: Koodistokoodiviite<T>) => void
  filter?: (tunniste: Koodistokoodiviite<T>) => boolean
  testId: string | number
  value?: string
  zeroValueOption?: boolean
}>

export function KoodistoSelect<T extends string>(
  props: KoodistoSelectProps<T>
) {
  const koodisto = useKoodisto(props.koodistoUri)
  const { filter, zeroValueOption } = props
  const options: OptionList<Koodistokoodiviite<T>> = useMemo(() => {
    const koodistoOptions = (koodisto || [])
      .map((tunniste) => ({
        key: tunniste.koodiviite.koodiarvo,
        label: t(tunniste.koodiviite.nimi),
        value: tunniste.koodiviite,
        removable: true
      }))
      .filter((entry) => {
        if (filter === undefined) {
          return entry
        }
        return filter(entry.value)
      })

    const zeroValue = {
      key: 'Ei valintaa',
      label: t('Ei valintaa'),
      value: undefined
    }

    return zeroValueOption ? [zeroValue, ...koodistoOptions] : koodistoOptions
  }, [koodisto, filter, zeroValueOption])

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
