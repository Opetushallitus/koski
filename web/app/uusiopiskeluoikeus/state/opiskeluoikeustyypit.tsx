import { useCallback, useMemo } from 'react'
import { isError, isSuccess, useApiWithParams } from '../../api-fetch'
import {
  SelectOption,
  koodiviiteToOption
} from '../../components-v2/controls/Select'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { fetchOrganisaationOpiskeluoikeustyypit } from '../../util/koskiApi'

export type OpiskeluoikeustyypitResult = {
  options: Array<SelectOption<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>>
  error: boolean
  retry: () => void
}

export const useOpiskeluoikeustyypit = (
  organisaatio?: OrganisaatioHierarkia
): OpiskeluoikeustyypitResult => {
  const apiCall = useApiWithParams(
    fetchOrganisaationOpiskeluoikeustyypit<'opiskeluoikeudentyyppi'>,
    organisaatio?.oid !== undefined ? [organisaatio?.oid] : undefined
  )
  const options = useMemo(
    () => (isSuccess(apiCall) ? apiCall.data : []).map(koodiviiteToOption),
    [apiCall]
  )
  const retry = useCallback(() => {
    if (organisaatio?.oid !== undefined) {
      apiCall.call(organisaatio.oid)
    }
  }, [apiCall, organisaatio?.oid])

  return { options, error: isError(apiCall), retry }
}
