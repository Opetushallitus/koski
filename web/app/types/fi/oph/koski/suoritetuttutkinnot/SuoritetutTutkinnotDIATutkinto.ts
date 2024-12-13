import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotDIATutkinto
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto`
 */
export type SuoritetutTutkinnotDIATutkinto = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotDIATutkinto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}): SuoritetutTutkinnotDIATutkinto => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto',
  ...o
})

SuoritetutTutkinnotDIATutkinto.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto' as const

export const isSuoritetutTutkinnotDIATutkinto = (
  a: any
): a is SuoritetutTutkinnotDIATutkinto =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto'
