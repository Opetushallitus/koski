import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto } from './MuunAmmatillisenKoulutuksenOsasuorituksenLisatieto'
import { TutkinnonOsaaPienempiKokonaisuus } from './TutkinnonOsaaPienempiKokonaisuus'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'

/**
 * PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus`
 */
export type PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
  {
    $class: 'fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
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
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
    liittyyTutkintoon: AmmatillinenTutkintoKoulutus
  }

export const PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
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
    koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
    liittyyTutkintoon: AmmatillinenTutkintoKoulutus
  }): PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tutkinnonosaapienempikokonaisuus',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus',
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
