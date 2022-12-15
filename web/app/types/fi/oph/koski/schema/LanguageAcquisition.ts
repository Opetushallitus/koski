import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * LanguageAcquisition
 *
 * @see `fi.oph.koski.schema.LanguageAcquisition`
 */
export type LanguageAcquisition = {
  $class: 'fi.oph.koski.schema.LanguageAcquisition'
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'LAC'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'ES' | 'FI' | 'FR' | 'EN'>
}

export const LanguageAcquisition = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetinternationalschool', 'LAC'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'ES' | 'FI' | 'FR' | 'EN'>
}): LanguageAcquisition => ({
  $class: 'fi.oph.koski.schema.LanguageAcquisition',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'LAC',
    koodistoUri: 'oppiaineetinternationalschool'
  }),
  ...o
})

export const isLanguageAcquisition = (a: any): a is LanguageAcquisition =>
  a?.$class === 'LanguageAcquisition'
