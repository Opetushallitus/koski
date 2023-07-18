import React, { useCallback, useMemo, useState } from 'react'
import {
  useHasOwnOrganisaatiot,
  useOrganisaatioHierarkia
} from '../../appstate/organisaatioHierarkia'
import { localize, t } from '../../i18n/i18n'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { isKoulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { getOrganisaatioId, toOrganisaatio } from '../../util/organisaatiot'
import { common, CommonProps, testId } from '../CommonProps'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

export type OrganisaatioViewProps<T extends Organisaatio> = CommonProps<
  FieldViewerProps<T, {}>
>

export const OrganisaatioView = <T extends Organisaatio>(
  props: OrganisaatioViewProps<T>
): React.ReactElement => (
  <div {...common(props, ['OrganisaatioView'])} {...testId(props)}>
    {t(props.value?.nimi) || '–'}
  </div>
)

export type OrganisaatioEditProps<T extends Organisaatio> = CommonProps<
  FieldEditorProps<
    T,
    {
      include?: Organisaatio[]
    }
  >
>

export const OrganisaatioEdit = <T extends Organisaatio>(
  props: OrganisaatioEditProps<T>
): React.ReactElement => {
  const [query, setQuery] = useState('')
  const queriedOrganisaatiot = useOrganisaatioHierarkia(query)
  const organisaatiot = useMemo(
    () => [
      ...queriedOrganisaatiot,
      ...(props.include?.map(organisaatioToOrganisaatioHierarkia) || [])
    ],
    [props.include, queriedOrganisaatiot]
  )
  const hasOwnOrganisaatiot = useHasOwnOrganisaatiot()

  const options: OptionList<T> = useMemo(
    () => organisaatioHierarkiaToOptions(organisaatiot, hasOwnOrganisaatiot),
    [hasOwnOrganisaatiot, organisaatiot]
  )

  const selected = useMemo(
    () => props.value && getOrganisaatioId(props.value),
    [props.value]
  )

  const { onChange } = props
  const onChangeCB = useCallback(
    (option?: SelectOption<T>) => {
      onChange(option?.value)
    },
    [onChange]
  )

  return (
    <Select
      options={options}
      value={selected}
      onChange={onChangeCB}
      onSearch={setQuery}
      testId={props.testId}
    />
  )
}

const organisaatioHierarkiaToOptions = <T extends Organisaatio>(
  orgs: OrganisaatioHierarkia[],
  hasOwnOrganisaatiot: boolean
): OptionList<T> =>
  orgs.map((organisaatiohierarkia) => {
    const org = toOrganisaatio(organisaatiohierarkia)
    return {
      key: getOrganisaatioId(org),
      label: t(org.nimi),
      value: org as T,
      children:
        organisaatiohierarkia.children &&
        organisaatioHierarkiaToOptions<T>(
          organisaatiohierarkia.children,
          hasOwnOrganisaatiot
        ),
      ignoreFilter: !hasOwnOrganisaatiot,
      isGroup: isKoulutustoimija(org)
    }
  })

const organisaatioToOrganisaatioHierarkia = <T extends Organisaatio>(
  org: T
): OrganisaatioHierarkia =>
  OrganisaatioHierarkia({
    oid: getOrganisaatioId(org),
    aktiivinen: true,
    nimi: org.nimi || localize('–')
  })
