/**
 * Vahvistus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Vahvistus`
 */
export type Vahvistus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Vahvistus'
  päivä: string
}

export const Vahvistus = (o: { päivä: string }): Vahvistus => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Vahvistus',
  ...o
})

Vahvistus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Vahvistus' as const

export const isVahvistus = (a: any): a is Vahvistus =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Vahvistus'
