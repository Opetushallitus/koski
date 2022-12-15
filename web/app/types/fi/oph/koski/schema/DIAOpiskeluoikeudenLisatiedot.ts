import { Ulkomaanjakso } from './Ulkomaanjakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { ErityisenKoulutustehtävänJakso } from './ErityisenKoulutustehtavanJakso'

/**
 * DIA-opiskeluoikeuden lisätiedot
 *
 * @see `fi.oph.koski.schema.DIAOpiskeluoikeudenLisätiedot`
 */
export type DIAOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija: boolean
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  pidennettyPäättymispäivä: boolean
}

export const DIAOpiskeluoikeudenLisätiedot = (
  o: {
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    maksuttomuus?: Array<Maksuttomuus>
    ulkomainenVaihtoopiskelija?: boolean
    erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
    pidennettyPäättymispäivä?: boolean
  } = {}
): DIAOpiskeluoikeudenLisätiedot => ({
  ulkomainenVaihtoopiskelija: false,
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeudenLisätiedot',
  pidennettyPäättymispäivä: false,
  ...o
})

export const isDIAOpiskeluoikeudenLisätiedot = (
  a: any
): a is DIAOpiskeluoikeudenLisätiedot =>
  a?.$class === 'DIAOpiskeluoikeudenLisätiedot'
