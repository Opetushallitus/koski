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

// Ilman ryhmää palautetaan kaikki perusteen tutkinnon osat
export const useTutkinnonOsat = (
  perusteenDiaarinumero: string | undefined,
  tutkinnonOsaRyhmä?: string
): LisättävätTutkinnonOsat => {
  const params =
    perusteenDiaarinumero === undefined
      ? undefined
      : ([perusteenDiaarinumero, tutkinnonOsaRyhmä] as [
          string,
          string | undefined
        ])

  const result = useApiWithParams(fetchPerusteTutkinnonOsat, params, cache)

  if (isSuccess(result)) {
    return result.data
  } else {
    return emptyResult
  }
}
