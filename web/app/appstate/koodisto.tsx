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
import * as C from '../util/constraints'
import { nonNull } from '../util/fp/arrays'
import { mapObjectValues } from '../util/fp/objects'
import { fetchKoodistot } from '../util/koskiApi'
import * as Ord from 'fp-ts/Ord'
import { t } from '../i18n/i18n'

/**
 * Palauttaa annetun koodiston koodiarvot. Jos koodiarvot-argumentti on annettu,
 * palautetaan vain siinä mainitut koodiarvot.
 *
 * @returns KoodistokoodiviiteKoodistonNimellä[] kun koodisto on saatu ladattua, null jos koodistoa vielä ladataan tai tapahtui virhe
 */
export function useKoodisto<T extends string>(
  koodistoUri?: T | null,
  koodiarvot?: string[] | null
): KoodistokoodiviiteKoodistonNimellä<T>[] | null {
  const koodit = useKoodistot<T>(koodistoUri)

  return useMemo(
    () =>
      koodit &&
      koodit.filter((koodi) =>
        koodiarvot === undefined
          ? true
          : koodiarvot === null
          ? false
          : koodiarvot.includes(koodi.koodiviite.koodiarvo)
      ),
    [koodit, koodiarvot]
  )
}

/**
 * Palauttaa yhden tai useamman koodiston koodiarvot.
 *
 * @returns KoodistokoodiviiteKoodistonNimellä[] kun yksikin koodisto on saatu ladattua, muuten null.
 */
export const useKoodistot = <T extends string>(
  ...koodistoUris: Array<string | null | undefined>
) => {
  const { koodistot, loadKoodistot } = useContext(KoodistoContext)

  useEffect(() => {
    loadKoodistot(koodistoUris.filter(nonNull))
  }, [koodistoUris, loadKoodistot])

  return useMemo(() => {
    const k = koodistoUris
      .filter(nonNull)
      .flatMap(
        (uri) =>
          (Array.isArray(koodistot[uri])
            ? koodistot[uri]
            : []) as KoodistokoodiviiteKoodistonNimellä<T>[]
      )
    return A.isNonEmpty(k) ? k : null
  }, [koodistoUris, koodistot])
}

/**
 * Palauttaa vastaavaa constraintia vastaavan koodiston.
 *
 * Heittää poikkeuksen, jos
 *    - annettu constraint ei viittaa koodistokoodiviitteeseen
 *    - tai constraint viittaa useampaan kuin yhteen koodistoon (TODO: lisää tälle tuki, jos tarvitset sitä).
 *
 * @returns KoodistokoodiviiteKoodistonNimellä[] kun koodisto on saatu ladattua, null jos koodistoa vielä ladataan tai tapahtui virhe
 */
export const useKoodistoOfConstraint = <T extends string = string>(
  constraint: Constraint | null
): KoodistokoodiviiteKoodistonNimellä<T>[] | null => {
  const koodistoSchemas = useMemo(
    () => C.koodiviite<T>(constraint)?.filter(nonNull) || [],
    [constraint]
  )
  const koodistot = useKoodistot<T>(
    ...koodistoSchemas.map((k) => k.koodistoUri)
  )
  return (
    koodistot?.filter((k) =>
      koodistoSchemas.find(
        (s) =>
          s.koodistoUri === k.koodiviite.koodistoUri &&
          (s.koodiarvot === null ||
            A.isEmpty(s.koodiarvot) ||
            s.koodiarvot.includes(k.koodiviite.koodiarvo))
      )
    ) || null
  )
}

/**
 * Palauttaa funktion, joka täyttää sille annettuun mihin tahansa muuttujaan siitä puuttuvat koodistokoodiviitteiden nimet.
 */
export const useKoodistoFiller = (): (<T>(a: T) => Promise<T>) =>
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

    const populate = <A,>(a: A): A =>
      Array.isArray(a)
        ? (a.map(populate) as A)
        : typeof a === 'object'
        ? isKoodistokoodiviite(a)
          ? a.nimi === undefined
            ? (koodistoLoaderSingleton.findKoodi(
                a.koodistoUri,
                a.koodiarvo
              ) as A)
            : a
          : mapObjectValues(populate)(a)
        : a

    return populate(obj)
  }, [])

// Context provider

const Loading = Symbol('loading')

export type KoodistoContextValue = {
  readonly koodistot: KoodistoRecord
  readonly loadKoodistot: (koodistoUris: string[]) => void
}

const KoodistoContext = React.createContext<KoodistoContextValue>({
  koodistot: {},
  loadKoodistot: () => {}
})

export type KoodistoProviderProps = React.PropsWithChildren

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
            NEA.groupBy((koodi) => koodi.koodiviite.koodistoUri)
          )

          this.koodistot = { ...this.koodistot, ...k }
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

const distinct = A.uniq(string.Eq)

export const KoodistokoodiviiteKoodistonNimelläOrd = Ord.contramap(
  (k: KoodistokoodiviiteKoodistonNimellä) =>
    analyzeItem(t(k.koodiviite.nimi) || '')
)(string.Ord)

const analyzeItem = (value: string): string =>
  value
    .split(' ')
    .map((x) => {
      const n = parseFloat(x.replace(',', '.'))
      return Number.isFinite(n) ? x.padStart(16, '0') : x
    })
    .join(' ')
