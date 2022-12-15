import {
  TutkinnonOsaaPienemmänKokonaisuudenSuoritus,
  isTutkinnonOsaaPienemmänKokonaisuudenSuoritus
} from './TutkinnonOsaaPienemmanKokonaisuudenSuoritus'
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

    | TutkinnonOsaaPienemmänKokonaisuudenSuoritus
    | YhteisenTutkinnonOsanOsaAlueenSuoritus

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =
  (
    a: any
  ): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =>
    isTutkinnonOsaaPienemmänKokonaisuudenSuoritus(a) ||
    isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)
