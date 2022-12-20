import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa } from './JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa'
import { YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus } from './YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus'

/**
 * Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus`
 */
export type AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus>
  }

export const AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  (
    o: {
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'ammatillisentutkinnonosa'
      >
      tila?: Koodistokoodiviite<'suorituksentila', string>
      koulutusmoduuli?: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa
      tutkinnonOsanRyhmä?: Koodistokoodiviite<
        'ammatillisentutkinnonosanryhma',
        '1'
      >
      osasuoritukset?: Array<YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus>
    } = {}
  ): AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'ammatillisentutkinnonosa',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa({
      tunniste: Koodistokoodiviite({
        koodiarvo: '1',
        koodistoUri: 'tutkinnonosatvalinnanmahdollisuus'
      })
    }),
    $class:
      'fi.oph.koski.schema.AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus',
    ...o
  })

export const isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  (
    a: any
  ): a is AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
