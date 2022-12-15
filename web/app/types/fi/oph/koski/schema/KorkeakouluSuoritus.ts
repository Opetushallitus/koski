import {
  KorkeakoulunOpintojaksonSuoritus,
  isKorkeakoulunOpintojaksonSuoritus
} from './KorkeakoulunOpintojaksonSuoritus'
import {
  KorkeakoulututkinnonSuoritus,
  isKorkeakoulututkinnonSuoritus
} from './KorkeakoulututkinnonSuoritus'
import {
  MuuKorkeakoulunSuoritus,
  isMuuKorkeakoulunSuoritus
} from './MuuKorkeakoulunSuoritus'

/**
 * KorkeakouluSuoritus
 *
 * @see `fi.oph.koski.schema.KorkeakouluSuoritus`
 */
export type KorkeakouluSuoritus =
  | KorkeakoulunOpintojaksonSuoritus
  | KorkeakoulututkinnonSuoritus
  | MuuKorkeakoulunSuoritus

export const isKorkeakouluSuoritus = (a: any): a is KorkeakouluSuoritus =>
  isKorkeakoulunOpintojaksonSuoritus(a) ||
  isKorkeakoulututkinnonSuoritus(a) ||
  isMuuKorkeakoulunSuoritus(a)
