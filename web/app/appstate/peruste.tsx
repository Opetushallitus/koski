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
import {
  LoadingOptions,
  SelectOption,
  perusteToOption
} from '../components-v2/controls/Select'
import { PropsWithOnlyChildren } from '../util/react'

export type Peruste = Omit<
  Koodistokoodiviite<string, string>,
  '$class' | 'koodistoVersio' | 'lyhytNimi'
>

/**
 * Palauttaa annetun suoritustyypin mukaiset perusteet.
 */
export function usePeruste(suoritustyyppi?: string): Peruste[] | null {
  const perusteet = usePerusteet(suoritustyyppi)

  return useMemo(
    () =>
      suoritustyyppi
        ? perusteet[suoritustyyppi]
          ? perusteet[suoritustyyppi]
          : null
        : [],
    [suoritustyyppi, perusteet]
  )
}

/**
 * Palauttaa annetun suoritustyypin mukaiset perusteet Select-komponentin käyttämässä muodossa.
 */
export function usePerusteSelectOptions(
  suoritustyyppi?: string
): SelectOption<Peruste>[] {
  const perusteet = usePeruste(suoritustyyppi)
  return useMemo(
    () => perusteet?.map(perusteToOption) || LoadingOptions,
    [perusteet]
  )
}

/**
 * Palauttaa yhden tai useamman suoritustyypin perusteet.
 *
 */
const usePerusteet = (...suoritustyypit: Array<string | null | undefined>) => {
  const { perusteet, loadPerusteet } = useContext(PerusteContext)

  useEffect(() => {
    loadPerusteet(suoritustyypit.filter(nonNull))
  }, [suoritustyypit, loadPerusteet])

  return useMemo(() => {
    const k = suoritustyypit
      .filter(nonNull)
      .reduce<Record<string, Peruste[]>>((prev, suoritustyyppi) => {
        const p = perusteet[suoritustyyppi]
        if (Array.isArray(p)) {
          return { ...prev, [suoritustyyppi]: p }
        }
        return { ...prev }
      }, {})
    return k
  }, [suoritustyypit, perusteet])
}

const Loading = Symbol('loading')

export type PerusteContextValue = {
  readonly perusteet: PerusteRecord
  readonly loadPerusteet: (suoritustyypit: string[]) => void
}

const PerusteContext = React.createContext<PerusteContextValue>({
  perusteet: {},
  loadPerusteet: () => {}
})

export type PerusteProviderProps = PropsWithOnlyChildren

export type PerusteRecord = Record<string, Peruste[] | typeof Loading>

class PerusteLoader {
  perusteet: PerusteRecord = {}

  async loadPerusteet(suoritustyypit: string[]): Promise<boolean> {
    const unfetchedSuoritustyypit = suoritustyypit.filter(
      (st) => !this.perusteet[st]
    )
    if (A.isNonEmpty(unfetchedSuoritustyypit)) {
      unfetchedSuoritustyypit.forEach((uri) => {
        this.perusteet[uri] = Loading
      })

      const unfetchedPerusteet = await Promise.all(
        unfetchedSuoritustyypit.map(async (st) => {
          const perusteResponse = await fetchPeruste(st)
          return {
            suoritustyyppi: st,
            perusteRequest: perusteResponse
          }
        })
      )
      unfetchedPerusteet.forEach(({ perusteRequest, suoritustyyppi }) => {
        if (E.isRight(perusteRequest)) {
          // @ts-expect-error TODO: Tyypitys kuntoon, fp-ts käyttöön
          this.perusteet = {
            ...this.perusteet,
            [suoritustyyppi]: perusteRequest.right.data
          }
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
