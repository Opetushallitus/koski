import { OsaAikaisuusJakso } from './OsaAikaisuusJakso'
import { Aikajakso } from './Aikajakso'
import { Ulkomaanjakso } from './Ulkomaanjakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
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
    vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    vammainenJaAvustaja?: Array<Aikajakso>
    majoitus?: Array<Aikajakso>
    vankilaopetuksessa?: Array<Aikajakso>
    erityinenTuki?: Array<Aikajakso>
    koulutusvienti?: boolean
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export const TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  (
    o: {
      osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
      vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
      ulkomaanjaksot?: Array<Ulkomaanjakso>
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
      oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
      vaikeastiVammainen?: Array<Aikajakso>
      maksuttomuus?: Array<Maksuttomuus>
      vammainenJaAvustaja?: Array<Aikajakso>
      majoitus?: Array<Aikajakso>
      vankilaopetuksessa?: Array<Aikajakso>
      erityinenTuki?: Array<Aikajakso>
      koulutusvienti?: boolean
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
