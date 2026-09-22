/**
 * QueryProgress
 *
 * @see `fi.oph.koski.massaluovutus.QueryProgress`
 */
export type QueryProgress = {
  $class: 'fi.oph.koski.massaluovutus.QueryProgress'
  percentage: number
  estimatedCompletionTime?: string
}

export const QueryProgress = (o: {
  percentage: number
  estimatedCompletionTime?: string
}): QueryProgress => ({
  $class: 'fi.oph.koski.massaluovutus.QueryProgress',
  ...o
})

QueryProgress.className = 'fi.oph.koski.massaluovutus.QueryProgress' as const

export const isQueryProgress = (a: any): a is QueryProgress =>
  a?.$class === 'fi.oph.koski.massaluovutus.QueryProgress'
