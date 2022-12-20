/**
 * Maksuttomuus
 *
 * @see `fi.oph.koski.schema.Maksuttomuus`
 */
export type Maksuttomuus = {
  $class: 'fi.oph.koski.schema.Maksuttomuus'
  alku: string
  loppu?: string
  maksuton: boolean
}

export const Maksuttomuus = (o: {
  alku: string
  loppu?: string
  maksuton: boolean
}): Maksuttomuus => ({ $class: 'fi.oph.koski.schema.Maksuttomuus', ...o })

export const isMaksuttomuus = (a: any): a is Maksuttomuus =>
  a?.$class === 'fi.oph.koski.schema.Maksuttomuus'
