import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import * as E from 'fp-ts/Either'
import * as Eq from 'fp-ts/Eq'
import * as string from 'fp-ts/string'
import { constant, pipe } from 'fp-ts/lib/function'
import { StorablePreference } from '../types/fi/oph/koski/schema/StorablePreference'
import {
  fetchPreferences,
  removePreference,
  storePreference
} from '../util/koskiApi'
import { tap } from '../util/fp/either'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import { PropsWithOnlyChildren } from '../util/react'

type OrganisaatioOid = string
type PreferenceType = string
type OrganisaatioPreferences = Record<PreferenceType, StorablePreference[]>

// Koulutustoimijan rajaama lista on eri lista kuin rajaamaton, joten ne
// pidetään välimuistissa erillään.
const listKey = (type: PreferenceType, koulutustoimijaOid?: OrganisaatioOid) =>
  koulutustoimijaOid ? `${type}:${koulutustoimijaOid}` : type

export type PreferencesHook<T extends StorablePreference> = {
  // Lista ladatuista arvoista
  preferences: T[]
  // Tallenna uusi arvo (lisätään backendin puolelle ja preferences-listaan)
  store: (key: string, t: T) => void
  // Tallenna päivitetty arvo vasta tallennuksen yhteydessä
  deferredUpdate: (key: string, t: Partial<T>, original: T) => void
  // Poista olemassaoleva arvo (poistetaan myös backendin puolelta)
  remove: (key: string) => void
}

/**
 * Palauttaa annetun organisaation ja määrätyn tyypin preferencet.
 *
 * Preference service on backendin puolella oleva avain-arvo-säilö, johon tallennetaan usein syötettäviä
 * tietoja, kuten henkilöiden nimiä ja paikallisia osasuoritusten nimiä. Jokaisella organisaatiolla on
 * omat säilönsä. `type`-parametrin ja geneerisen tyypin `T` pitää vastata tiedostossa
 * PreferencesService.scala olevaa määrittelyä.
 *
 * @param organisaatioOid Organisaation oid
 * @param type Preferencen tyyppi, kts. PreferencesService.scala
 * @param koulutustoimijaOid Rajaa arvot koulutustoimijan omiin ja rajaamattomiin. Vanha käyttöliittymä
 * rajaa näin vain myöntäjät, joten muiden tyyppien kanssa tätä ei pidä käyttää.
 * @returns
 */
export const usePreferences = <T extends StorablePreference>(
  organisaatioOid?: OrganisaatioOid,
  type?: PreferenceType,
  koulutustoimijaOid?: OrganisaatioOid
): PreferencesHook<T> => {
  const {
    available,
    load,
    store: storePref,
    deferredUpdate: deferUpdate,
    remove: removePref,
    preferences
  } = useContext(PreferencesContext)

  useEffect(() => {
    if (organisaatioOid && type && available) {
      load(organisaatioOid, type, koulutustoimijaOid)
    }
  }, [available, load, organisaatioOid, type, koulutustoimijaOid])

  const store = useCallback(
    (key: string, data: T) => {
      if (organisaatioOid && type) {
        storePref(organisaatioOid, type, key, data, koulutustoimijaOid)
      } else {
        console.error(
          `Cannot store a preference without organisaatioOid (${organisaatioOid}) and preference type (${type})`
        )
      }
    },
    [organisaatioOid, storePref, type, koulutustoimijaOid]
  )

  const deferredUpdate = useCallback(
    (
      key: string,
      patch: Partial<StorablePreference>,
      original: StorablePreference
    ) => {
      if (organisaatioOid && type) {
        deferUpdate(organisaatioOid, type, key, patch, original)
      }
    },
    [deferUpdate, organisaatioOid, type]
  )

  const remove = useCallback(
    (key: string) => {
      if (organisaatioOid && type) {
        removePref(organisaatioOid, type, key, koulutustoimijaOid)
      } else {
        console.error(
          `Cannot remove a preference without organisaatioOid (${organisaatioOid}) and preference type (${type})`
        )
      }
    },
    [organisaatioOid, removePref, type, koulutustoimijaOid]
  )

  return useMemo(
    () => ({
      preferences: (organisaatioOid && type
        ? preferences[organisaatioOid]?.[listKey(type, koulutustoimijaOid)] ||
          []
        : emptyArray) as T[],
      store,
      deferredUpdate,
      remove
    }),
    [
      organisaatioOid,
      type,
      koulutustoimijaOid,
      preferences,
      store,
      deferredUpdate,
      remove
    ]
  )
}

/**
 * Rakentaa yhdenmukaisen alityyppejä sisältävän tyypityksen nimen. Tällaisen tyypityksen käyttö vaatii
 * PreferencesService.scala-tiedostossa assortedPrefTypes-ominaisuuden käyttöä. Alatyyppien avulla voi
 * muodostaa hierarkian, esim. `assortedPreferenceType('taiteenperusopetus', oppimäärä, taiteenala)`
 *
 * @param group assortedPrefTypes-listassa mainittu nimi
 * @param subtypes vapaavalintainen määrä alatyyppejä
 * @returns Tyypin nimi. Jos yksikin annetuista alatyypeistä on undefined, palautetaan undefined.
 */
export const assortedPreferenceType = (
  group: string,
  ...subtypes: NonEmptyArray<string | undefined>
): string | undefined =>
  subtypes.some((s) => s === undefined)
    ? undefined
    : [group, ...subtypes].join('.')

// Context provider
type DeferredUpdate = {
  organisaatioOid: OrganisaatioOid
  type: PreferenceType
  key: string
  data: StorablePreference
}

class PreferencesLoader {
  preferences: Record<OrganisaatioOid, OrganisaatioPreferences> = {}
  deferred: Record<string, DeferredUpdate> = {}

  async load(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    koulutustoimijaOid?: OrganisaatioOid
  ): Promise<boolean> {
    const key = listKey(type, koulutustoimijaOid)
    if (!this.preferences[organisaatioOid]) {
      this.preferences[organisaatioOid] = {}
    }
    if (!this.preferences[organisaatioOid][key]) {
      this.preferences[organisaatioOid][key] = []
      this.set(
        organisaatioOid,
        key,
        await this.reload(organisaatioOid, type, koulutustoimijaOid)
      )
      return true
    }
    return false
  }

  async store(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    data: StorablePreference,
    koulutustoimijaOid?: OrganisaatioOid
  ): Promise<void> {
    const cacheKey = listKey(type, koulutustoimijaOid)
    if (!this.preferences[organisaatioOid]) {
      this.preferences[organisaatioOid] = {}
    }
    if (!this.preferences[organisaatioOid][cacheKey]) {
      this.preferences[organisaatioOid][cacheKey] = []
    }
    pipe(
      await storePreference(
        organisaatioOid,
        type,
        key,
        data,
        koulutustoimijaOid
      ),
      tap(() => {
        this.set(organisaatioOid, cacheKey, [
          ...this.get(organisaatioOid, cacheKey),
          data
        ])
      })
    )
  }

  async remove(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    koulutustoimijaOid?: OrganisaatioOid
  ): Promise<void> {
    await removePreference(organisaatioOid, type, key, koulutustoimijaOid)
    this.set(
      organisaatioOid,
      listKey(type, koulutustoimijaOid),
      await this.reload(organisaatioOid, type, koulutustoimijaOid)
    )
  }

  deferUpdate(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    patch: Partial<StorablePreference>,
    original: StorablePreference
  ) {
    const fullKey = `${organisaatioOid}_${type}_${key}`
    const base = this.deferred[fullKey]?.data || original
    this.deferred[fullKey] = {
      organisaatioOid,
      type,
      key,
      data: {
        ...base,
        ...patch
      } as StorablePreference
    }
  }

  async storeDeferred() {
    for (const deferred of Object.values(this.deferred)) {
      await this.store(
        deferred.organisaatioOid,
        deferred.type,
        deferred.key,
        deferred.data
      )
    }

    const toReload = pipe(
      Object.values(this.deferred),
      A.uniq(
        Eq.contramap((d: DeferredUpdate) => `${d.organisaatioOid}_${d.type}`)(
          string.Eq
        )
      )
    )

    for (const r of toReload) {
      this.set(
        r.organisaatioOid,
        r.type,
        await this.reload(r.organisaatioOid, r.type)
      )
    }

    this.deferred = {}
  }

  private get(organisaatioOid: string, key: string): StorablePreference[] {
    return this.preferences[organisaatioOid]?.[key] || []
  }

  private set(
    organisaatioOid: string,
    key: string,
    data: StorablePreference[]
  ) {
    this.preferences = {
      ...this.preferences,
      [organisaatioOid]: {
        ...this.preferences[organisaatioOid],
        [key]: data
      }
    }
  }

  private async reload(
    organisaatioOid: string,
    type: string,
    koulutustoimijaOid?: string
  ) {
    return pipe(
      await fetchPreferences(organisaatioOid, type, koulutustoimijaOid),
      E.fold(constant([]), (response) => response.data)
    )
  }
}

const preferencesLoader = new PreferencesLoader()

export type PreferencesContext = {
  available: boolean
  preferences: Record<OrganisaatioOid, OrganisaatioPreferences>
  load: (
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    koulutustoimijaOid?: OrganisaatioOid
  ) => void
  store: (
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    data: StorablePreference,
    koulutustoimijaOid?: OrganisaatioOid
  ) => void
  deferredUpdate: (
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    t: Partial<StorablePreference>,
    original: StorablePreference
  ) => void
  remove: (
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    koulutustoimijaOid?: OrganisaatioOid
  ) => void
}

const providerMissing = () => {
  throw new Error('PreferencesProvider is missing')
}

const initialContextValue: PreferencesContext = {
  available: false,
  preferences: {},
  load: providerMissing,
  store: providerMissing,
  deferredUpdate: providerMissing,
  remove: providerMissing
}

const PreferencesContext = React.createContext(initialContextValue)

export const PreferencesProvider: React.FC<PropsWithOnlyChildren> = (props) => {
  const [preferences, setPreferences] = useState<
    Record<OrganisaatioOid, OrganisaatioPreferences>
  >({})

  const load = useCallback(
    async (
      organisaatioOid: OrganisaatioOid,
      type: PreferenceType,
      koulutustoimijaOid?: OrganisaatioOid
    ) => {
      await preferencesLoader.load(organisaatioOid, type, koulutustoimijaOid)
      setPreferences(preferencesLoader.preferences)
    },
    []
  )

  const store = useCallback(
    async (
      organisaatioOid: OrganisaatioOid,
      type: PreferenceType,
      key: string,
      data: StorablePreference,
      koulutustoimijaOid?: OrganisaatioOid
    ) => {
      await preferencesLoader.store(
        organisaatioOid,
        type,
        key,
        data,
        koulutustoimijaOid
      )
      setPreferences(preferencesLoader.preferences)
    },
    []
  )

  const deferredUpdate = useCallback(
    (
      organisaatioOid: OrganisaatioOid,
      type: PreferenceType,
      key: string,
      patch: Partial<StorablePreference>,
      original: StorablePreference
    ) => {
      preferencesLoader.deferUpdate(organisaatioOid, type, key, patch, original)
    },
    []
  )

  const remove = useCallback(
    async (
      organisaatioOid: OrganisaatioOid,
      type: PreferenceType,
      key: string,
      koulutustoimijaOid?: OrganisaatioOid
    ) => {
      await preferencesLoader.remove(
        organisaatioOid,
        type,
        key,
        koulutustoimijaOid
      )
      setPreferences(preferencesLoader.preferences)
    },
    []
  )

  const contextValue: PreferencesContext = useMemo(
    () => ({
      available: true,
      preferences,
      load,
      store,
      deferredUpdate,
      remove
    }),
    [preferences, load, store, deferredUpdate, remove]
  )

  return (
    <PreferencesContext.Provider value={contextValue}>
      {props.children}
    </PreferencesContext.Provider>
  )
}

const emptyArray: OrganisaatioPreferences[] = []

export const classPreferenceName = (clss: any): string => {
  const name =
    typeof clss === 'string'
      ? clss
      : '$class' in clss
        ? clss.$class
        : 'className' in clss
          ? clss.className
          : `${clss}`
  return pipe(
    name.split('.'),
    A.last,
    O.getOrElse(() => name),
    (s) => s.toLowerCase().replace(/ö/g, 'o').replace(/ä/g, 'a')
  )
}

export const storeDeferredPreferences = async () =>
  preferencesLoader.storeDeferred()
