import { ErityisenKoulutustehtävänJakso } from './ErityisenKoulutustehtavanJakso'
import { Ulkomaanjakso } from './Ulkomaanjakso'
import { Maksuttomuus } from './Maksuttomuus'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'

/**
 * InternationalSchoolOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenLisätiedot`
 */
export type InternationalSchoolOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenLisätiedot'
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export const InternationalSchoolOpiskeluoikeudenLisätiedot = (
  o: {
    erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    maksuttomuus?: Array<Maksuttomuus>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  } = {}
): InternationalSchoolOpiskeluoikeudenLisätiedot => ({
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenLisätiedot',
  ...o
})

export const isInternationalSchoolOpiskeluoikeudenLisätiedot = (
  a: any
): a is InternationalSchoolOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeudenLisätiedot'
