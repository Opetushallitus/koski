import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../api-fetch'
import { fetchOppilaitoksenPerusteet } from '../util/koskiApi'
import { useMemo, useState } from 'react'
import { t } from '../i18n/i18n'
import { SelectOption } from '../components-v2/controls/Select'
import { TutkintoPeruste } from '../types/fi/oph/koski/tutkinto/TutkintoPeruste'

const cache = createPreferLocalCache(fetchOppilaitoksenPerusteet)

export const useTutkinnot = (oppilaitosOid?: string) => {
  const [query, setQuery] = useState<string>()

  const tutkinnot = useApiWithParams(
    fetchOppilaitoksenPerusteet,
    oppilaitosOid !== undefined ? [oppilaitosOid, query] : undefined,
    cache
  )

  const options = useMemo(
    () =>
      isSuccess(tutkinnot)
        ? tutkinnot.data
            .map(
              (k) =>
                ({
                  key: tutkintoKey(k),
                  value: k,
                  label: `${k.tutkintoKoodi} ${t(k.nimi)} (${k.diaarinumero})`
                }) satisfies SelectOption<TutkintoPeruste>
            )
            .sort((a, b) => a.key.localeCompare(b.key))
        : [],
    [tutkinnot]
  )

  return { options, setQuery }
}

export const tutkintoKey = (tutkinto: TutkintoPeruste): string =>
  `${tutkinto.tutkintoKoodi}_${tutkinto.diaarinumero}`
