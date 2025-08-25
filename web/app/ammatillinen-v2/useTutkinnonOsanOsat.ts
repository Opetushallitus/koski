import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../api-fetch'
import { fetchPerusteTutkinnonOsanOsat } from '../util/koskiApi'

const cache = createPreferLocalCache(fetchPerusteTutkinnonOsanOsat)

export const useTutkinnonOsanOsat = (
  perusteenDiaarinumero: string,
  tutkinnonOsanKoodi: string
) => {
  const result = useApiWithParams(
    fetchPerusteTutkinnonOsanOsat,
    [perusteenDiaarinumero, tutkinnonOsanKoodi],
    cache
  )

  if (isSuccess(result)) {
    return result.data
  } else {
    return []
  }
}
