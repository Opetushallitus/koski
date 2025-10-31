import {
  AmmatillisenTutkinnonOsittainenSuoritus,
  isAmmatillisenTutkinnonOsittainenSuoritus
} from './AmmatillisenTutkinnonOsittainenSuoritus'
import {
  AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus,
  isAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
} from './AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
import {
  AmmatillisenTutkinnonSuoritus,
  isAmmatillisenTutkinnonSuoritus
} from './AmmatillisenTutkinnonSuoritus'
import {
  MuunAmmatillisenKoulutuksenSuoritus,
  isMuunAmmatillisenKoulutuksenSuoritus
} from './MuunAmmatillisenKoulutuksenSuoritus'
import {
  NäyttötutkintoonValmistavanKoulutuksenSuoritus,
  isNäyttötutkintoonValmistavanKoulutuksenSuoritus
} from './NayttotutkintoonValmistavanKoulutuksenSuoritus'
import {
  TelmaKoulutuksenSuoritus,
  isTelmaKoulutuksenSuoritus
} from './TelmaKoulutuksenSuoritus'
import {
  TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus,
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
} from './TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaSuoritus'
import {
  ValmaKoulutuksenSuoritus,
  isValmaKoulutuksenSuoritus
} from './ValmaKoulutuksenSuoritus'

/**
 * AmmatillinenPäätasonsuoritusLike
 *
 * @see `fi.oph.koski.schema.AmmatillinenPäätasonsuoritusLike`
 */
export type AmmatillinenPäätasonsuoritusLike =
  | AmmatillisenTutkinnonOsittainenSuoritus
  | AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
  | AmmatillisenTutkinnonSuoritus
  | MuunAmmatillisenKoulutuksenSuoritus
  | NäyttötutkintoonValmistavanKoulutuksenSuoritus
  | TelmaKoulutuksenSuoritus
  | TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
  | ValmaKoulutuksenSuoritus

export const isAmmatillinenPäätasonsuoritusLike = (
  a: any
): a is AmmatillinenPäätasonsuoritusLike =>
  isAmmatillisenTutkinnonOsittainenSuoritus(a) ||
  isAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus(a) ||
  isAmmatillisenTutkinnonSuoritus(a) ||
  isMuunAmmatillisenKoulutuksenSuoritus(a) ||
  isNäyttötutkintoonValmistavanKoulutuksenSuoritus(a) ||
  isTelmaKoulutuksenSuoritus(a) ||
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(a) ||
  isValmaKoulutuksenSuoritus(a)
