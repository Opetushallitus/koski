import React from 'react'
import { usePeruste } from '../../appstate/peruste'
import { t } from '../../i18n/i18n'
import { CommonProps } from '../CommonProps'
import { OptionList, Select } from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { TestIdText } from '../../appstate/useTestId'

type PerusteViewProps = CommonProps<FieldViewerProps<string | undefined, {}>>

// TODO: Perusteen linkitys
export const PerusteView: React.FC<PerusteViewProps> = (props) => {
  return (
    <div>
      <TestIdText id="peruste.value">{props.value || '-'}</TestIdText>
    </div>
  )
}

type PerusteEditProps = CommonProps<
  FieldEditorProps<
    string | undefined,
    {
      diaariNumero: string
    }
  >
>

export const PerusteEdit: React.FC<PerusteEditProps> = (props) => {
  const perusteet = usePeruste(props.diaariNumero)
  const mappedPerusteet: OptionList<string> = (perusteet || []).map((p) => ({
    key: p.koodiarvo,
    label: `${p.koodiarvo} ${t(p.nimi)}`,
    value: p.koodiarvo
  }))
  return (
    <Select
      testId="peruste.edit"
      onChange={(opt) => {
        props.onChange(opt?.value || '')
      }}
      initialValue={props.value}
      value={props.value || ''}
      options={mappedPerusteet}
    />
  )
}
