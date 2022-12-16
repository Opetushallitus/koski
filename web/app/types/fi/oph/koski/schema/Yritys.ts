import { LocalizedString } from './LocalizedString'

/**
 * Yritys, jolla on y-tunnus
 *
 * @see `fi.oph.koski.schema.Yritys`
 */
export type Yritys = {
  $class: 'fi.oph.koski.schema.Yritys'
  nimi: LocalizedString
  yTunnus: string
}

export const Yritys = (o: {
  nimi: LocalizedString
  yTunnus: string
}): Yritys => ({ $class: 'fi.oph.koski.schema.Yritys', ...o })

export const isYritys = (a: any): a is Yritys => a?.$class === 'Yritys'
