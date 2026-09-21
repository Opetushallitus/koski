import { MassaluovutusQueryParameters } from './MassaluovutusQueryParameters'

/**
 * FailedQueryResponse
 *
 * @see `fi.oph.koski.massaluovutus.FailedQueryResponse`
 */
export type FailedQueryResponse = {
  $class: 'fi.oph.koski.massaluovutus.FailedQueryResponse'
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  files: Array<string>
  error?: string
  status: 'failed'
  finishedAt: string
  createdAt: string
  startedAt: string
  hint?: string
}

export const FailedQueryResponse = (o: {
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  files?: Array<string>
  error?: string
  status?: 'failed'
  finishedAt: string
  createdAt: string
  startedAt: string
  hint?: string
}): FailedQueryResponse => ({
  files: [],
  status: 'failed',
  $class: 'fi.oph.koski.massaluovutus.FailedQueryResponse',
  ...o
})

FailedQueryResponse.className =
  'fi.oph.koski.massaluovutus.FailedQueryResponse' as const

export const isFailedQueryResponse = (a: any): a is FailedQueryResponse =>
  a?.$class === 'fi.oph.koski.massaluovutus.FailedQueryResponse'
