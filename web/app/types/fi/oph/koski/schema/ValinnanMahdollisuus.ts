import {
  AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from './AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from './AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import {
  OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from './OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from './OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'

/**
 * ValinnanMahdollisuus
 *
 * @see `fi.oph.koski.schema.ValinnanMahdollisuus`
 */
export type ValinnanMahdollisuus =
  | AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus

export const isValinnanMahdollisuus = (a: any): a is ValinnanMahdollisuus =>
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a)
