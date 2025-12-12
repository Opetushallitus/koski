import { Aikajakso } from './Aikajakso'
import { TehostetunTuenPäätös } from './TehostetunTuenPaatos'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { ErityisenTuenPäätös } from './ErityisenTuenPaatos'
import { Tukijakso } from './Tukijakso'

/**
 * PerusopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot`
 */
export type PerusopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot'
  pidennettyOppivelvollisuus?: Aikajakso
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  kotiopetusjaksot?: Array<Aikajakso>
  kotiopetus?: Aikajakso
  koulukoti?: Array<Aikajakso>
  tehostetunTuenPäätös?: TehostetunTuenPäätös
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella?: Array<Aikajakso>
  tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
  joustavaPerusopetus?: Aikajakso
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  perusopetuksenAloittamistaLykätty?: boolean
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  aloittanutEnnenOppivelvollisuutta: boolean
  erityisenTuenPäätös?: ErityisenTuenPäätös
  tavoitekokonaisuuksittainOpiskelu?: Array<Aikajakso>
  ulkomailla?: Aikajakso
  toimintaAlueittainOpiskelu?: Array<Aikajakso>
  tuenPäätöksenJaksot?: Array<Tukijakso>
  vammainen?: Array<Aikajakso>
}

export const PerusopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    pidennettyOppivelvollisuus?: Aikajakso
    ulkomaanjaksot?: Array<Aikajakso>
    majoitusetu?: Aikajakso
    kotiopetusjaksot?: Array<Aikajakso>
    kotiopetus?: Aikajakso
    koulukoti?: Array<Aikajakso>
    tehostetunTuenPäätös?: TehostetunTuenPäätös
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    vuosiluokkiinSitoutumatonOpetus?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
    opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella?: Array<Aikajakso>
    tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
    joustavaPerusopetus?: Aikajakso
    oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    perusopetuksenAloittamistaLykätty?: boolean
    erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
    aloittanutEnnenOppivelvollisuutta?: boolean
    erityisenTuenPäätös?: ErityisenTuenPäätös
    tavoitekokonaisuuksittainOpiskelu?: Array<Aikajakso>
    ulkomailla?: Aikajakso
    toimintaAlueittainOpiskelu?: Array<Aikajakso>
    tuenPäätöksenJaksot?: Array<Tukijakso>
    vammainen?: Array<Aikajakso>
  } = {}
): PerusopetuksenOpiskeluoikeudenLisätiedot => ({
  aloittanutEnnenOppivelvollisuutta: false,
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

PerusopetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot' as const

export const isPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeudenLisätiedot'
