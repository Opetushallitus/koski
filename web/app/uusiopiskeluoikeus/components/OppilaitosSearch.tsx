import React, { useCallback, useMemo, useState } from 'react'
import { useOrganisaatioHierarkiaSearch } from '../../appstate/organisaatioHierarkia'
import { Select, SelectOption } from '../../components-v2/controls/Select'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import {
  OrgType,
  filterOrgsByType,
  organisaatiohierarkiaToOption
} from './OppilaitosSelect'

export type OppilaitosSearchProps = {
  value?: OrganisaatioHierarkia
  onChange: (org?: OrganisaatioHierarkia) => void
  orgTypes?: OrgType[]
}

export const OppilaitosSearch = (props: OppilaitosSearchProps) => {
  const [query, setQuery] = useState<string>()
  const options = useOrganisaatioOptions(props.orgTypes, query)
  const onChange = useCallback(
    (opt?: SelectOption<OrganisaatioHierarkia>) => props.onChange(opt?.value),
    [props]
  )

  return (
    <Select
      inlineOptions
      options={options}
      value={props.value?.oid}
      onChange={onChange}
      onSearch={setQuery}
      testId="oppilaitos"
    />
  )
}

const useOrganisaatioOptions = (
  orgTypes?: OrgType[],
  query?: string
): Array<SelectOption<OrganisaatioHierarkia>> => {
  const organisaatiot = useOrganisaatioHierarkiaSearch(query)
  return useMemo(() => {
    const filtered = orgTypes
      ? filterOrgsByType(organisaatiot, orgTypes)
      : organisaatiot
    return filtered.map(organisaatiohierarkiaToOption(orgTypes))
  }, [orgTypes, organisaatiot])
}
