import { isEmpty } from 'fp-ts/lib/Array'
import React, { useCallback, useMemo } from 'react'
import { useOrganisaatioHierarkia } from '../appstate/organisaatioHierarkia'
import { Select, SelectOption } from '../components-v2/controls/Select'
import { t } from '../i18n/i18n'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { intersects } from '../util/array'

export type OppilaitosSelectProps = {
  value?: OrganisaatioHierarkia
  onChange: (org?: OrganisaatioHierarkia) => void
  orgTypes?: OrgType[]
}

export type OrgType =
  | 'KOULUTUSTOIMIJA'
  | 'TOIMIPISTE'
  | 'OPPILAITOS'
  | 'OPPISOPIMUSTOIMIPISTE'
  | 'VARHAISKASVATUKSEN_TOIMIPAIKKA'

export const OppilaitosSelect = (props: OppilaitosSelectProps) => {
  const options = useOrganisaatioOptions(props.orgTypes)
  const onChange = useCallback(
    (opt?: SelectOption<OrganisaatioHierarkia>) => props.onChange(opt?.value),
    [props]
  )

  return (
    <Select
      options={options}
      value={props.value?.oid}
      onChange={onChange}
      testId="oppilaitos"
    />
  )
}

const useOrganisaatioOptions = (
  orgTypes?: OrgType[]
): Array<SelectOption<OrganisaatioHierarkia>> => {
  const organisaatiot = useOrganisaatioHierarkia()
  return useMemo(() => {
    const filtered = orgTypes
      ? filterOrgsByType(organisaatiot, orgTypes)
      : organisaatiot
    return filtered.map(organisaatiohierarkiaToOption(orgTypes))
  }, [orgTypes, organisaatiot])
}

const filterOrgsByType = (
  orgs: OrganisaatioHierarkia[],
  orgTypes: OrgType[]
): OrganisaatioHierarkia[] =>
  orgs.flatMap((org) => {
    const children = filterOrgsByType(org.children, orgTypes)
    return children.length > 0 || intersects(org.organisaatiotyypit, orgTypes)
      ? [{ ...org, children }]
      : []
  })

const organisaatiohierarkiaToOption =
  (orgTypes?: OrgType[]) =>
  (org: OrganisaatioHierarkia): SelectOption<OrganisaatioHierarkia> => ({
    key: org.oid,
    label: t(org.nimi),
    value: org,
    children: isEmpty(org.children)
      ? undefined
      : org.children.map(organisaatiohierarkiaToOption(orgTypes)),
    isGroup: orgTypes ? !intersects(org.organisaatiotyypit, orgTypes) : false
  })
