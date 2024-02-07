import React from 'react'
import { usePeruste } from '../../appstate/peruste'
import { t } from '../../i18n/i18n'
import { CommonProps } from '../CommonProps'
import { OptionList, Select } from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { TestIdText } from '../../appstate/useTestId'
import {
  mapError,
  mapInitial,
  mapLoading,
  mapSuccess,
  useApiWithParams
} from '../../api-fetch'
import { fetchPerustelinkki, Perustelinkki } from '../../util/koskiApi'
import { Trans } from '../texts/Trans'
import { useTree } from '../../appstate/tree'

type PerusteViewProps = CommonProps<FieldViewerProps<string | undefined, {}>>

export const PerusteView: React.FC<PerusteViewProps> = (props) => {
  const { TreeNode, ...tree } = useTree()

  return props.value ? (
    <PerusteViewLink diaarinumero={props.value} />
  ) : (
    <TestIdText id="peruste.value">{'-'}</TestIdText>
  )
}

export const PerusteViewLink: React.FC<{ diaarinumero: string }> = ({
  diaarinumero
}) => {
  const { TreeNode, ...tree } = useTree()

  const perustelinkkiResponse = useApiWithParams(fetchPerustelinkki, [
    diaarinumero
  ])

  return (
    <TreeNode>
      {mapInitial(perustelinkkiResponse, () => (
        <span>{diaarinumero}</span>
      ))}
      {mapLoading(perustelinkkiResponse, () => (
        <Trans>{'Haetaan'}</Trans>
      ))}
      {mapError(perustelinkkiResponse, () => (
        <span>{diaarinumero}</span>
      ))}
      {mapSuccess(perustelinkkiResponse, (responseData: Perustelinkki) => (
        <>
          <a href={responseData.url}>
            <TestIdText id="peruste.value">{diaarinumero}</TestIdText>
          </a>
        </>
      ))}
    </TreeNode>
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
