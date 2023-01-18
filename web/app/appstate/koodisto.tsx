import * as A from 'fp-ts/Array'
import * as E from 'fp-ts/Either'
import { pipe } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as string from 'fp-ts/string'
import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import {
  isKoodistokoodiviite,
  Koodistokoodiviite
} from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { GroupedKoodistot } from '../types/fi/oph/koski/typemodel/GroupedKoodistot'
import { koodiviiteConstraints } from '../util/constraints'
import { nonNull } from '../util/fp/arrays'
import { fromEntries, mapObjectValues } from '../util/fp/objects'
import { fetchKoodistot } from '../util/koskiApi'

const Loading = Symbol('loading')

export type KoodistoContextValue = {
  readonly koodistot: KoodistoRecord
  readonly loadKoodistot: (koodistoUris: string[]) => void
}

const KoodistoContext = React.createContext<KoodistoContextValue>({
  koodistot: {},
  loadKoodistot: () => {}
})

export type KoodistoProviderProps = {
  children: React.ReactNode
}

export type KoodistoRecord = {
  [URI in string]: KoodistokoodiviiteKoodistonNimellä<URI>[] | typeof Loading
}

export type KoodistokoodiviiteKoodistonNimellä<T extends string = string> = {
  id: string
  koodistoNimi: string
  koodiviite: Koodistokoodiviite<T>
}

class KoodistoLoader {
  koodistot: KoodistoRecord = {}

  async loadKoodistot(koodistoUris: string[]): Promise<boolean> {
    const unfetchedKoodistoUris = koodistoUris.filter(
      (uri) => !this.koodistot[uri]
    )
    if (A.isNonEmpty(unfetchedKoodistoUris)) {
      unfetchedKoodistoUris.forEach((uri) => {
        this.koodistot[uri] = Loading
      })

      pipe(
        await fetchKoodistot(unfetchedKoodistoUris),
        E.map((response) => {
          const k: KoodistoRecord = pipe(
            Object.entries(response.data.koodistot),
            A.chain(([koodistoNimi, koodiviitteet]) =>
              koodiviitteet.map((koodiviite) => ({
                id: `${koodiviite.koodistoUri}_${koodiviite.koodiarvo}`,
                koodistoNimi,
                koodiviite
              }))
            ),
            NEA.groupBy((k) => k.koodiviite.koodistoUri)
          )

          Object.assign(this.koodistot, k)
        })
      )
      return true
    }
    return false
  }

  findKoodi<T extends string>(
    uri: T,
    koodiarvo: string
  ): Koodistokoodiviite<T> {
    const group = this.koodistot[uri]
    if (group === undefined) {
      throw new Error(
        `Cannot find koodi ${uri}_${koodiarvo} because loading of koodisto ${uri} hasn't been loaded`
      )
    }
    if (group === Loading) {
      throw new Error(
        `Cannot find koodi ${uri}_${koodiarvo} because loading of koodisto ${uri} hasn't finished`
      )
    }
    const viite = group.find((k) => k.koodiviite.koodiarvo === koodiarvo)
    if (!viite) {
      throw new Error(
        `Koodiarvo ${koodiarvo} does not exist in koodisto ${uri}`
      )
    }
    return viite.koodiviite as Koodistokoodiviite<T>
  }
}

const koodistoLoaderSingleton = new KoodistoLoader()

export const KoodistoProvider = (props: KoodistoProviderProps) => {
  const [koodistot, setKoodistot] = useState<KoodistoRecord>({})

  const loadKoodistot = useCallback(async (koodistoUris: string[]) => {
    if (await koodistoLoaderSingleton.loadKoodistot(koodistoUris)) {
      setKoodistot(koodistoLoaderSingleton.koodistot)
    }
  }, [])

  const providedValue: KoodistoContextValue = useMemo(
    () => ({ koodistot, loadKoodistot }),
    [koodistot, loadKoodistot]
  )

  return (
    <KoodistoContext.Provider value={providedValue}>
      {props.children}
    </KoodistoContext.Provider>
  )
}

// Ylimäärittelyt paremmalle tyypitykselle
export function useKoodisto<T extends string>(
  koodistoUri: T
): KoodistokoodiviiteKoodistonNimellä<T>[] | null
export function useKoodisto(
  ...koodistoUris: Array<string | null | undefined>
): KoodistokoodiviiteKoodistonNimellä[] | null
export function useKoodisto(
  ...koodistoUris: Array<string | null | undefined>
): KoodistokoodiviiteKoodistonNimellä[] | null {
  const context = useContext(KoodistoContext)

  useEffect(() => {
    if (A.isNonEmpty(koodistoUris)) {
      context.loadKoodistot(koodistoUris.filter(nonNull))
    }
  }, [koodistoUris])

  return koodistoUris.flatMap((uri) => {
    const k = uri && context.koodistot[uri]
    return Array.isArray(k) ? k : []
  })
}

export const useKoodistoOfConstraint = (
  constraint: Constraint | null
): KoodistokoodiviiteKoodistonNimellä[] | null => {
  const koodiviiteC = koodiviiteConstraints(constraint)
  const koodit = useKoodisto(koodiviiteC?.koodistoUri)
  return (
    koodit?.filter(
      (k) =>
        !koodiviiteC?.koodiarvot ||
        koodiviiteC.koodiarvot.includes(k.koodiviite.koodiarvo)
    ) || null
  )
}

/**
 * KoodistoFiller ottaa sisään minkä tahansa dataobjektin ja täyttää sieltä löytyville
 * nimettömille koodistoviitteille nimet.
 */
export type KoodistoFiller = <T>(a: T) => Promise<T>

const distinct = A.uniq(string.Eq)

export const useKoodistoFiller = (): KoodistoFiller =>
  useCallback(async <T,>(obj: T): Promise<T> => {
    const collectKoodistoUris = (a: any): string[] =>
      Array.isArray(a)
        ? distinct(a.flatMap(collectKoodistoUris))
        : typeof a === 'object'
        ? isKoodistokoodiviite(a)
          ? a.nimi === undefined
            ? [a.koodistoUri]
            : []
          : distinct(Object.values(a).flatMap(collectKoodistoUris))
        : []

    const uris = collectKoodistoUris(obj)
    await koodistoLoaderSingleton.loadKoodistot(uris)

    const populate = <T,>(a: T): T =>
      Array.isArray(a)
        ? (a.map(populate) as T)
        : typeof a === 'object'
        ? isKoodistokoodiviite(a)
          ? a.nimi === undefined
            ? (koodistoLoaderSingleton.findKoodi(
                a.koodistoUri,
                a.koodiarvo
              ) as T)
            : a
          : mapObjectValues(populate)(a)
        : a

    return populate(obj)
  }, [])
