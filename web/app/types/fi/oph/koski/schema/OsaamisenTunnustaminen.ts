import { Suoritus } from './Suoritus'
import { LocalizedString } from './LocalizedString'

/**
 * Tiedot aiemmin hankitun osaamisen tunnustamisesta.
 *
 * @see `fi.oph.koski.schema.OsaamisenTunnustaminen`
 */
export type OsaamisenTunnustaminen = {
  $class: 'fi.oph.koski.schema.OsaamisenTunnustaminen'
  osaaminen?: Suoritus
  selite: LocalizedString
  rahoituksenPiirissä: boolean
}

export const OsaamisenTunnustaminen = (o: {
  osaaminen?: Suoritus
  selite: LocalizedString
  rahoituksenPiirissä?: boolean
}): OsaamisenTunnustaminen => ({
  $class: 'fi.oph.koski.schema.OsaamisenTunnustaminen',
  rahoituksenPiirissä: false,
  ...o
})

export const isOsaamisenTunnustaminen = (a: any): a is OsaamisenTunnustaminen =>
  a?.$class === 'OsaamisenTunnustaminen'
