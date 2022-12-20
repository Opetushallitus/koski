import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * KorkeakoulunKoodistostaLöytyväArviointi
 *
 * @see `fi.oph.koski.schema.KorkeakoulunKoodistostaLöytyväArviointi`
 */
export type KorkeakoulunKoodistostaLöytyväArviointi = {
  $class: 'fi.oph.koski.schema.KorkeakoulunKoodistostaLöytyväArviointi'
  arvosana: Koodistokoodiviite<'virtaarvosana', string>
  päivä: string
  hyväksytty?: boolean
}

export const KorkeakoulunKoodistostaLöytyväArviointi = (o: {
  arvosana: Koodistokoodiviite<'virtaarvosana', string>
  päivä: string
  hyväksytty?: boolean
}): KorkeakoulunKoodistostaLöytyväArviointi => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunKoodistostaLöytyväArviointi',
  ...o
})

export const isKorkeakoulunKoodistostaLöytyväArviointi = (
  a: any
): a is KorkeakoulunKoodistostaLöytyväArviointi =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunKoodistostaLöytyväArviointi'
