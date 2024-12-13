import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotEBTutkinto
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto`
 */
export type SuoritetutTutkinnotEBTutkinto = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  curriculum: SuoritetutTutkinnotKoodistokoodiviite
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}

export const SuoritetutTutkinnotEBTutkinto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  curriculum: SuoritetutTutkinnotKoodistokoodiviite
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}): SuoritetutTutkinnotEBTutkinto => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto',
  ...o
})

SuoritetutTutkinnotEBTutkinto.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto' as const

export const isSuoritetutTutkinnotEBTutkinto = (
  a: any
): a is SuoritetutTutkinnotEBTutkinto =>
  a?.$class === 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinto'
