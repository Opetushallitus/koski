import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotKorkeakoulunLisätiedot
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot`
 */
export type SuoritetutTutkinnotKorkeakoulunLisätiedot = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot'
  virtaOpiskeluoikeudenTyyppi?: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotKorkeakoulunLisätiedot = (
  o: {
    virtaOpiskeluoikeudenTyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  } = {}
): SuoritetutTutkinnotKorkeakoulunLisätiedot => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot',
  ...o
})

SuoritetutTutkinnotKorkeakoulunLisätiedot.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot' as const

export const isSuoritetutTutkinnotKorkeakoulunLisätiedot = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulunLisätiedot =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulunLisätiedot'
