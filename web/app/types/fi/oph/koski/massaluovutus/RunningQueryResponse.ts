import { MassaluovutusQueryParameters } from './MassaluovutusQueryParameters'
import { QueryProgress } from './QueryProgress'

/**
 * RunningQueryResponse
 *
 * @see `fi.oph.koski.massaluovutus.RunningQueryResponse`
 */
export type RunningQueryResponse = {
  $class: 'fi.oph.koski.massaluovutus.RunningQueryResponse'
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  files: Array<string>
  resultsUrl: string
  progress?: QueryProgress
  status: 'running'
  createdAt: string
  startedAt: string
}

export const RunningQueryResponse = (o: {
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  files?: Array<string>
  resultsUrl: string
  progress?: QueryProgress
  status?: 'running'
  createdAt: string
  startedAt: string
}): RunningQueryResponse => ({
  $class: 'fi.oph.koski.massaluovutus.RunningQueryResponse',
  files: [],
  status: 'running',
  ...o
})

RunningQueryResponse.className =
  'fi.oph.koski.massaluovutus.RunningQueryResponse' as const

export const isRunningQueryResponse = (a: any): a is RunningQueryResponse =>
  a?.$class === 'fi.oph.koski.massaluovutus.RunningQueryResponse'
