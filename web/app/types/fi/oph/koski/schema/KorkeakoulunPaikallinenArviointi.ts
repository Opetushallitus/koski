import { PaikallinenKoodi } from './PaikallinenKoodi'

/**
 * KorkeakoulunPaikallinenArviointi
 *
 * @see `fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi`
 */
export type KorkeakoulunPaikallinenArviointi = {
  $class: 'fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi'
  arvosana: PaikallinenKoodi
  päivä: string
  hyväksytty?: boolean
}

export const KorkeakoulunPaikallinenArviointi = (o: {
  arvosana: PaikallinenKoodi
  päivä: string
  hyväksytty?: boolean
}): KorkeakoulunPaikallinenArviointi => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunPaikallinenArviointi',
  ...o
})

export const isKorkeakoulunPaikallinenArviointi = (
  a: any
): a is KorkeakoulunPaikallinenArviointi =>
  a?.$class === 'KorkeakoulunPaikallinenArviointi'