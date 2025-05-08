import { Aikajakso } from './Aikajakso'
import { ErityisenTuenPäätös } from './ErityisenTuenPaatos'
import { Tukijakso } from './Tukijakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TyhjäTukijakso } from './TyhjaTukijakso'

/**
 * EsiopetuksenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.EsiopetuksenOpiskeluoikeudenLisätiedot`
 */
export type EsiopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeudenLisätiedot'
  pidennettyOppivelvollisuus?: Aikajakso
  majoitusetu?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  koulukoti?: Array<Aikajakso>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  erityisenTuenPäätös?: ErityisenTuenPäätös
  tuenPäätöksenJaksot?: Array<Tukijakso>
  vammainen?: Array<Aikajakso>
  varhennetunOppivelvollisuudenJaksot?: Array<Aikajakso>
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  tukijaksot?: Array<TyhjäTukijakso>
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export const EsiopetuksenOpiskeluoikeudenLisätiedot = (
  o: {
    pidennettyOppivelvollisuus?: Aikajakso
    majoitusetu?: Aikajakso
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    koulukoti?: Array<Aikajakso>
    erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
    erityisenTuenPäätös?: ErityisenTuenPäätös
    tuenPäätöksenJaksot?: Array<Tukijakso>
    vammainen?: Array<Aikajakso>
    varhennetunOppivelvollisuudenJaksot?: Array<Aikajakso>
    tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
    tukijaksot?: Array<TyhjäTukijakso>
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  } = {}
): EsiopetuksenOpiskeluoikeudenLisätiedot => ({
  $class: 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeudenLisätiedot',
  ...o
})

EsiopetuksenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeudenLisätiedot' as const

export const isEsiopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is EsiopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeudenLisätiedot'
