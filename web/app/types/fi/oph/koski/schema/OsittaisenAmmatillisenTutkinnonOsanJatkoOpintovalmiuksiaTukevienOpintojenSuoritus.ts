import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa } from './JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa'
import { YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus } from './YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus'

/**
 * Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja
 *
 * @see `fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus`
 */
export type OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus>
  }

export const OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
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
  ): OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus => ({
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
      'fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus',
    ...o
  })

export const isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  (
    a: any
  ): a is OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
