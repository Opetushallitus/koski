import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto } from './MuunAmmatillisenKoulutuksenOsasuorituksenLisatieto'
import { TutkinnonOsaaPienempiKokonaisuus } from './TutkinnonOsaaPienempiKokonaisuus'

/**
 * ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus`
 */
export type ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  {
    $class: 'fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
    arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
    näyttö?: Näyttö
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkinnonosaapienempikokonaisuus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
    liittyyTutkinnonOsaan: Koodistokoodiviite<'tutkinnonosat', string>
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
  }

export const ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  (o: {
    arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
    näyttö?: Näyttö
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkinnonosaapienempikokonaisuus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
    liittyyTutkinnonOsaan: Koodistokoodiviite<'tutkinnonosat', string>
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
  }): ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tutkinnonosaapienempikokonaisuus',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.ValtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus',
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
