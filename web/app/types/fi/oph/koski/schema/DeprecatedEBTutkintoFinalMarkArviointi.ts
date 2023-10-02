import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * DeprecatedEBTutkintoFinalMarkArviointi
 *
 * @see `fi.oph.koski.schema.DeprecatedEBTutkintoFinalMarkArviointi`
 */
export type DeprecatedEBTutkintoFinalMarkArviointi = {
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkintoFinalMarkArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkifinalmark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export const DeprecatedEBTutkintoFinalMarkArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkifinalmark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}): DeprecatedEBTutkintoFinalMarkArviointi => ({
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkintoFinalMarkArviointi',
  ...o
})

DeprecatedEBTutkintoFinalMarkArviointi.className =
  'fi.oph.koski.schema.DeprecatedEBTutkintoFinalMarkArviointi' as const

export const isDeprecatedEBTutkintoFinalMarkArviointi = (
  a: any
): a is DeprecatedEBTutkintoFinalMarkArviointi =>
  a?.$class === 'fi.oph.koski.schema.DeprecatedEBTutkintoFinalMarkArviointi'
