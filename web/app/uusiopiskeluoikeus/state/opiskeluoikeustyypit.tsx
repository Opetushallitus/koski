import { useMemo } from 'react'
import { isSuccess, useApiWithParams } from '../../api-fetch'
import {
  SelectOption,
  koodiviiteToOption
} from '../../components-v2/controls/Select'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { fetchOrganisaationOpiskeluoikeustyypit } from '../../util/koskiApi'

export const useOpiskeluoikeustyypit = (
  organisaatio?: OrganisaatioHierarkia
): Array<SelectOption<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>> => {
  const apiCall = useApiWithParams(
    fetchOrganisaationOpiskeluoikeustyypit<'opiskeluoikeudentyyppi'>,
    organisaatio?.oid !== undefined ? [organisaatio?.oid] : undefined
  )
  const options = useMemo(
    () => (isSuccess(apiCall) ? apiCall.data : []).map(koodiviiteToOption),
    [apiCall]
  )
  return options
}
