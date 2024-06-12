import * as A from 'fp-ts/Array'
import * as E from 'fp-ts/Either'
import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { nonNull } from '../util/fp/arrays'
import { fetchPeruste } from '../util/koskiApi'

export type Peruste = Omit<
  Koodistokoodiviite<string, string>,
  '$class' | 'koodistoVersio' | 'lyhytNimi'
>

/**
 * Palauttaa annetun diaarinumeron mukaiset perusteet.
 */
export function usePeruste(diaariNumero?: string): Peruste[] | null {
  const perusteet = usePerusteet(diaariNumero)

  return useMemo(
    () =>
      diaariNumero
        ? perusteet[diaariNumero]
          ? perusteet[diaariNumero]
          : null
        : [],
    [diaariNumero, perusteet]
  )
}

/**
 * Palauttaa yhden tai useamman diaarinumeron perusteet.
 *
 */
const usePerusteet = (...diaariNumerot: Array<string | null | undefined>) => {
  const { perusteet, loadPerusteet } = useContext(PerusteContext)

  useEffect(() => {
    loadPerusteet(diaariNumerot.filter(nonNull))
  }, [diaariNumerot, loadPerusteet])

  return useMemo(() => {
    const k = diaariNumerot
      .filter(nonNull)
      .reduce<Record<string, Peruste[]>>((prev, diaarinumero) => {
        const p = perusteet[diaarinumero]
        if (Array.isArray(p)) {
          return { ...prev, [diaarinumero]: p }
        }
        return { ...prev }
      }, {})
    return k
  }, [diaariNumerot, perusteet])
}

const Loading = Symbol('loading')

export type PerusteContextValue = {
  readonly perusteet: PerusteRecord
  readonly loadPerusteet: (diaarinumerot: string[]) => void
}

const PerusteContext = React.createContext<PerusteContextValue>({
  perusteet: {},
  loadPerusteet: () => {}
})

export type PerusteProviderProps = React.PropsWithChildren<{}>

export type PerusteRecord = Record<string, Peruste[] | typeof Loading>

class PerusteLoader {
  perusteet: PerusteRecord = {}

  async loadPerusteet(diaarinumerot: string[]): Promise<boolean> {
    const unfetchedDiaarinumerot = diaarinumerot.filter(
      (dn) => !this.perusteet[dn]
    )
    if (A.isNonEmpty(unfetchedDiaarinumerot)) {
      unfetchedDiaarinumerot.forEach((uri) => {
        this.perusteet[uri] = Loading
      })

      const unfetchedPerusteet = await Promise.all(
        unfetchedDiaarinumerot.map(async (dn) => {
          const perusteRequest = await fetchPeruste(dn)
          return {
            diaarinumero: dn,
            perusteRequest
          }
        })
      )
      unfetchedPerusteet.forEach(({ perusteRequest, diaarinumero }) => {
        if (E.isRight(perusteRequest)) {
          // @ts-expect-error TODO: Tyypitys kuntoon, fp-ts käyttöön
          this.perusteet[diaarinumero] = perusteRequest.right.data
        }
      })

      return true
    }
    return false
  }
}

const perusteLoaderSingleton = new PerusteLoader()

export const PerusteProvider = (props: PerusteProviderProps) => {
  const [perusteet, setPerusteet] = useState<PerusteRecord>({})

  const loadPerusteet = useCallback(async (koodistoUris: string[]) => {
    if (await perusteLoaderSingleton.loadPerusteet(koodistoUris)) {
      setPerusteet(perusteLoaderSingleton.perusteet)
    }
  }, [])

  const providedValue: PerusteContextValue = useMemo(
    () => ({ perusteet, loadPerusteet }),
    [loadPerusteet, perusteet]
  )

  return (
    <PerusteContext.Provider value={providedValue}>
      {props.children}
    </PerusteContext.Provider>
  )
}
