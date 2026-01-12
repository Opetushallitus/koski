import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto } from './MuunAmmatillisenKoulutuksenOsasuorituksenLisatieto'
import { TutkinnonOsaaPienempiKokonaisuus } from './TutkinnonOsaaPienempiKokonaisuus'
import { Näyttö } from './Naytto'

/**
 * ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus`
 */
export type ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
    arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
    liittyyTutkinnonOsaan: Koodistokoodiviite<'tutkinnonosat', string>
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
    näyttö?: Näyttö
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkinnonosaapienempikokonaisuus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }

export const ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  (o: {
    arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
    liittyyTutkinnonOsaan: Koodistokoodiviite<'tutkinnonosat', string>
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
    näyttö?: Näyttö
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkinnonosaapienempikokonaisuus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }): ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus => ({
    $class:
      'fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tutkinnonosaapienempikokonaisuus',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus' as const

export const isValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  (
    a: any
  ): a is ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
