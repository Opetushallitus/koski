import {
  MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus,
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
} from './MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
import {
  PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus,
  isPaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
} from './PaikalliseenTutkinnonOsaanLiittyvanTutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import {
  ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus,
  isValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
} from './ValtakunnalliseenTutkinnonOsaanLiittyvanTutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import {
  YhteisenTutkinnonOsanOsaAlueenSuoritus,
  isYhteisenTutkinnonOsanOsaAlueenSuoritus
} from './YhteisenTutkinnonOsanOsaAlueenSuoritus'

/**
 * MuuAmmatillinenOsasuoritus
 *
 * @see `fi.oph.koski.schema.MuuAmmatillinenOsasuoritus`
 */
export type MuuAmmatillinenOsasuoritus =
  | MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
  | PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
  | ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
  | YhteisenTutkinnonOsanOsaAlueenSuoritus

export const isMuuAmmatillinenOsasuoritus = (
  a: any
): a is MuuAmmatillinenOsasuoritus =>
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(a) ||
  isPaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
    a
  ) ||
  isValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
    a
  ) ||
  isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)
