import {
  AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import { AmmatillisenTutkinnonOsanSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsanSuoritus'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  AmmatillisenTutkinnonOsittainenSuoritus,
  isAmmatillisenTutkinnonOsittainenSuoritus
} from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenSuoritus'
import {
  AmmatillisenTutkinnonSuoritus,
  isAmmatillisenTutkinnonSuoritus
} from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonSuoritus'
import {
  isMuunAmmatillisenTutkinnonOsanSuoritus,
  MuunAmmatillisenTutkinnonOsanSuoritus
} from '../types/fi/oph/koski/schema/MuunAmmatillisenTutkinnonOsanSuoritus'
import {
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from '../types/fi/oph/koski/schema/MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
import {
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
} from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus,
  OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
} from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import { OsittaisenAmmatillisenTutkinnonOsanSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanSuoritus'
import {
  isYhteisenAmmatillisenTutkinnonOsanSuoritus,
  YhteisenAmmatillisenTutkinnonOsanSuoritus
} from '../types/fi/oph/koski/schema/YhteisenAmmatillisenTutkinnonOsanSuoritus'
import {
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus,
  YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
} from '../types/fi/oph/koski/schema/YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'

// Ammatillisen tutkinnon (koko tutkinto) ja ammatillisen tutkinnon osan/osien
// suorituksilla on skeemassa omat, kentiltään samanlaiset tutkinnon osien
// suoritusluokat. Käyttöliittymä käsittelee molempia samoilla komponenteilla;
// luokka valitaan vain uutta osasuoritusta luotaessa päätason suorituksen
// mukaan. Osia useasta tutkinnosta -suoritus ei kuulu tähän, koska sen osat
// liittyvät aina johonkin toiseen tutkintoon eivätkä yhteen perusteeseen.

export type AmisTutkinnonSuoritus =
  AmmatillisenTutkinnonSuoritus | AmmatillisenTutkinnonOsittainenSuoritus

export const isAmisTutkinnonSuoritus = (
  s: unknown
): s is AmisTutkinnonSuoritus =>
  isAmmatillisenTutkinnonSuoritus(s) ||
  isAmmatillisenTutkinnonOsittainenSuoritus(s)

export type AmisTutkinnonOsanSuoritus =
  | AmmatillisenTutkinnonOsanSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanSuoritus

export type AmisYhteisenTutkinnonOsanSuoritus =
  | YhteisenAmmatillisenTutkinnonOsanSuoritus
  | YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus

export const isAmisYhteisenTutkinnonOsanSuoritus = (
  s: unknown
): s is AmisYhteisenTutkinnonOsanSuoritus =>
  isYhteisenAmmatillisenTutkinnonOsanSuoritus(s) ||
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(s)

export type AmisMuunTutkinnonOsanSuoritus =
  | MuunAmmatillisenTutkinnonOsanSuoritus
  | MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus

export const isAmisMuunTutkinnonOsanSuoritus = (
  s: unknown
): s is AmisMuunTutkinnonOsanSuoritus =>
  isMuunAmmatillisenTutkinnonOsanSuoritus(s) ||
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(s)

export type AmisKorkeakouluopintoSuoritus =
  | AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus

export const isAmisKorkeakouluopintoSuoritus = (
  s: unknown
): s is AmisKorkeakouluopintoSuoritus =>
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(s) ||
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(s)

export type AmisJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  | AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus

export const isAmisJatkoOpintovalmiuksiaTukevienOpintojenSuoritus = (
  s: unknown
): s is AmisJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    s
  ) ||
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    s
  )

type YhteisenOsanKentät = Parameters<
  typeof YhteisenAmmatillisenTutkinnonOsanSuoritus
>[0]

export const newYhteisenTutkinnonOsanSuoritus = (
  päätasonSuoritus: AmisTutkinnonSuoritus,
  kentät: YhteisenOsanKentät
): AmisYhteisenTutkinnonOsanSuoritus =>
  isAmmatillisenTutkinnonSuoritus(päätasonSuoritus)
    ? YhteisenAmmatillisenTutkinnonOsanSuoritus(kentät)
    : YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(kentät)

type MuunOsanKentät = Parameters<
  typeof MuunAmmatillisenTutkinnonOsanSuoritus
>[0]

export const newMuunTutkinnonOsanSuoritus = (
  päätasonSuoritus: AmisTutkinnonSuoritus,
  kentät: MuunOsanKentät
): AmisMuunTutkinnonOsanSuoritus =>
  isAmmatillisenTutkinnonSuoritus(päätasonSuoritus)
    ? MuunAmmatillisenTutkinnonOsanSuoritus(kentät)
    : MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(kentät)

type ValinnanMahdollisuudenKentät = {
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1'>
}

export const newKorkeakouluopintoSuoritus = (
  päätasonSuoritus: AmisTutkinnonSuoritus,
  kentät: ValinnanMahdollisuudenKentät
): AmisKorkeakouluopintoSuoritus =>
  isAmmatillisenTutkinnonSuoritus(päätasonSuoritus)
    ? AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(kentät)
    : OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(kentät)

export const newJatkoOpintovalmiuksiaTukevienOpintojenSuoritus = (
  päätasonSuoritus: AmisTutkinnonSuoritus,
  kentät: ValinnanMahdollisuudenKentät
): AmisJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
  isAmmatillisenTutkinnonSuoritus(päätasonSuoritus)
    ? AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
        kentät
      )
    : OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
        kentät
      )
