import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Maksuttomuus } from './Maksuttomuus'

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
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: Aikajakso
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  vaikeastiVammainen?: Array<Aikajakso>
  maksuttomuus?: Array<Maksuttomuus>
}

export const AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    tehostetunTuenPäätökset?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Aikajakso>
    majoitusetu?: Aikajakso
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
    ulkomailla?: Aikajakso
    vammainen?: Array<Aikajakso>
    tehostetunTuenPäätös?: Aikajakso
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    vuosiluokkiinSitoutumatonOpetus?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
  } = {}
): AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot' as const

export const isAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot'
