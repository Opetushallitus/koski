import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { Laajuus } from './Laajuus'

/**
 * Korkeakoulun opintojakson tunnistetiedot
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpintojakso`
 */
export type KorkeakoulunOpintojakso = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpintojakso'
  tunniste: PaikallinenKoodi
  nimi: LocalizedString
  laajuus?: Laajuus
}

export const KorkeakoulunOpintojakso = (o: {
  tunniste: PaikallinenKoodi
  nimi: LocalizedString
  laajuus?: Laajuus
}): KorkeakoulunOpintojakso => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunOpintojakso',
  ...o
})

export const isKorkeakoulunOpintojakso = (
  a: any
): a is KorkeakoulunOpintojakso => a?.$class === 'KorkeakoulunOpintojakso'
