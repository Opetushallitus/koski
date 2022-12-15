import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * YlioppilaskokeenArviointi
 *
 * @see `fi.oph.koski.schema.YlioppilaskokeenArviointi`
 */
export type YlioppilaskokeenArviointi = {
  $class: 'fi.oph.koski.schema.YlioppilaskokeenArviointi'
  arvosana: Koodistokoodiviite<'koskiyoarvosanat', string>
  pisteet?: number
  hyväksytty?: boolean
}

export const YlioppilaskokeenArviointi = (o: {
  arvosana: Koodistokoodiviite<'koskiyoarvosanat', string>
  pisteet?: number
  hyväksytty?: boolean
}): YlioppilaskokeenArviointi => ({
  $class: 'fi.oph.koski.schema.YlioppilaskokeenArviointi',
  ...o
})

export const isYlioppilaskokeenArviointi = (
  a: any
): a is YlioppilaskokeenArviointi => a?.$class === 'YlioppilaskokeenArviointi'
