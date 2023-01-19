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
import { fetchPreferences, storePreference } from '../util/koskiApi'
import { tap } from '../util/fp/either'

type OrganisaatioOid = string
type PreferenceType = string
type OrganisaatioPreferences = Record<PreferenceType, StorablePreference[]>

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
      this.preferences[organisaatioOid][type] = pipe(
        await fetchPreferences(organisaatioOid, type),
        E.fold(constant([]), (response) => response.data)
      )
      return true
    }
    return false
  }

  async store(
    organisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    data: StorablePreference
  ): Promise<void> {
    if (!this.preferences[organisaatioOid]) {
      this.preferences[organisaatioOid] = {}
    }
    if (!this.preferences[organisaatioOid][type]) {
      this.preferences[organisaatioOid][type] = []
    }
    pipe(
      await storePreference(organisaatioOid, type, data),
      tap(() => {
        this.preferences[organisaatioOid][type].push(data)
      })
    )
  }
}

const preferencesLoader = new PreferencesLoader()

export type PreferencesContext = {
  preferences: Record<OrganisaatioOid, OrganisaatioPreferences>
  load: (organisaatioOid: OrganisaatioOid, type: PreferenceType) => void
  store: (
    OrganisaatioOid: OrganisaatioOid,
    type: PreferenceType,
    data: StorablePreference
  ) => void
}

const providerMissing = () => {
  throw new Error('PreferencesProvider is missing')
}

const initialContextValue: PreferencesContext = {
  preferences: {},
  load: providerMissing,
  store: providerMissing
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
      data: StorablePreference
    ) => {
      await preferencesLoader.store(organisaatioOid, type, data)
      setPreferences(preferencesLoader.preferences)
    },
    []
  )

  const contextValue: PreferencesContext = useMemo(
    () => ({ preferences, load, store }),
    [preferences, load, store]
  )

  return (
    <PreferencesContext.Provider value={contextValue}>
      {props.children}
    </PreferencesContext.Provider>
  )
}

export const usePreferences = <T extends StorablePreference>(
  organisaatioOid?: OrganisaatioOid,
  type?: PreferenceType
): [T[], (data: T) => void] => {
  const context = useContext(PreferencesContext)

  useEffect(() => {
    if (organisaatioOid && type) {
      context.load(organisaatioOid, type)
    }
  }, [organisaatioOid, type])

  const store = useCallback((data: T) => {
    if (organisaatioOid && type) {
      context.store(organisaatioOid, type, data)
    } else {
      console.error(
        `Cannot store preference without organisaatioOid (${organisaatioOid}) and preference type (${type})`
      )
    }
  }, [])

  return [
    (organisaatioOid && type
      ? context.preferences[organisaatioOid]?.[type] || []
      : []) as T[],
    store
  ]
}
