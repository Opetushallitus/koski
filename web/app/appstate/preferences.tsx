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
 * @returns
 */
export const usePreferences = <T extends StorablePreference>(
  organisaatioOid?: OrganisaatioOid,
  type?: PreferenceType
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
      load(organisaatioOid, type)
    }
  }, [available, load, organisaatioOid, type])

  const store = useCallback(
    (key: string, data: T) => {
      if (organisaatioOid && type) {
        storePref(organisaatioOid, type, key, data)
      } else {
        console.error(
          `Cannot store a preference without organisaatioOid (${organisaatioOid}) and preference type (${type})`
        )
      }
    },
    [organisaatioOid, storePref, type]
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
        removePref(organisaatioOid, type, key)
      } else {
        console.error(
          `Cannot remove a preference without organisaatioOid (${organisaatioOid}) and preference type (${type})`
        )
      }
    },
    [organisaatioOid, removePref, type]
  )

  return useMemo(
    () => ({
      preferences: (organisaatioOid && type
        ? preferences[organisaatioOid]?.[type] || []
        : emptyArray) as T[],
      store,
      deferredUpdate,
      remove
    }),
    [organisaatioOid, type, preferences, store, deferredUpdate, remove]
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
    type: PreferenceType
  ): Promise<boolean> {
    if (!this.preferences[organisaatioOid]) {
      this.preferences[organisaatioOid] = {}
    }
    if (!this.preferences[organisaatioOid][type]) {
      this.preferences[organisaatioOid][type] = []
      this.set(organisaatioOid, type, await this.reload(organisaatioOid, type))
      return true
    }
    return false
  }

  async store(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    data: StorablePreference
  ): Promise<void> {
    if (!this.preferences[organisaatioOid]) {
      this.preferences[organisaatioOid] = {}
    }
    if (!this.preferences[organisaatioOid][type]) {
      this.preferences[organisaatioOid][type] = []
    }
    pipe(
      await storePreference(organisaatioOid, type, key, data),
      tap(() => {
        this.set(organisaatioOid, type, [
          ...this.get(organisaatioOid, type),
          data
        ])
      })
    )
  }

  async remove(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string
  ): Promise<void> {
    await removePreference(organisaatioOid, type, key)
    this.set(organisaatioOid, type, await this.reload(organisaatioOid, type))
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

  private get(organisaatioOid: string, type: string): StorablePreference[] {
    return this.preferences[organisaatioOid]?.[type] || []
  }

  private set(
    organisaatioOid: string,
    type: string,
    data: StorablePreference[]
  ) {
    this.preferences = {
      ...this.preferences,
      [organisaatioOid]: {
        ...this.preferences[organisaatioOid],
        [type]: data
      }
    }
  }

  private async reload(organisaatioOid: string, type: string) {
    return pipe(
      await fetchPreferences(organisaatioOid, type),
      E.fold(constant([]), (response) => response.data)
    )
  }
}

const preferencesLoader = new PreferencesLoader()

export type PreferencesContext = {
  available: boolean
  preferences: Record<OrganisaatioOid, OrganisaatioPreferences>
  load: (organisaatioOid: OrganisaatioOid, type: PreferenceType) => void
  store: (
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    data: StorablePreference
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
    key: string
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

export const PreferencesProvider: React.FC<PropsWithOnlyChildren> = (
  props
) => {
  const [preferences, setPreferences] = useState<
    Record<OrganisaatioOid, OrganisaatioPreferences>
  >({})

  const load = useCallback(
    async (organisaatioOid: OrganisaatioOid, type: PreferenceType) => {
      await preferencesLoader.load(organisaatioOid, type)
      setPreferences(preferencesLoader.preferences)
    },
    []
  )

  const store = useCallback(
    async (
      organisaatioOid: OrganisaatioOid,
      type: PreferenceType,
      key: string,
      data: StorablePreference
    ) => {
      await preferencesLoader.store(organisaatioOid, type, key, data)
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
      key: string
    ) => {
      await preferencesLoader.remove(organisaatioOid, type, key)
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
