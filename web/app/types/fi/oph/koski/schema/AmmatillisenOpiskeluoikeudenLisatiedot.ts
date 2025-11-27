import { OsaAikaisuusJakso } from './OsaAikaisuusJakso'
import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Ulkomaanjakso } from './Ulkomaanjakso'
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
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  vammainenJaAvustaja?: Array<Aikajakso>
  siirtynytUusiinTutkinnonPerusteisiin?: boolean
  erityinenTuki?: Array<Aikajakso>
  jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero', string>
  vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  vaikeastiVammainen?: Array<Aikajakso>
  maksuttomuus?: Array<Maksuttomuus>
  majoitus?: Array<Aikajakso>
  vankilaopetuksessa?: Array<Aikajakso>
  henkilöstökoulutus: boolean
  koulutusvienti: boolean
  opiskeluvalmiuksiaTukevatOpinnot?: Array<OpiskeluvalmiuksiaTukevienOpintojenJakso>
  hojks?: Hojks
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const AmmatillisenOpiskeluoikeudenLisätiedot = (
  o: {
    osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    oikeusMaksuttomaanAsuntolapaikkaan?: boolean
    vammainenJaAvustaja?: Array<Aikajakso>
    siirtynytUusiinTutkinnonPerusteisiin?: boolean
    erityinenTuki?: Array<Aikajakso>
    jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero', string>
    vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    majoitus?: Array<Aikajakso>
    vankilaopetuksessa?: Array<Aikajakso>
    henkilöstökoulutus?: boolean
    koulutusvienti?: boolean
    opiskeluvalmiuksiaTukevatOpinnot?: Array<OpiskeluvalmiuksiaTukevienOpintojenJakso>
    hojks?: Hojks
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): AmmatillisenOpiskeluoikeudenLisätiedot => ({
  $class: 'fi.oph.koski.schema.AmmatillisenOpiskeluoikeudenLisätiedot',
  henkilöstökoulutus: false,
  koulutusvienti: false,
  ...o
})

AmmatillisenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.AmmatillisenOpiskeluoikeudenLisätiedot' as const

export const isAmmatillisenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AmmatillisenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.AmmatillisenOpiskeluoikeudenLisätiedot'
