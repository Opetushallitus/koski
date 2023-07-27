import React, { useCallback, useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { CommonProps, subTestId } from '../CommonProps'
import { OptionList, Select, SelectOption } from '../controls/Select'

export type KoodistoSelectProps = CommonProps<{
  koodistoUri: string
  addNewText: string | LocalizedString
  onSelect: (tunniste: Koodistokoodiviite, isNew: boolean) => void
  onRemove?: (tunniste: Koodistokoodiviite) => void
}>

export const KoodistoSelect: React.FC<KoodistoSelectProps> = (props) => {
  const k = useKoodisto(props.koodistoUri)
  const options: OptionList<Koodistokoodiviite> = useMemo(
    () => [
      ...(k || []).map((tunniste) => ({
        key: tunniste.koodiviite.koodiarvo,
        label: t(tunniste.koodiviite.nimi),
        value: tunniste.koodiviite,
        removable: true
      }))
    ],
    [k]
  )

  const { onSelect } = props
  const onChangeCB = useCallback(
    (option?: SelectOption<Koodistokoodiviite>) => {
      if (option?.value) {
        onSelect(option.value, false)
      }
    },
    [onSelect]
  )

  return (
    <>
      <Select
        placeholder={props.addNewText}
        options={options}
        hideEmpty
        onChange={onChangeCB}
        testId={subTestId(props, 'select')}
      />
    </>
  )
}
