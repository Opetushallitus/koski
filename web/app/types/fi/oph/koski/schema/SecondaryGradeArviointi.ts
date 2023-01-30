import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * SecondaryGradeArviointi
 *
 * @see `fi.oph.koski.schema.SecondaryGradeArviointi`
 */
export type SecondaryGradeArviointi = {
  $class: 'fi.oph.koski.schema.SecondaryGradeArviointi'
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export const SecondaryGradeArviointi = (o: {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}): SecondaryGradeArviointi => ({
  $class: 'fi.oph.koski.schema.SecondaryGradeArviointi',
  ...o
})

SecondaryGradeArviointi.className =
  'fi.oph.koski.schema.SecondaryGradeArviointi' as const

export const isSecondaryGradeArviointi = (
  a: any
): a is SecondaryGradeArviointi =>
  a?.$class === 'fi.oph.koski.schema.SecondaryGradeArviointi'
