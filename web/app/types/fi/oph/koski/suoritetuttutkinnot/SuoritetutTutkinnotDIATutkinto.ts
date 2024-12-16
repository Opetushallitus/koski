import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotDIATutkinto
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto`
 */
export type SuoritetutTutkinnotDIATutkinto = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}

export const SuoritetutTutkinnotDIATutkinto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
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
