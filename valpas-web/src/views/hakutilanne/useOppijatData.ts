import * as E from "fp-ts/Either"
import { useCallback } from "react"
import {
  fetchOppijat,
  fetchOppijatCache,
  fetchOppijatKuntailmoituksilla,
  fetchOppijatKuntailmoituksillaCache,
  setMuuHaku,
} from "../../api/api"
import { ApiError } from "../../api/apiFetch"
import {
  ApiMethodHook,
  useApiMethod,
  useApiWithParams,
  useLocalDataCopy,
} from "../../api/apiHooks"
import { isError, isLoading } from "../../api/apiUtils"
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

export const useOppijatData = (
  organisaatioOid?: Oid
): UseOppijatDataApi & UseOppijatDataApiReload => {
  const oppijatFetch = useApiWithParams(
    fetchOppijat,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchOppijatCache
  )

  const reload = useCallback(() => {
    if (organisaatioOid) {
      fetchOppijatCache.clearAll()
      oppijatFetch.call(organisaatioOid)
    }
  }, [oppijatFetch, organisaatioOid])

  return {
    ...useOppijatDataAPI(organisaatioOid, oppijatFetch),
    reload,
  }
}

export const useOppijatKuntailmoituksillaData = (
  organisaatioOid?: Oid
): UseOppijatDataApi => {
  const oppijatFetch = useApiWithParams(
    fetchOppijatKuntailmoituksilla,
    organisaatioOid ? [organisaatioOid] : undefined,
    fetchOppijatKuntailmoituksillaCache
  )

  return useOppijatDataAPI(organisaatioOid, oppijatFetch)
}

const useOppijatDataAPI = (
  organisaatioOid: Oid | undefined,
  oppijatFetch: ApiMethodHook<
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
      fetchOppijatCache.clear([organisaatioOid!!])

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
    [localData, organisaatioOid, saveMuuHakuState, setLocalData]
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
