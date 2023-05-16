import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotDIATutkinto
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto`
 */
export type SuoritetutTutkinnotDIATutkinto = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotDIATutkinto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}): SuoritetutTutkinnotDIATutkinto => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto',
  ...o
})

SuoritetutTutkinnotDIATutkinto.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto' as const

export const isSuoritetutTutkinnotDIATutkinto = (
  a: any
): a is SuoritetutTutkinnotDIATutkinto =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto'
