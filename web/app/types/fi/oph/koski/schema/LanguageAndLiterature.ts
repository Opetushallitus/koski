import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * LanguageAndLiterature
 *
 * @see `fi.oph.koski.schema.LanguageAndLiterature`
 */
export type LanguageAndLiterature = {
  $class: 'fi.oph.koski.schema.LanguageAndLiterature'
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'LL'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FI'>
}

export const LanguageAndLiterature = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetinternationalschool', 'LL'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FI'>
}): LanguageAndLiterature => ({
  $class: 'fi.oph.koski.schema.LanguageAndLiterature',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'LL',
    koodistoUri: 'oppiaineetinternationalschool'
  }),
  ...o
})

LanguageAndLiterature.className =
  'fi.oph.koski.schema.LanguageAndLiterature' as const

export const isLanguageAndLiterature = (a: any): a is LanguageAndLiterature =>
  a?.$class === 'fi.oph.koski.schema.LanguageAndLiterature'
