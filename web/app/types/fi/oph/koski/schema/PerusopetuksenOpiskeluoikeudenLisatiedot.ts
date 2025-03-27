import { TehostetunTuenPäätös } from './TehostetunTuenPaatos'
import { Aikajakso } from './Aikajakso'
import { ErityisenTuenPäätös } from './ErityisenTuenPaatos'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Tukijakso } from './Tukijakso'

/**
 * PerusopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot`
 */
export type PerusopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot'
  tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
  joustavaPerusopetus?: Aikajakso
  pidennettyOppivelvollisuus?: Aikajakso
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  kotiopetusjaksot?: Array<Aikajakso>
  kotiopetus?: Aikajakso
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  perusopetuksenAloittamistaLykätty?: boolean
  koulukoti?: Array<Aikajakso>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  aloittanutEnnenOppivelvollisuutta: boolean
  erityisenTuenPäätös?: ErityisenTuenPäätös
  ulkomailla?: Aikajakso
  toimintaAlueittainOpiskelu?: Array<Aikajakso>
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: TehostetunTuenPäätös
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus: boolean
  tukijaksot?: Array<Tukijakso>
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella?: Array<Aikajakso>
}

export const PerusopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
    joustavaPerusopetus?: Aikajakso
    pidennettyOppivelvollisuus?: Aikajakso
    ulkomaanjaksot?: Array<Aikajakso>
    majoitusetu?: Aikajakso
    kotiopetusjaksot?: Array<Aikajakso>
    kotiopetus?: Aikajakso
    oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    perusopetuksenAloittamistaLykätty?: boolean
    koulukoti?: Array<Aikajakso>
    erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
    aloittanutEnnenOppivelvollisuutta?: boolean
    erityisenTuenPäätös?: ErityisenTuenPäätös
    ulkomailla?: Aikajakso
    toimintaAlueittainOpiskelu?: Array<Aikajakso>
    vammainen?: Array<Aikajakso>
    tehostetunTuenPäätös?: TehostetunTuenPäätös
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    vuosiluokkiinSitoutumatonOpetus?: boolean
    tukijaksot?: Array<Tukijakso>
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella?: Array<Aikajakso>
  } = {}
): PerusopetuksenOpiskeluoikeudenLisätiedot => ({
  aloittanutEnnenOppivelvollisuutta: false,
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot',
  vuosiluokkiinSitoutumatonOpetus: false,
  ...o
})

PerusopetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot' as const

export const isPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot'
