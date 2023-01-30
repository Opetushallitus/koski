import { Yritys } from './Yritys'
import { OppisopimuksenPurkaminen } from './OppisopimuksenPurkaminen'

/**
 * Oppisopimuksen tiedot
 *
 * @see `fi.oph.koski.schema.Oppisopimus`
 */
export type Oppisopimus = {
  $class: 'fi.oph.koski.schema.Oppisopimus'
  työnantaja: Yritys
  oppisopimuksenPurkaminen?: OppisopimuksenPurkaminen
}

export const Oppisopimus = (o: {
  työnantaja: Yritys
  oppisopimuksenPurkaminen?: OppisopimuksenPurkaminen
}): Oppisopimus => ({ $class: 'fi.oph.koski.schema.Oppisopimus', ...o })

Oppisopimus.className = 'fi.oph.koski.schema.Oppisopimus' as const

export const isOppisopimus = (a: any): a is Oppisopimus =>
  a?.$class === 'fi.oph.koski.schema.Oppisopimus'
