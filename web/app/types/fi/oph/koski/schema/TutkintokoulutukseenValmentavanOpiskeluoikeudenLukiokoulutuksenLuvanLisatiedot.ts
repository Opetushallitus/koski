import { Ulkomaanjakso } from './Ulkomaanjakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { Aikajakso } from './Aikajakso'

/**
 * Tutkintokoulutukseen valmentavan opiskeluoikeuden lukiokoulutuksen järjestämisluvan lisätiedot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot'
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    maksuttomuus?: Array<Maksuttomuus>
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export const TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =
  (
    o: {
      ulkomaanjaksot?: Array<Ulkomaanjakso>
      oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
      maksuttomuus?: Array<Maksuttomuus>
      pidennettyPäättymispäivä?: boolean
      sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    } = {}
  ): TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot',
    ...o
  })

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =>
    a?.$class ===
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot'
