import { useMemo } from 'react'
import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../../api-fetch'
import {
  groupKoodistoToOptions,
  koodiviiteToOption
} from '../../components-v2/controls/Select'
import { fetchTutkinnonPerusteenSuoritustavat } from '../../util/koskiApi'
import { UusiOpiskeluoikeusDialogState } from './state'
import { useKoodisto } from '../../appstate/koodisto'

const cache = createPreferLocalCache(fetchTutkinnonPerusteenSuoritustavat)

export const useAmmatillisenTutkinnonSuoritustapa = (
  state: UusiOpiskeluoikeusDialogState
) => {
  const allOptions = useKoodisto('ammatillisentutkinnonsuoritustapa')

  const tutkinto = state.tutkinto.value
  const optionsForTutkinto = useApiWithParams(
    fetchTutkinnonPerusteenSuoritustavat,
    tutkinto && [tutkinto.diaarinumero],
    cache
  )

  return useMemo(
    () =>
      isSuccess(optionsForTutkinto)
        ? optionsForTutkinto.data.map(koodiviiteToOption)
        : allOptions
          ? groupKoodistoToOptions(allOptions)
          : [],
    [allOptions, optionsForTutkinto]
  )
}
