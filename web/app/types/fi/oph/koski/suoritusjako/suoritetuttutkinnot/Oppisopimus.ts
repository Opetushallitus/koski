import { Yritys } from './Yritys'
import { OppisopimuksenPurkaminen } from './OppisopimuksenPurkaminen'

/**
 * Oppisopimus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Oppisopimus`
 */
export type Oppisopimus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Oppisopimus'
  työnantaja: Yritys
  oppisopimuksenPurkaminen?: OppisopimuksenPurkaminen
}

export const Oppisopimus = (o: {
  työnantaja: Yritys
  oppisopimuksenPurkaminen?: OppisopimuksenPurkaminen
}): Oppisopimus => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Oppisopimus',
  ...o
})

Oppisopimus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Oppisopimus' as const

export const isOppisopimus = (a: any): a is Oppisopimus =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Oppisopimus'
