import {
  AmmatillisenTutkinnonOsittainenSuoritus,
  isAmmatillisenTutkinnonOsittainenSuoritus
} from './AmmatillisenTutkinnonOsittainenSuoritus'
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
 * AmmatillinenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.AmmatillinenPäätasonSuoritus`
 */
export type AmmatillinenPäätasonSuoritus =
  | AmmatillisenTutkinnonOsittainenSuoritus
  | AmmatillisenTutkinnonSuoritus
  | MuunAmmatillisenKoulutuksenSuoritus
  | NäyttötutkintoonValmistavanKoulutuksenSuoritus
  | TelmaKoulutuksenSuoritus
  | TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
  | ValmaKoulutuksenSuoritus

export const isAmmatillinenPäätasonSuoritus = (
  a: any
): a is AmmatillinenPäätasonSuoritus =>
  isAmmatillisenTutkinnonOsittainenSuoritus(a) ||
  isAmmatillisenTutkinnonSuoritus(a) ||
  isMuunAmmatillisenKoulutuksenSuoritus(a) ||
  isNäyttötutkintoonValmistavanKoulutuksenSuoritus(a) ||
  isTelmaKoulutuksenSuoritus(a) ||
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(a) ||
  isValmaKoulutuksenSuoritus(a)
