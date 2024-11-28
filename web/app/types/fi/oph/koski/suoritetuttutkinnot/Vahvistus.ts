/**
 * Vahvistus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.Vahvistus`
 */
export type Vahvistus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.Vahvistus'
  päivä: string
}

export const Vahvistus = (o: { päivä: string }): Vahvistus => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.Vahvistus',
  ...o
})

Vahvistus.className = 'fi.oph.koski.suoritetuttutkinnot.Vahvistus' as const

export const isVahvistus = (a: any): a is Vahvistus =>
  a?.$class === 'fi.oph.koski.suoritetuttutkinnot.Vahvistus'
