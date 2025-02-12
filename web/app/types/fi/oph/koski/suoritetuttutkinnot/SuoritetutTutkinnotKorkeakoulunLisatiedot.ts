import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotKorkeakoulunLisätiedot
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot`
 */
export type SuoritetutTutkinnotKorkeakoulunLisätiedot = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot'
  virtaOpiskeluoikeudenTyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  opettajanPätevyys?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  opetettavanAineenPätevyys?: Array<SuoritetutTutkinnotKoodistokoodiviite>
}

export const SuoritetutTutkinnotKorkeakoulunLisätiedot = (
  o: {
    virtaOpiskeluoikeudenTyyppi?: SuoritetutTutkinnotKoodistokoodiviite
    opettajanPätevyys?: Array<SuoritetutTutkinnotKoodistokoodiviite>
    opetettavanAineenPätevyys?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  } = {}
): SuoritetutTutkinnotKorkeakoulunLisätiedot => ({
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot',
  ...o
})

SuoritetutTutkinnotKorkeakoulunLisätiedot.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot' as const

export const isSuoritetutTutkinnotKorkeakoulunLisätiedot = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulunLisätiedot =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot'
