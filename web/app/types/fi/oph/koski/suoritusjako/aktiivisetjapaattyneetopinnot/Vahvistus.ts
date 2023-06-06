/**
 * Vahvistus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Vahvistus`
 */
export type Vahvistus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Vahvistus'
  päivä: string
}

export const Vahvistus = (o: { päivä: string }): Vahvistus => ({
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Vahvistus',
  ...o
})

Vahvistus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Vahvistus' as const

export const isVahvistus = (a: any): a is Vahvistus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Vahvistus'
