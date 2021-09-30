import * as E from "fp-ts/Either"
import { useCallback } from "react"
import {
  fetchHakeutumisvalvonnanKunnalleTehdytIlmoitukset,
  fetchHakeutumisvalvonnanKunnalleTehdytIlmoituksetCache,
  fetchNivelvaiheenOppijat,
  fetchNivelvaiheenOppijatCache,
  fetchOppijat,
  fetchOppijatCache,
  fetchSuorittamisvalvonnanKunnalleTehdytIlmoitukset,
  fetchSuorittamisvalvonnanKunnalleTehdytIlmoituksetCache,
  setMuuHaku,
} from "../../api/api"
import { ApiError, ApiResponse } from "../../api/apiFetch"
import {
  ApiMethodHook,
  useApiMethod,
  useApiWithParams,
  useLocalDataCopy,
} from "../../api/apiHooks"
import { isError, isLoading } from "../../api/apiUtils"
import { ApiCache } from "../../api/cache"
import { OpiskeluoikeusSuppeatTiedot } from "../../state/apitypes/opiskeluoikeus"
import {
  lisätietoMatches,
  OpiskeluoikeusLisätiedot,
  OppijaHakutilanteillaSuppeatTiedot,
} from "../../state/apitypes/oppija"
import { Oid } from "../../state/common"
import { upsert } from "../../utils/arrays"

export type UseOppijatDataApi = {
  data: OppijaHakutilanteillaSuppeatTiedot[] | null
  setMuuHaku: (
    oppijaOid: Oid,
    opiskeluoikeus: OpiskeluoikeusSuppeatTiedot,
    value: boolean
  ) => void
  isLoading: boolean
  errors?: ApiError[]
}

export type UseOppijatDataApiReload = {
  reload: () => void
}

const oppijatFetchHook = (
  fetchFn: (
    organisaatioOid: Oid
  ) => Promise<ApiResponse<OppijaHakutilanteillaSuppeatTiedot[]>>,
  cache: ApiCache<
    OppijaHakutilanteillaSuppeatTiedot[],
    [organisaatioOid: string]
  >
) => (organisaatioOid?: Oid): UseOppijatDataApi & UseOppijatDataApiReload => {
  const oppijatFetch = useApiWithParams(
    fetchFn,
    organisaatioOid ? [organisaatioOid] : undefined,
    // @ts-ignore
    cache
  )

  const reload = useCallback(() => {
    if (organisaatioOid) {
      cache.clearAll()
      oppijatFetch.call(organisaatioOid)
    }
  }, [oppijatFetch, organisaatioOid])

  return {
    ...useOppijatDataAPI(organisaatioOid, oppijatFetch, cache),
    reload,
  }
}

export const useOppijatData = oppijatFetchHook(fetchOppijat, fetchOppijatCache)

export const useNivelvaiheenOppijatData = oppijatFetchHook(
  fetchNivelvaiheenOppijat,
  fetchNivelvaiheenOppijatCache
)

export const useHakeutumisvalvonnanKunnalleTehdytIlmoitukset = (
  organisaatioOid?: Oid
): UseOppijatDataApi => {
  const oppijatFetch = useApiWithParams(
    fetchHakeutumisvalvonnanKunnalleTehdytIlmoitukset,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchHakeutumisvalvonnanKunnalleTehdytIlmoituksetCache
  )

  return useOppijatDataAPI(organisaatioOid, oppijatFetch, fetchOppijatCache)
}

export const useSuorittamisvalvonnanKunnalleTehdytIlmoitukset = (
  organisaatioOid?: Oid
): UseOppijatDataApi => {
  const oppijatFetch = useApiWithParams(
    fetchSuorittamisvalvonnanKunnalleTehdytIlmoitukset,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchSuorittamisvalvonnanKunnalleTehdytIlmoituksetCache
  )

  return useOppijatDataAPI(organisaatioOid, oppijatFetch, fetchOppijatCache)
}

const useOppijatDataAPI = (
  organisaatioOid: Oid | undefined,
  oppijatFetch: ApiMethodHook<
    OppijaHakutilanteillaSuppeatTiedot[],
    [organisaatioOid: string]
  >,
  cache: ApiCache<
    OppijaHakutilanteillaSuppeatTiedot[],
    [organisaatioOid: string]
  >
): UseOppijatDataApi => {
  const [localData, setLocalData] = useLocalDataCopy(oppijatFetch)

  const saveMuuHakuState = useApiMethod(setMuuHaku)
  const storeMuuHakuState = useCallback(
    async (
      oppijaOid: Oid,
      opiskeluoikeus: OpiskeluoikeusSuppeatTiedot,
      value: boolean
    ) => {
      cache.clear([organisaatioOid!!])

      const response = await saveMuuHakuState.call(
        oppijaOid,
        opiskeluoikeus,
        value
      )

      if (E.isRight(response) && localData) {
        const oppija = localData.find(oppijaOidEqualsTo(oppijaOid))

        if (oppija) {
          const lisätiedot: OpiskeluoikeusLisätiedot[] = upsert(
            oppija.lisätiedot,
            lisätietoMatches(
              oppijaOid,
              opiskeluoikeus.oid,
              opiskeluoikeus.oppilaitos.oid
            ),
            {
              oppijaOid,
              opiskeluoikeusOid: opiskeluoikeus.oid,
              oppilaitosOid: opiskeluoikeus.oppilaitos.oid,
              muuHaku: value,
            }
          )

          setLocalData(
            upsert(localData, oppijaOidEqualsTo(oppijaOid), {
              ...oppija,
              lisätiedot,
            })
          )
        }
      }
    },
    [cache, localData, organisaatioOid, saveMuuHakuState, setLocalData]
  )

  return {
    data: localData,
    setMuuHaku: storeMuuHakuState,
    isLoading: isLoading(oppijatFetch),
    errors: isError(oppijatFetch) ? oppijatFetch.errors : undefined,
  }
}

const oppijaOidEqualsTo = (oppijaOid: Oid) => (
  o: OppijaHakutilanteillaSuppeatTiedot
) => o.oppija.henkilö.oid === oppijaOid
