import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * DeprecatedEBTutkintoPreliminaryMarkArviointi
 *
 * @see `fi.oph.koski.schema.DeprecatedEBTutkintoPreliminaryMarkArviointi`
 */
export type DeprecatedEBTutkintoPreliminaryMarkArviointi = {
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkintoPreliminaryMarkArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export const DeprecatedEBTutkintoPreliminaryMarkArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}): DeprecatedEBTutkintoPreliminaryMarkArviointi => ({
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkintoPreliminaryMarkArviointi',
  ...o
})

DeprecatedEBTutkintoPreliminaryMarkArviointi.className =
  'fi.oph.koski.schema.DeprecatedEBTutkintoPreliminaryMarkArviointi' as const

export const isDeprecatedEBTutkintoPreliminaryMarkArviointi = (
  a: any
): a is DeprecatedEBTutkintoPreliminaryMarkArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.DeprecatedEBTutkintoPreliminaryMarkArviointi'
