import {
  AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus,
  isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus
} from './AktiivisetJaPaattyneetOpinnotKorkeakoulunOpintojaksonSuoritus'
import {
  AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus,
  isAktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus
} from './AktiivisetJaPaattyneetOpinnotKorkeakoulututkinnonSuoritus'
import {
  AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus,
  isAktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus
} from './AktiivisetJaPaattyneetOpinnotMuuKorkeakoulunSuoritus'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus =
  | AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus
  | AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus
  | AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus

export const isAktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus =>
  isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus(a) ||
  isAktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus(a) ||
  isAktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus(a)
