import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKorkeakoulututkinto
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto`
 */
export type SuoritetutTutkinnotKorkeakoulututkinto = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto'
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
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto',
  ...o
})

SuoritetutTutkinnotKorkeakoulututkinto.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto' as const

export const isSuoritetutTutkinnotKorkeakoulututkinto = (
  a: any
): a is SuoritetutTutkinnotKorkeakoulututkinto =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKorkeakoulututkinto'
