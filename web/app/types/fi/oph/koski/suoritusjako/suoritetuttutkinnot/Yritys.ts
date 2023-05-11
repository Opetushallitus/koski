import { LocalizedString } from '../../schema/LocalizedString'

/**
 * Yritys
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Yritys`
 */
export type Yritys = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Yritys'
  nimi: LocalizedString
  yTunnus: string
}

export const Yritys = (o: {
  nimi: LocalizedString
  yTunnus: string
}): Yritys => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Yritys',
  ...o
})

Yritys.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Yritys' as const

export const isYritys = (a: any): a is Yritys =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Yritys'
