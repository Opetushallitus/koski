import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot`
 */
export type AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot'
  tehostetunTuenPäätökset?: Array<Aikajakso>
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  maksuttomuus?: Array<Maksuttomuus>
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: Aikajakso
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    tehostetunTuenPäätökset?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Aikajakso>
    majoitusetu?: Aikajakso
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    ulkomailla?: Aikajakso
    vammainen?: Array<Aikajakso>
    tehostetunTuenPäätös?: Aikajakso
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    vuosiluokkiinSitoutumatonOpetus?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

export const isAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot'
