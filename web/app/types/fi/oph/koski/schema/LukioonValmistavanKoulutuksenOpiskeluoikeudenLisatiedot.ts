import { Ulkomaanjakso } from './Ulkomaanjakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { Aikajakso } from './Aikajakso'

/**
 * LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot`
 */
export type LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija: boolean
  pidennettyPäättymispäivä: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = (
  o: {
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    oikeusMaksuttomaanAsuntolapaikkaan?: boolean
    maksuttomuus?: Array<Maksuttomuus>
    ulkomainenVaihtoopiskelija?: boolean
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot => ({
  ulkomainenVaihtoopiskelija: false,
  $class:
    'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot',
  pidennettyPäättymispäivä: false,
  ...o
})

export const isLukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot'
