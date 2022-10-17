import * as A from "fp-ts/Array"
import * as E from "fp-ts/Either"
import { useCallback, useMemo } from "react"
import {
  fetchHakeutumisvalvonnanKunnalleTehdytIlmoitukset,
  fetchHakeutumisvalvonnanKunnalleTehdytIlmoituksetCache,
  fetchNivelvaiheenOppijat,
  fetchNivelvaiheenOppijatCache,
  fetchNivelvaiheenOppijatHakutiedoilla,
  fetchNivelvaiheenOppijatHakutiedoillaCache,
  fetchOppijat,
  fetchOppijatCache,
  fetchOppijatHakutiedoilla,
  fetchOppijatHakutiedoillaCache,
  fetchSuorittamisvalvonnanKunnalleTehdytIlmoitukset,
  fetchSuorittamisvalvonnanKunnalleTehdytIlmoituksetCache,
  setMuuHaku,
} from "../../api/api"
import { ApiError, ApiResponse } from "../../api/apiFetch"
import {
  ApiMethodHook,
  useApiMethod,
  useApiSequence,
  useApiWithParams,
  useLocalDataCopy,
  useOnApiSuccess,
} from "../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../api/apiUtils"
import { ApiCache } from "../../api/cache"
import { OpiskeluoikeusSuppeatTiedot } from "../../state/apitypes/opiskeluoikeus"
import {
  lisätietoMatches,
  OpiskeluoikeusLisätiedot,
  OppijaHakutilanteillaSuppeatTiedot,
} from "../../state/apitypes/oppija"
import { Oid } from "../../state/common"
import { useSafeState } from "../../state/useSafeState"
import { upsert } from "../../utils/arrays"

const HAKUTIEDOT_FETCH_LIST_SIZE = 100

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

const oppijatFetchHook =
  (
    fetchOppijatFn: (
      organisaatioOid: Oid
    ) => Promise<ApiResponse<OppijaHakutilanteillaSuppeatTiedot[]>>,
    oppijatCache: ApiCache<
      OppijaHakutilanteillaSuppeatTiedot[],
      [organisaatioOid: string]
    >,
    fetchHakutiedotFn: (
      organisaatioOid: Oid,
      oppijaOids: Oid[]
    ) => Promise<ApiResponse<OppijaHakutilanteillaSuppeatTiedot[]>>,
    hakutiedotCache: ApiCache<
      OppijaHakutilanteillaSuppeatTiedot[],
      [organisaatioOid: Oid, oppijaOids: Oid[]]
    >
  ) =>
  (organisaatioOid?: Oid): UseOppijatDataApi & UseOppijatDataApiReload => {
    const [oppijaOids, setOppijaOids] = useSafeState<Oid[]>([])

    const oppijatFetch = useApiWithParams(
      fetchOppijatFn,
      organisaatioOid ? [organisaatioOid] : undefined,
      // @ts-ignore
      oppijatCache
    )

    const hakutiedotFetches = useApiSequence(
      fetchHakutiedotFn,
      oppijaOids,
      useCallback(
        (oids) => {
          const [oppijaOids, pendingOppijaOids] = A.splitAt(
            HAKUTIEDOT_FETCH_LIST_SIZE
          )(oids)
          if (A.isNonEmpty(oppijaOids) && organisaatioOid) {
            const fetchParams: Parameters<typeof fetchOppijatHakutiedoilla> = [
              organisaatioOid,
              oppijaOids,
            ]
            return [fetchParams, pendingOppijaOids]
          }
          return null
        },
        [organisaatioOid]
      ),
      hakutiedotCache
    )

    const hakutiedotFlattened = useMemo(
      () => A.flatten(hakutiedotFetches.filter(isSuccess).map((h) => h.data)),
      [hakutiedotFetches]
    )

    useOnApiSuccess(oppijatFetch, (suppeatTiedot) => {
      setOppijaOids(suppeatTiedot.data.map((x) => x.oppija.henkilö.oid))
    })

    const reload = useCallback(() => {
      if (organisaatioOid) {
        oppijatCache.clearAll()
        oppijatFetch.call(organisaatioOid)
      }
    }, [oppijatFetch, organisaatioOid])

    return {
      ...useOppijatDataAPI(
        organisaatioOid,
        oppijatFetch,
        oppijatCache,
        hakutiedotFlattened
      ),
      reload,
    }
  }

export const useOppijatData = oppijatFetchHook(
  fetchOppijat,
  fetchOppijatCache,
  fetchOppijatHakutiedoilla,
  fetchOppijatHakutiedoillaCache
)

export const useNivelvaiheenOppijatData = oppijatFetchHook(
  fetchNivelvaiheenOppijat,
  fetchNivelvaiheenOppijatCache,
  fetchNivelvaiheenOppijatHakutiedoilla,
  fetchNivelvaiheenOppijatHakutiedoillaCache
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
  >,
  hakutiedotResults?: OppijaHakutilanteillaSuppeatTiedot[]
): UseOppijatDataApi => {
  const [localData, setLocalData] = useLocalDataCopy(oppijatFetch)

  // Yhdistä erikseen haetut hakutilanteet Valpas-kannoista haettuihin datoihin
  const mergedData = useMemo(
    () =>
      localData?.map((d) => {
        const hakutilanne = hakutiedotResults?.find(
          oppijaOidEqualsTo(d.oppija.henkilö.oid)
        )
        return {
          ...d,
          hakutilanteet: hakutilanne?.hakutilanteet || d.hakutilanteet,
          hakutilanneError: hakutilanne?.hakutilanneError,
          isLoadingHakutilanteet: !hakutilanne,
        }
      }) || null,
    [hakutiedotResults, localData]
  )

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

      if (E.isRight(response) && mergedData) {
        const oppija = mergedData.find(oppijaOidEqualsTo(oppijaOid))

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
            upsert(mergedData, oppijaOidEqualsTo(oppijaOid), {
              ...oppija,
              lisätiedot,
            })
          )
        }
      }
    },
    [cache, mergedData, organisaatioOid, saveMuuHakuState, setLocalData]
  )

  return {
    data: mergedData,
    setMuuHaku: storeMuuHakuState,
    isLoading: isLoading(oppijatFetch),
    errors: isError(oppijatFetch) ? oppijatFetch.errors : undefined,
  }
}

const oppijaOidEqualsTo =
  (oppijaOid: Oid) => (o: OppijaHakutilanteillaSuppeatTiedot) =>
    o.oppija.henkilö.oid === oppijaOid
