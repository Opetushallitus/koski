import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotEBTutkinto
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto`
 */
export type SuoritetutTutkinnotEBTutkinto = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  curriculum: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotEBTutkinto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  curriculum: SuoritetutTutkinnotKoodistokoodiviite
}): SuoritetutTutkinnotEBTutkinto => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto',
  ...o
})

SuoritetutTutkinnotEBTutkinto.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto' as const

export const isSuoritetutTutkinnotEBTutkinto = (
  a: any
): a is SuoritetutTutkinnotEBTutkinto =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto'
