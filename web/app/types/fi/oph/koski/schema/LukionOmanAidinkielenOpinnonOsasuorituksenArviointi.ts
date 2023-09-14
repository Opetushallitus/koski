import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi
 *
 * @see `fi.oph.koski.schema.LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi`
 */
export type LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi = {
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export const LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}): LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi => ({
  $class:
    'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi',
  ...o
})

LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi.className =
  'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi' as const

export const isLukionOmanÄidinkielenOpinnonOsasuorituksenArviointi = (
  a: any
): a is LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi'
