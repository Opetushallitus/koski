import { OsaAikaisuusJakso } from './OsaAikaisuusJakso'
import { Aikajakso } from './Aikajakso'
import { Ulkomaanjakso } from './Ulkomaanjakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { OpiskeluvalmiuksiaTukevienOpintojenJakso } from './OpiskeluvalmiuksiaTukevienOpintojenJakso'
import { Hojks } from './Hojks'

/**
 * Ammatillisen opiskeluoikeuden lisätiedot (mm. rahoituksessa käytettävät)
 *
 * @see `fi.oph.koski.schema.AmmatillisenOpiskeluoikeudenLisätiedot`
 */
export type AmmatillisenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.AmmatillisenOpiskeluoikeudenLisätiedot'
  osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
  vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  vaikeastiVammainen?: Array<Aikajakso>
  maksuttomuus?: Array<Maksuttomuus>
  vammainenJaAvustaja?: Array<Aikajakso>
  majoitus?: Array<Aikajakso>
  vankilaopetuksessa?: Array<Aikajakso>
  henkilöstökoulutus: boolean
  erityinenTuki?: Array<Aikajakso>
  koulutusvienti: boolean
  opiskeluvalmiuksiaTukevatOpinnot?: Array<OpiskeluvalmiuksiaTukevienOpintojenJakso>
  hojks?: Hojks
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const AmmatillisenOpiskeluoikeudenLisätiedot = (
  o: {
    osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
    vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    oikeusMaksuttomaanAsuntolapaikkaan?: boolean
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    vammainenJaAvustaja?: Array<Aikajakso>
    majoitus?: Array<Aikajakso>
    vankilaopetuksessa?: Array<Aikajakso>
    henkilöstökoulutus?: boolean
    erityinenTuki?: Array<Aikajakso>
    koulutusvienti?: boolean
    opiskeluvalmiuksiaTukevatOpinnot?: Array<OpiskeluvalmiuksiaTukevienOpintojenJakso>
    hojks?: Hojks
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): AmmatillisenOpiskeluoikeudenLisätiedot => ({
  henkilöstökoulutus: false,
  koulutusvienti: false,
  $class: 'fi.oph.koski.schema.AmmatillisenOpiskeluoikeudenLisätiedot',
  ...o
})

export const isAmmatillisenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AmmatillisenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'AmmatillisenOpiskeluoikeudenLisätiedot'