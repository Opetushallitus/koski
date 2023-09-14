import { useCallback, useState } from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'

const ROOT_LEVEL_PREFIX = 'level_0_'
const ROOT_LEVEL = 0

export type OsasuorituksetExpandedState = Record<string, boolean>

const constructOsasuorituksetOpenState = (
  prevState: Record<string, boolean>,
  level: number,
  suoritusIndex: number,
  osasuoritukset: any[]
): Record<string, boolean> => {
  const newState = { ...prevState }
  osasuoritukset.map((_os, i) => {
    const key = `level_${level}_suoritus_${suoritusIndex}_osasuoritus_${i}`
    const existing = newState[key]
    if (existing === undefined) {
      newState[key] = false
    } else {
      newState[key] = !existing
    }
  })
  return newState
}

export const useOsasuorituksetExpand = <T extends Opiskeluoikeus>(
  päätasonSuoritus: ActivePäätasonSuoritus<T>
) => {
  const [osasuorituksetOpenState, setOsasuorituksetOpenState] =
    useState<OsasuorituksetExpandedState>({})

  const rootLevelOsasuoritusOpen = Object.entries(osasuorituksetOpenState)
    .filter(([k, _v]) => k.indexOf(ROOT_LEVEL_PREFIX) === 0)
    .some(([_key, val]) => val === true)

  /**
   * Avaa kaikki ylimmän tason osasuoritukset
   */
  const openAllOsasuoritukset = useCallback(() => {
    const os =
      'osasuoritukset' in päätasonSuoritus.suoritus
        ? päätasonSuoritus.suoritus.osasuoritukset || []
        : []
    setOsasuorituksetOpenState((_oldState) => {
      const expandedState = constructOsasuorituksetOpenState(
        {},
        ROOT_LEVEL,
        päätasonSuoritus.index,
        os
      )
      const newExpandedState = Object.entries(expandedState).reduce(
        (prev, [key, _val]) => {
          return { ...prev, [key]: true }
        },
        expandedState
      )
      return newExpandedState
    })
  }, [päätasonSuoritus.index, päätasonSuoritus.suoritus])

  const closeAllOsasuoritukset = useCallback(() => {
    setOsasuorituksetOpenState({})
  }, [])

  const setOsasuorituksetStateHandler = useCallback(
    (key: string, expanded: boolean) => {
      setOsasuorituksetOpenState((oldState) => ({
        ...oldState,
        [key]: expanded
      }))
    },
    []
  )

  return {
    osasuorituksetOpenState,
    rootLevelOsasuoritusOpen,
    openAllOsasuoritukset,
    closeAllOsasuoritukset,
    setOsasuorituksetStateHandler
  }
}

type UseOsasuorituksetExpand = ReturnType<typeof useOsasuorituksetExpand>
export type SetOsasuoritusOpen =
  UseOsasuorituksetExpand['setOsasuorituksetStateHandler']
export type SetOsasuorituksetStateHandler =
  UseOsasuorituksetExpand['setOsasuorituksetStateHandler']

export type OpenAllOsasuorituksetHandler =
  UseOsasuorituksetExpand['openAllOsasuoritukset']
export type CloseOsasuorituksetHandler =
  UseOsasuorituksetExpand['closeAllOsasuoritukset']
