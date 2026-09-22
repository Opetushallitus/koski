import { MassaluovutusQueryParameters } from './MassaluovutusQueryParameters'

/**
 * CompleteQueryResponse
 *
 * @see `fi.oph.koski.massaluovutus.CompleteQueryResponse`
 */
export type CompleteQueryResponse = {
  $class: 'fi.oph.koski.massaluovutus.CompleteQueryResponse'
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  files: Array<string>
  status: 'complete'
  finishedAt: string
  sourceDataUpdatedAt?: string
  password?: string
  createdAt: string
  startedAt: string
}

export const CompleteQueryResponse = (o: {
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  files?: Array<string>
  status?: 'complete'
  finishedAt: string
  sourceDataUpdatedAt?: string
  password?: string
  createdAt: string
  startedAt: string
}): CompleteQueryResponse => ({
  files: [],
  status: 'complete',
  $class: 'fi.oph.koski.massaluovutus.CompleteQueryResponse',
  ...o
})

CompleteQueryResponse.className =
  'fi.oph.koski.massaluovutus.CompleteQueryResponse' as const

export const isCompleteQueryResponse = (a: any): a is CompleteQueryResponse =>
  a?.$class === 'fi.oph.koski.massaluovutus.CompleteQueryResponse'
