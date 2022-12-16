import {
  AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from './AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from './AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import {
  MuunAmmatillisenTutkinnonOsanSuoritus,
  isMuunAmmatillisenTutkinnonOsanSuoritus
} from './MuunAmmatillisenTutkinnonOsanSuoritus'
import {
  YhteisenAmmatillisenTutkinnonOsanSuoritus,
  isYhteisenAmmatillisenTutkinnonOsanSuoritus
} from './YhteisenAmmatillisenTutkinnonOsanSuoritus'

/**
 * AmmatillisenTutkinnonOsanSuoritus
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsanSuoritus`
 */
export type AmmatillisenTutkinnonOsanSuoritus =
  | AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | MuunAmmatillisenTutkinnonOsanSuoritus
  | YhteisenAmmatillisenTutkinnonOsanSuoritus

export const isAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsanSuoritus =>
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isMuunAmmatillisenTutkinnonOsanSuoritus(a) ||
  isYhteisenAmmatillisenTutkinnonOsanSuoritus(a)
