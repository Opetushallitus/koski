import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto } from './MuunAmmatillisenKoulutuksenOsasuorituksenLisatieto'
import { TutkinnonOsaaPienempiKokonaisuus } from './TutkinnonOsaaPienempiKokonaisuus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { Näyttö } from './Naytto'

/**
 * PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus`
 */
export type PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  {
    $class: 'fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
    arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
    liittyyTutkintoon: AmmatillinenTutkintoKoulutus
    näyttö?: Näyttö
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkinnonosaapienempikokonaisuus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }

export const PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  (o: {
    arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
    liittyyTutkintoon: AmmatillinenTutkintoKoulutus
    näyttö?: Näyttö
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkinnonosaapienempikokonaisuus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    alkamispäivä?: string
  }): PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus => ({
    $class:
      'fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tutkinnonosaapienempikokonaisuus',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus' as const

export const isPaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  (
    a: any
  ): a is PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
