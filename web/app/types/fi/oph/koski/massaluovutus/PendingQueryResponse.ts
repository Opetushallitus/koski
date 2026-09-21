import { MassaluovutusQueryParameters } from './MassaluovutusQueryParameters'

/**
 * Massaluovutuskysely on luotu, mutta sen käsittelyä ei ole vielä aloitettu.
 *
 * @see `fi.oph.koski.massaluovutus.PendingQueryResponse`
 */
export type PendingQueryResponse = {
  $class: 'fi.oph.koski.massaluovutus.PendingQueryResponse'
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  resultsUrl: string
  status: 'pending'
  createdAt: string
}

export const PendingQueryResponse = (o: {
  queryId: string
  query: MassaluovutusQueryParameters
  requestedBy: string
  resultsUrl: string
  status?: 'pending'
  createdAt: string
}): PendingQueryResponse => ({
  $class: 'fi.oph.koski.massaluovutus.PendingQueryResponse',
  status: 'pending',
  ...o
})

PendingQueryResponse.className =
  'fi.oph.koski.massaluovutus.PendingQueryResponse' as const

export const isPendingQueryResponse = (a: any): a is PendingQueryResponse =>
  a?.$class === 'fi.oph.koski.massaluovutus.PendingQueryResponse'
