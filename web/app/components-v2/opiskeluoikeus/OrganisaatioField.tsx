import React, { useCallback, useMemo, useState } from 'react'
import {
  useHasOwnOrganisaatiot,
  useOrganisaatioHierarkia
} from '../../appstate/organisaatioHierarkia'
import { TestIdText } from '../../appstate/useTestId'
import { localize, t } from '../../i18n/i18n'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { isKoulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { isOppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { isToimipiste } from '../../types/fi/oph/koski/schema/Toimipiste'
import { nonNull } from '../../util/fp/arrays'
import { EmptyObject } from '../../util/objects'
import {
  getOrganisaationKotipaikka,
  getOrganisaatioId,
  toOrganisaatio
} from '../../util/organisaatiot'
import { common, CommonProps } from '../CommonProps'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

export type OrganisaatioViewProps<T extends Organisaatio> = CommonProps<
  FieldViewerProps<T, EmptyObject>
>

export const OrganisaatioView = <T extends Organisaatio>(
  props: OrganisaatioViewProps<T>
): React.ReactElement => (
  <TestIdText {...common(props, ['OrganisaatioView'])} id="organisaatio.value">
    {t(props.value?.nimi) || '–'}
  </TestIdText>
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

  const includedOrganisaatiot = useMemo(
    () => [props.value, ...(props.include || [])].filter(nonNull),
    [props.include, props.value]
  )

  const organisaatiot = useMemo(
    () => addMissingOrganisaatiot(queriedOrganisaatiot, includedOrganisaatiot),
    [includedOrganisaatiot, queriedOrganisaatiot]
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
      testId="organisaatio.edit"
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

const addMissingOrganisaatiot = <T extends Organisaatio>(
  orgs: OrganisaatioHierarkia[],
  included: T[]
): OrganisaatioHierarkia[] => {
  const missing = included.filter(
    (org) => !hasOrganisaatio(orgs, getOrganisaatioId(org))
  )

  return missing.length === 0
    ? orgs
    : [...orgs, ...missing.map(organisaatioToOrganisaatioHierarkia)]
}

const hasOrganisaatio = (orgs: OrganisaatioHierarkia[], oid: string): boolean =>
  orgs.some(
    (org) => org.oid === oid || hasOrganisaatio(org.children || [], oid)
  )

const organisaatioToOrganisaatioHierarkia = <T extends Organisaatio>(
  org: T
): OrganisaatioHierarkia =>
  OrganisaatioHierarkia({
    oid: getOrganisaatioId(org),
    aktiivinen: true,
    nimi: org.nimi || localize('–'),
    organisaatiotyypit: organisaatiotyypit(org),
    kotipaikka: getOrganisaationKotipaikka(org),
    oppilaitosnumero: isOppilaitos(org) ? org.oppilaitosnumero : undefined,
    yTunnus: isKoulutustoimija(org) ? org.yTunnus : undefined
  })

const organisaatiotyypit = (org: Organisaatio): string[] =>
  isKoulutustoimija(org)
    ? ['KOULUTUSTOIMIJA']
    : isOppilaitos(org)
      ? ['OPPILAITOS']
      : isToimipiste(org)
        ? ['TOIMIPISTE']
        : []
