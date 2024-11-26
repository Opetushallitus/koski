import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKorkeakoulututkinto
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto`
 */
export type SuoritetutTutkinnotKorkeakoulututkinto = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  virtaNimi?: LocalizedString
}

export const SuoritetutTutkinnotKorkeakoulututkinto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  virtaNimi?: LocalizedString
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
