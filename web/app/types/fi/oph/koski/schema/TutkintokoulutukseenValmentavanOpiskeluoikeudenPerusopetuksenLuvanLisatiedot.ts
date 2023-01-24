import { Ulkomaanjakso } from './Ulkomaanjakso'
import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { TuvaErityisenTuenPäätös } from './TuvaErityisenTuenPaatos'

/**
 * Tutkintokoulutukseen valmentavan opiskeluoikeuden perusopetuksen järjestämisluvan lisätiedot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot'
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    majoitusetu?: Aikajakso
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    koulukoti?: Array<Aikajakso>
    erityisenTuenPäätökset?: Array<TuvaErityisenTuenPäätös>
    vammainen?: Array<Aikajakso>
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export const TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =
  (
    o: {
      ulkomaanjaksot?: Array<Ulkomaanjakso>
      majoitusetu?: Aikajakso
      oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
      kuljetusetu?: Aikajakso
      vaikeastiVammainen?: Array<Aikajakso>
      maksuttomuus?: Array<Maksuttomuus>
      koulukoti?: Array<Aikajakso>
      erityisenTuenPäätökset?: Array<TuvaErityisenTuenPäätös>
      vammainen?: Array<Aikajakso>
      pidennettyPäättymispäivä?: boolean
      sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    } = {}
  ): TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot',
    ...o
  })

TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot.className =
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot' as const

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =>
    a?.$class ===
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot'
