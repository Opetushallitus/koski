import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../api-fetch'
import {
  fetchPerusteTutkinnonOsat,
  LisättävätTutkinnonOsat
} from '../util/koskiApi'

const cache = createPreferLocalCache(fetchPerusteTutkinnonOsat)

const emptyResult: LisättävätTutkinnonOsat = {
  osat: []
}

export const useTutkinnonOsat = (
  perusteenDiaarinumero: string | undefined,
  tutkinnonOsaRyhmä: string | undefined
): LisättävätTutkinnonOsat => {
  const params =
    perusteenDiaarinumero === undefined || tutkinnonOsaRyhmä === undefined
      ? undefined
      : ([perusteenDiaarinumero, tutkinnonOsaRyhmä] as [string, string])

  const result = useApiWithParams(fetchPerusteTutkinnonOsat, params, cache)

  if (isSuccess(result)) {
    return result.data
  } else {
    return emptyResult
  }
}
