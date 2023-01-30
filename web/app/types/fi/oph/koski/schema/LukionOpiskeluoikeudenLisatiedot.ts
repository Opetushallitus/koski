import { LocalizedString } from './LocalizedString'
import { Ulkomaanjakso } from './Ulkomaanjakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { ErityisenKoulutustehtävänJakso } from './ErityisenKoulutustehtavanJakso'
import { Aikajakso } from './Aikajakso'

/**
 * Lukion opiskeluoikeuden lisätiedot
 *
 * @see `fi.oph.koski.schema.LukionOpiskeluoikeudenLisätiedot`
 */
export type LukionOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeudenLisätiedot'
  alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy?: LocalizedString
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija: boolean
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  yksityisopiskelija?: boolean
  pidennettyPäättymispäivä: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const LukionOpiskeluoikeudenLisätiedot = (
  o: {
    alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy?: LocalizedString
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    oikeusMaksuttomaanAsuntolapaikkaan?: boolean
    maksuttomuus?: Array<Maksuttomuus>
    ulkomainenVaihtoopiskelija?: boolean
    erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
    yksityisopiskelija?: boolean
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): LukionOpiskeluoikeudenLisätiedot => ({
  ulkomainenVaihtoopiskelija: false,
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeudenLisätiedot',
  pidennettyPäättymispäivä: false,
  ...o
})

LukionOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.LukionOpiskeluoikeudenLisätiedot' as const

export const isLukionOpiskeluoikeudenLisätiedot = (
  a: any
): a is LukionOpiskeluoikeudenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.LukionOpiskeluoikeudenLisätiedot'
