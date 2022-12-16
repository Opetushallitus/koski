import {
  KorkeakoulunKoodistostaLöytyväArviointi,
  isKorkeakoulunKoodistostaLöytyväArviointi
} from './KorkeakoulunKoodistostaLoytyvaArviointi'
import {
  KorkeakoulunPaikallinenArviointi,
  isKorkeakoulunPaikallinenArviointi
} from './KorkeakoulunPaikallinenArviointi'

/**
 * KorkeakoulunArviointi
 *
 * @see `fi.oph.koski.schema.KorkeakoulunArviointi`
 */
export type KorkeakoulunArviointi =
  | KorkeakoulunKoodistostaLöytyväArviointi
  | KorkeakoulunPaikallinenArviointi

export const isKorkeakoulunArviointi = (a: any): a is KorkeakoulunArviointi =>
  isKorkeakoulunKoodistostaLöytyväArviointi(a) ||
  isKorkeakoulunPaikallinenArviointi(a)
