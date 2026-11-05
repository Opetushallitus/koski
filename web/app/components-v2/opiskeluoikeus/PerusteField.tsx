import React from 'react'
import {
  createPreferLocalCache,
  mapError,
  mapInitial,
  mapLoading,
  mapSuccess,
  useApiWithParams
} from '../../api-fetch'
import { usePeruste } from '../../appstate/peruste'
import { useTree } from '../../appstate/tree'
import { TestIdText } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { fetchPerustelinkki, Perustelinkki } from '../../util/koskiApi'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import { OptionList, Select } from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { Trans } from '../texts/Trans'

type PerusteViewProps = CommonProps<
  FieldViewerProps<string | undefined, { päättymispäivä?: string }>
>

export const PerusteView: React.FC<PerusteViewProps> = (props) => {
  const { TreeNode, ...tree } = useTree()

  return props.value ? (
    <PerusteViewLink
      diaarinumero={props.value}
      päättymispäivä={props.päättymispäivä}
    />
  ) : (
    <TestIdText id="peruste.value">{'-'}</TestIdText>
  )
}

const cache = createPreferLocalCache(fetchPerustelinkki)

// Päättyneeltä opiskeluoikeudelta linkitetään päättymispäivänä voimassa olleeseen
// perusteeseen.
export const PerusteViewLink: React.FC<{
  diaarinumero: string
  päättymispäivä?: string
}> = ({ diaarinumero, päättymispäivä }) => {
  const { TreeNode, ...tree } = useTree()

  const perustelinkkiResponse = useApiWithParams(
    fetchPerustelinkki,
    [diaarinumero, päättymispäivä],
    cache
  )

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
          <a href={responseData.url} target="_blank" rel="noopener noreferrer">
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
      suorituksenTyyppi: string
    }
  >
>

export const PerusteEdit: React.FC<PerusteEditProps> = (props) => {
  const perusteet = usePeruste(props.suorituksenTyyppi)
  const mappedPerusteet: OptionList<string> = (perusteet || []).map((p) => ({
    key: p.koodiarvo,
    label: `${p.koodiarvo} ${t(p.nimi)}`,
    value: p.koodiarvo
  }))
  return (
    <Select
      hasErrors={Boolean(props.errors)}
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
