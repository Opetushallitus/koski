import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../api-fetch'
import { fetchPerusteTutkinnonOsaRyhmät } from '../util/koskiApi'

const cache = createPreferLocalCache(fetchPerusteTutkinnonOsaRyhmät)

export const useTutkinnonOsaRyhmät = (
  perusteenDiaarinumero: string,
  suoritustapa: string
) => {
  const result = useApiWithParams(
    fetchPerusteTutkinnonOsaRyhmät,
    [perusteenDiaarinumero, suoritustapa],
    cache
  )

  if (isSuccess(result)) {
    return result.data
  } else {
    return []
  }
}
