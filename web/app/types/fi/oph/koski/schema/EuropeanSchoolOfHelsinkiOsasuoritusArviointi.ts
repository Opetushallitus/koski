import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * EuropeanSchoolOfHelsinkiOsasuoritusArviointi
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOsasuoritusArviointi`
 */
export type EuropeanSchoolOfHelsinkiOsasuoritusArviointi = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOsasuoritusArviointi'
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiosasuoritus',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export const EuropeanSchoolOfHelsinkiOsasuoritusArviointi = (o: {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiosasuoritus',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}): EuropeanSchoolOfHelsinkiOsasuoritusArviointi => ({
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOsasuoritusArviointi',
  ...o
})

export const isEuropeanSchoolOfHelsinkiOsasuoritusArviointi = (
  a: any
): a is EuropeanSchoolOfHelsinkiOsasuoritusArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOsasuoritusArviointi'
