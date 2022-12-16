import { Ulkomaanjakso } from './Ulkomaanjakso'
import { Maksuttomuus } from './Maksuttomuus'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'

/**
 * EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot`
 */
export type EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export const EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot = (
  o: {
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    maksuttomuus?: Array<Maksuttomuus>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  } = {}
): EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot',
  ...o
})

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot'
