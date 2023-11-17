import React, { useCallback, useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodiviiteId } from '../../util/koodisto'
import { common, CommonProps } from '../CommonProps'
import {
  groupKoodistoToOptions,
  Select,
  SelectOption
} from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { TestIdText } from '../../appstate/useTestId'

export type Suorituskielikoodiviite = Koodistokoodiviite<'kieli'>

export type SuorituskieliViewProps = CommonProps<
  FieldViewerProps<Suorituskielikoodiviite, {}>
>

export const SuorituskieliView: React.FC<SuorituskieliViewProps> = (props) => (
  <div {...common(props, ['SuorituskieliView'])}>
    <TestIdText id="suorituskieli.value">{t(props.value?.nimi)}</TestIdText>
  </div>
)

export type SuorituskieliEditProps = CommonProps<
  FieldEditorProps<Suorituskielikoodiviite, {}>
>

export const SuorituskieliEdit: React.FC<SuorituskieliEditProps> = (props) => {
  const kunnat = useKoodisto('kieli')

  const options = useMemo(
    () => kunnat && groupKoodistoToOptions(kunnat),
    [kunnat]
  )

  const selected = useMemo(
    () => props.value && koodiviiteId(props.value),
    [props.value]
  )

  const { onChange } = props
  const onChangeCB = useCallback(
    (option?: SelectOption<Suorituskielikoodiviite>) => {
      onChange(option?.value)
    },
    [onChange]
  )

  return options ? (
    <Select
      {...common(props, ['SuorituskieliEdit'])}
      options={options}
      value={selected}
      onChange={onChangeCB}
      testId="suorituskieli.edit"
    />
  ) : (
    <SuorituskieliView value={props.value} />
  )
}
