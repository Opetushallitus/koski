import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import * as E from 'fp-ts/Either'
import { constant, pipe } from 'fp-ts/lib/function'
import { StorablePreference } from '../types/fi/oph/koski/schema/StorablePreference'
import {
  fetchPreferences,
  removePreference,
  storePreference
} from '../util/koskiApi'
import { tap } from '../util/fp/either'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'

type OrganisaatioOid = string
type PreferenceType = string
type OrganisaatioPreferences = Record<PreferenceType, StorablePreference[]>

export type PreferencesHook<T extends StorablePreference> = {
  // Lista ladatuista arvoista
  preferences: T[]
  // Tallenna uusi arvo (lisätään backendin puolelle ja preferences-listaan)
  store: (key: string, t: T) => void
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
    load,
    store: storePref,
    remove: removePref,
    preferences
  } = useContext(PreferencesContext)

  useEffect(() => {
    if (organisaatioOid && type) {
      load(organisaatioOid, type)
    }
  }, [load, organisaatioOid, type])

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
      remove
    }),
    [organisaatioOid, type, preferences, store, remove]
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

class PreferencesLoader {
  preferences: Record<OrganisaatioOid, OrganisaatioPreferences> = {}

  async load(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType
  ): Promise<boolean> {
    if (!this.preferences[organisaatioOid]) {
      this.preferences[organisaatioOid] = {}
    }
    if (!this.preferences[organisaatioOid][type]) {
      this.preferences[organisaatioOid][type] = []
      this.set(
        organisaatioOid,
        type,
        pipe(
          await fetchPreferences(organisaatioOid, type),
          E.fold(constant([]), (response) => response.data)
        )
      )
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
    this.set(
      organisaatioOid,
      type,
      pipe(
        await fetchPreferences(organisaatioOid, type),
        E.fold(constant([]), (response) => response.data)
      )
    )
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
}

const preferencesLoader = new PreferencesLoader()

export type PreferencesContext = {
  preferences: Record<OrganisaatioOid, OrganisaatioPreferences>
  load: (organisaatioOid: OrganisaatioOid, type: PreferenceType) => void
  store: (
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    key: string,
    data: StorablePreference
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
  preferences: {},
  load: providerMissing,
  store: providerMissing,
  remove: providerMissing
}

const PreferencesContext = React.createContext(initialContextValue)

export const PreferencesProvider: React.FC<React.PropsWithChildren> = (
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
    () => ({ preferences, load, store, remove }),
    [preferences, load, store, remove]
  )

  return (
    <PreferencesContext.Provider value={contextValue}>
      {props.children}
    </PreferencesContext.Provider>
  )
}

const emptyArray: OrganisaatioPreferences[] = []
