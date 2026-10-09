import { useEffect, useMemo, useState } from 'react'
import { kuunteleOsoitetta } from '../util/router'

// Osoitteen hakuosa reaktiivisena: päivittyy kaikesta navigoinnista, myös
// vanhan käyttöliittymän ja selaimen historian kautta tulevasta.
const useLocationSearch = (): string => {
  const [search, setSearch] = useState(window.location.search)
  useEffect(() => {
    const päivitä = () => setSearch(window.location.search)
    const lopeta = kuunteleOsoitetta(päivitä)
    päivitä()
    return lopeta
  }, [])
  return search
}

export const useSearchParam = (key: string): string | null => {
  const search = useLocationSearch()
  return useMemo(() => new URLSearchParams(search).get(key), [key, search])
}

// Versiohistoriassa katsotaan yhden opiskeluoikeuden versiota
// (?opiskeluoikeus=<oid>&versionumero=<n>). Samalla sivulla näkyvät muut
// opiskeluoikeudet pysyvät nykyisessä versiossaan.
export const opiskeluoikeudenVersionumero = (
  search: string,
  opiskeluoikeusOid: string | undefined
): string | null => {
  const params = new URLSearchParams(search)
  return opiskeluoikeusOid !== undefined &&
    params.get('opiskeluoikeus') === opiskeluoikeusOid
    ? params.get('versionumero')
    : null
}

export const useVersionumero = (
  opiskeluoikeusOid: string | undefined
): string | null => {
  const search = useLocationSearch()
  return useMemo(
    () => opiskeluoikeudenVersionumero(search, opiskeluoikeusOid),
    [search, opiskeluoikeusOid]
  )
}
