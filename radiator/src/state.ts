import {pipe} from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as O from 'fp-ts/Option'
import * as R from 'fp-ts/Record'
import * as string from 'fp-ts/String'
import {HealthDataEntry} from './health/HealthSource'

export interface ServiceError {
  timestamp: Date
  message?: string
}

export interface Service {
  lastSeen: Date
  name: string
  operational: boolean
  latestError?: ServiceError
}

export interface Host {
  lastSeen: Date
  services: Record<string, Service>
}

export interface State {
  timestamp: Date
  env: string
  hosts: Record<string, Host>
}

const state: State = {
  timestamp: new Date(),
  env: '',
  hosts: {}
}

export const getState = (env: string): Readonly<State> => {
  state.env = env
  return state
}

export const updateWithHealthData = (env: string, entries: HealthDataEntry[]) => {
  state.hosts = pipe(
    hostRecordFromEntries(entries),
    R.reduceWithIndex(string.Ord)(state.hosts, (key, acc, host) => ({
      ...acc,
      [key]: mergeHosts(acc[key], host)
    }))
  )
}

const mergeHosts = (base: Host | undefined, top: Host): Host => ({
  lastSeen: top.lastSeen,
  services: base ? {...base.services, ...top.services} : top.services
})

const hostRecordFromEntries = (entries: HealthDataEntry[]): Record<string, Host> =>
  pipe(
    NEA.fromArray(entries),
    O.map(NEA.groupBy(e => e.instance)),
    O.map(R.map(hostFromHealthData)),
    O.getOrElse(() => ({}))
  )

const hostFromHealthData = (entries: HealthDataEntry[]): Host => ({
  lastSeen: new Date(),
  services: pipe(
    entries.map(healthDataEntryToService),
    NEA.groupBy(e => e.name),
    R.map(NEA.head)
  )
})

const healthDataEntryToService = (entry: HealthDataEntry): Service => ({
  lastSeen: new Date(), // TODO: @timestampista tää
  name: entry.subsystem,
  operational: entry.operational,
  latestError: entry.message ? {message: entry.message, timestamp: new Date()} : undefined
})
