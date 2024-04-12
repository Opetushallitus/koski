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
 * TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus`
 */
export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =

    | PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
    | ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
    | YhteisenTutkinnonOsanOsaAlueenSuoritus

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =
  (
    a: any
  ): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =>
    isPaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
      a
    ) ||
    isValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
      a
    ) ||
    isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)
