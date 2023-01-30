/**
 * Limit
 *
 * @see `fi.oph.koski.typemodel.Limit`
 */
export type Limit = {
  $class: 'fi.oph.koski.typemodel.Limit'
  n: number
  inclusive: boolean
}

export const Limit = (o: { n: number; inclusive: boolean }): Limit => ({
  $class: 'fi.oph.koski.typemodel.Limit',
  ...o
})

Limit.className = 'fi.oph.koski.typemodel.Limit' as const

export const isLimit = (a: any): a is Limit =>
  a?.$class === 'fi.oph.koski.typemodel.Limit'
