import { TehostetunTuenPäätös } from './TehostetunTuenPaatos'
import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { Maksuttomuus } from './Maksuttomuus'
import { ErityisenTuenPäätös } from './ErityisenTuenPaatos'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot`
 */
export type PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot'
  tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
  joustavaPerusopetus?: Aikajakso
  pidennettyOppivelvollisuus?: Aikajakso
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  kotiopetusjaksot?: Array<Aikajakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  kotiopetus?: Aikajakso
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  perusopetuksenAloittamistaLykätty?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  koulukoti?: Array<Aikajakso>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  aloittanutEnnenOppivelvollisuutta?: boolean
  erityisenTuenPäätös?: ErityisenTuenPäätös
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: TehostetunTuenPäätös
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
    joustavaPerusopetus?: Aikajakso
    pidennettyOppivelvollisuus?: Aikajakso
    ulkomaanjaksot?: Array<Aikajakso>
    majoitusetu?: Aikajakso
    kotiopetusjaksot?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    kotiopetus?: Aikajakso
    oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    perusopetuksenAloittamistaLykätty?: boolean
    maksuttomuus?: Array<Maksuttomuus>
    koulukoti?: Array<Aikajakso>
    erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
    aloittanutEnnenOppivelvollisuutta?: boolean
    erityisenTuenPäätös?: ErityisenTuenPäätös
    ulkomailla?: Aikajakso
    vammainen?: Array<Aikajakso>
    tehostetunTuenPäätös?: TehostetunTuenPäätös
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    vuosiluokkiinSitoutumatonOpetus?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

export const isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot'
