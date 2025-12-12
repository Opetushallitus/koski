import { OsaAikaisuusJakso } from './OsaAikaisuusJakso'
import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Ulkomaanjakso } from './Ulkomaanjakso'
import { Maksuttomuus } from './Maksuttomuus'

/**
 * Tutkintokoulutukseen valmentavan opiskeluoikeuden ammatillisen koulutuksen järjestämisluvan lisätiedot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot'
    osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    vammainenJaAvustaja?: Array<Aikajakso>
    erityinenTuki?: Array<Aikajakso>
    koulutusvienti?: boolean
    vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    majoitus?: Array<Aikajakso>
    vankilaopetuksessa?: Array<Aikajakso>
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export const TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  (
    o: {
      osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
      oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
      vammainenJaAvustaja?: Array<Aikajakso>
      erityinenTuki?: Array<Aikajakso>
      koulutusvienti?: boolean
      vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
      ulkomaanjaksot?: Array<Ulkomaanjakso>
      vaikeastiVammainen?: Array<Aikajakso>
      maksuttomuus?: Array<Maksuttomuus>
      majoitus?: Array<Aikajakso>
      vankilaopetuksessa?: Array<Aikajakso>
      pidennettyPäättymispäivä?: boolean
      sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    } = {}
  ): TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot',
    ...o
  })

TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot.className =
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot' as const

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =>
    a?.$class ===
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot'
