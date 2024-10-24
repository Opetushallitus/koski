import { KorkeakoulunPaikallinenArvosana } from './KorkeakoulunPaikallinenArvosana'

/**
 * KorkeakoulunPaikallinenArviointi
 *
 * @see `fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi`
 */
export type KorkeakoulunPaikallinenArviointi = {
  $class: 'fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi'
  arvosana: KorkeakoulunPaikallinenArvosana
  päivä: string
  hyväksytty?: boolean
}

export const KorkeakoulunPaikallinenArviointi = (o: {
  arvosana: KorkeakoulunPaikallinenArvosana
  päivä: string
  hyväksytty?: boolean
}): KorkeakoulunPaikallinenArviointi => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi',
  ...o
})

KorkeakoulunPaikallinenArviointi.className =
  'fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi' as const

export const isKorkeakoulunPaikallinenArviointi = (
  a: any
): a is KorkeakoulunPaikallinenArviointi =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi'
