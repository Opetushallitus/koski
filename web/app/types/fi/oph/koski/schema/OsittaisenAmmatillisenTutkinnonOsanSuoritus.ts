import {
  MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from './MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
import {
  OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from './OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from './OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import {
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from './YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'

/**
 * OsittaisenAmmatillisenTutkinnonOsanSuoritus
 *
 * @see `fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanSuoritus`
 */
export type OsittaisenAmmatillisenTutkinnonOsanSuoritus =
  | MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus

export const isOsittaisenAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is OsittaisenAmmatillisenTutkinnonOsanSuoritus =>
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(a) ||
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(a)
