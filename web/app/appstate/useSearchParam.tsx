import { useEffect, useMemo, useState } from 'react'
import { LOCATION_CHANGE_EVENT } from '../util/url'

// Osoitteen hakuosa reaktiivisena: päivittyy selaimen popstate-tapahtumasta
// (eteen/taakse) sekä pushLocationin lähettämästä locationchange-tapahtumasta,
// jolloin komponentit renderöityvät uudelleen ilman koko sivun uudelleenlatausta.
const useLocationSearch = (): string => {
  const [search, setSearch] = useState(window.location.search)
  useEffect(() => {
    const onChange = () => setSearch(window.location.search)
    window.addEventListener('popstate', onChange)
    window.addEventListener(LOCATION_CHANGE_EVENT, onChange)
    return () => {
      window.removeEventListener('popstate', onChange)
      window.removeEventListener(LOCATION_CHANGE_EVENT, onChange)
    }
  }, [])
  return search
}

export const useSearchParam = (key: string): string | null => {
  const search = useLocationSearch()
  return useMemo(() => new URLSearchParams(search).get(key), [key, search])
}

export const useVersionumero = () => useSearchParam('versionumero')
