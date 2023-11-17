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

export type Kuntakoodiviite = Koodistokoodiviite<'kunta'>

export type KuntaViewProps = CommonProps<FieldViewerProps<Kuntakoodiviite, {}>>

export const KuntaView: React.FC<KuntaViewProps> = (props) => (
  <TestIdText {...common(props, ['KuntaView'])} id="kunta.value">
    {t(props.value?.nimi) || '–'}
  </TestIdText>
)

export type KuntaEditProps = CommonProps<FieldEditorProps<Kuntakoodiviite, {}>>

export const KuntaEdit: React.FC<KuntaEditProps> = (props) => {
  const kunnat = useKoodisto('kunta')

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
    (option?: SelectOption<Kuntakoodiviite>) => {
      onChange(option?.value)
    },
    [onChange]
  )

  return options ? (
    <Select
      {...common(props, ['KuntaEdit'])}
      options={options}
      value={selected}
      onChange={onChangeCB}
      testId="kunta.edit"
    />
  ) : (
    <KuntaView value={props.value} />
  )
}
