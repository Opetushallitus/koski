import {
  LukioOpintojenSuoritus,
  isLukioOpintojenSuoritus
} from './LukioOpintojenSuoritus'
import {
  MuidenOpintovalmiuksiaTukevienOpintojenSuoritus,
  isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus
} from './MuidenOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  YhteisenTutkinnonOsanOsaAlueenSuoritus,
  isYhteisenTutkinnonOsanOsaAlueenSuoritus
} from './YhteisenTutkinnonOsanOsaAlueenSuoritus'

/**
 * YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
 *
 * @see `fi.oph.koski.schema.YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus`
 */
export type YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =

    | LukioOpintojenSuoritus
    | MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
    | YhteisenTutkinnonOsanOsaAlueenSuoritus

export const isYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =
  (
    a: any
  ): a is YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =>
    isLukioOpintojenSuoritus(a) ||
    isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(a) ||
    isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)
