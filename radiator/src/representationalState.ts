import * as string from 'fp-ts/String'
import {pipe} from 'fp-ts/lib/function'
import * as R from 'fp-ts/Record'
import * as O from 'fp-ts/Option'
import {Host, Service, State} from './state'

export type ServiceStatus = 'ok' | 'degraded' | 'down' | 'lost'

export interface ReprService {
  name: string
  status: ServiceStatus
  message?: string
}

export interface ReprHost {
  status: ServiceStatus
  services: ReprService[]
}

export interface ReprState {
  hosts: Record<string, ReprHost>
  env: string
}

export const fromState = (state: State): ReprState => ({
  hosts: pipe(state.hosts, R.map(fromHost), R.compact),
  env: state.env
})

const fromHost = (host: Host): O.Option<ReprHost> => {
  if (olderThan(3)(host.lastSeen)) return O.none

  const services = pipe(
    host.services,
    R.map(fromService),
    R.collect(string.Ord)((_, s) => s)
  )
  const hasDegradedServices = services.some(s => s.status !== 'ok')
  const isLost = olderThan(1)(host.lastSeen)

  return O.some({
    status: isLost ? 'lost' : hasDegradedServices ? 'degraded' : 'ok',
    services
  })
}

const fromService = (service: Service): ReprService => {
  const hasError = Boolean(service.latestError && youngerThan(15)(service.latestError.timestamp))

  return {
    name: service.name,
    status: !service.operational ? 'down' : hasError ? 'degraded' : 'ok',
    message: service.latestError?.message
  }
}

const youngerThan = (minutes: number) => (obj: Date) =>
  new Date().getTime() - obj.getTime() < 60 * 1000 * minutes

const olderThan = (minutes: number) => (obj: Date) =>
  new Date().getTime() - obj.getTime() > 60 * 1000 * minutes
