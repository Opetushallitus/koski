import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotKorkeakoulututkinto
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto`
 */
export type SuoritetutTutkinnotKorkeakoulututkinto = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto'
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  virtaNimi?: LocalizedString
  koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotKorkeakoulututkinto = (o: {
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  virtaNimi?: LocalizedString
  koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}): SuoritetutTutkinnotKorkeakoulututkinto => ({
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto',
  ...o
})

SuoritetutTutkinnotKorkeakoulututkinto.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto' as const

export const isSuoritetutTutkinnotKorkeakoulututkinto = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulututkinto =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto'
