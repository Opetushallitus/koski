import { Aikajakso } from './Aikajakso'
import { OikeuttaMaksuttomuuteenPidennetty } from './OikeuttaMaksuttomuuteenPidennetty'
import { TehostetunTuenPäätös } from './TehostetunTuenPaatos'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Maksuttomuus } from './Maksuttomuus'
import { ErityisenTuenPäätös } from './ErityisenTuenPaatos'

/**
 * PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot`
 */
export type PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot'
  pidennettyOppivelvollisuus?: Aikajakso
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  kotiopetusjaksot?: Array<Aikajakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  kotiopetus?: Aikajakso
  koulukoti?: Array<Aikajakso>
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: TehostetunTuenPäätös
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
  joustavaPerusopetus?: Aikajakso
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  perusopetuksenAloittamistaLykätty?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  aloittanutEnnenOppivelvollisuutta?: boolean
  erityisenTuenPäätös?: ErityisenTuenPäätös
}

export const PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    pidennettyOppivelvollisuus?: Aikajakso
    ulkomaanjaksot?: Array<Aikajakso>
    majoitusetu?: Aikajakso
    kotiopetusjaksot?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    kotiopetus?: Aikajakso
    koulukoti?: Array<Aikajakso>
    ulkomailla?: Aikajakso
    vammainen?: Array<Aikajakso>
    tehostetunTuenPäätös?: TehostetunTuenPäätös
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    vuosiluokkiinSitoutumatonOpetus?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
    joustavaPerusopetus?: Aikajakso
    oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    perusopetuksenAloittamistaLykätty?: boolean
    maksuttomuus?: Array<Maksuttomuus>
    erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
    aloittanutEnnenOppivelvollisuutta?: boolean
    erityisenTuenPäätös?: ErityisenTuenPäätös
  } = {}
): PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot => ({
  $class:
    'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot' as const

export const isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot'
