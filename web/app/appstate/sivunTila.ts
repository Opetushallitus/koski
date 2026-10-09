import { useEffect, useMemo, useState } from 'react'
import { useSearchParam } from './useSearchParam'

// Oppijan sivun tila, jota sekä vanha että uusi käyttöliittymä lukevat: mikä
// opiskeluoikeus on muokkaustilassa ja minkä opiskeluoikeuden versiota katsotaan.

// --- Katseltava versio ---------------------------------------------------

// Versiohistoriassa katsotaan yhden opiskeluoikeuden versiota
// (?opiskeluoikeus=<oid>&versionumero=<n>). Samalla sivulla näkyvät muut
// opiskeluoikeudet pysyvät nykyisessä versiossaan. Pelkkä opiskeluoikeus jää
// osoitteeseen versiosta poistuttaessa, jotta sen välilehti pysyy valittuna.
export type KatseltavaVersio = {
  opiskeluoikeusOid: string
  versionumero: string
}

export const osoitteenOpiskeluoikeus = (
  search: string = window.location.search
): string | undefined =>
  new URLSearchParams(search).get('opiskeluoikeus') || undefined

export const katseltavaVersio = (
  search: string = window.location.search
): KatseltavaVersio | undefined => {
  const params = new URLSearchParams(search)
  const opiskeluoikeusOid = params.get('opiskeluoikeus')
  const versionumero = params.get('versionumero')
  return opiskeluoikeusOid && versionumero
    ? { opiskeluoikeusOid, versionumero }
    : undefined
}

export const opiskeluoikeudenVersionumero = (
  opiskeluoikeusOid: string | undefined,
  search: string = window.location.search
): string | null => {
  const versio = katseltavaVersio(search)
  return versio && versio.opiskeluoikeusOid === opiskeluoikeusOid
    ? versio.versionumero
    : null
}

export const useKatseltavaVersio = (): KatseltavaVersio | undefined => {
  const opiskeluoikeusOid = useSearchParam('opiskeluoikeus')
  const versionumero = useSearchParam('versionumero')
  return useMemo(
    () =>
      opiskeluoikeusOid && versionumero
        ? { opiskeluoikeusOid, versionumero }
        : undefined,
    [opiskeluoikeusOid, versionumero]
  )
}

export const useVersionumero = (
  opiskeluoikeusOid: string | undefined
): string | null => {
  const versio = useKatseltavaVersio()
  return versio && versio.opiskeluoikeusOid === opiskeluoikeusOid
    ? versio.versionumero
    : null
}

// --- Muokattava opiskeluoikeus ------------------------------------------

// Sivulla saa olla muokkaustilassa vain yksi opiskeluoikeus kerrallaan, oli se
// vanhan tai uuden käyttöliittymän. Vanha käyttöliittymä pitää muokattavan
// opiskeluoikeuden osoitteessa (?edit=<oid>). Uusi käyttöliittymä ei voi
// käyttää samaa parametria, koska se käynnistää vanhan käyttöliittymän
// muokkaustilan, joten sen muokattava opiskeluoikeus pidetään tässä.
let uudenKäyttöliittymänMuokattava: string | undefined
const kuuntelijat = new Set<() => void>()

const ilmoitaMuutoksesta = () =>
  kuuntelijat.forEach((kuuntelija) => kuuntelija())

// Palauttaa funktion, joka päättää muokkauksen.
export const aloitaUudenKäyttöliittymänMuokkaus = (
  opiskeluoikeusOid: string
): (() => void) => {
  uudenKäyttöliittymänMuokattava = opiskeluoikeusOid
  ilmoitaMuutoksesta()
  return () => {
    if (uudenKäyttöliittymänMuokattava === opiskeluoikeusOid) {
      uudenKäyttöliittymänMuokattava = undefined
      ilmoitaMuutoksesta()
    }
  }
}

export const useMuokattavaOpiskeluoikeus = (): string | undefined => {
  const vanhanKäyttöliittymänMuokattava = useSearchParam('edit')
  const [uudenMuokattava, setUudenMuokattava] = useState(
    uudenKäyttöliittymänMuokattava
  )
  useEffect(() => {
    const päivitä = () => setUudenMuokattava(uudenKäyttöliittymänMuokattava)
    kuuntelijat.add(päivitä)
    päivitä()
    return () => {
      kuuntelijat.delete(päivitä)
    }
  }, [])
  return vanhanKäyttöliittymänMuokattava || uudenMuokattava
}
