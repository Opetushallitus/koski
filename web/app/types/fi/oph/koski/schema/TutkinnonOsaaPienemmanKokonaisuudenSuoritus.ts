import { MuunAmmatillisenKoulutuksenArviointi } from './MuunAmmatillisenKoulutuksenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto } from './MuunAmmatillisenKoulutuksenOsasuorituksenLisatieto'
import { TutkinnonOsaaPienempiKokonaisuus } from './TutkinnonOsaaPienempiKokonaisuus'

/**
 * TutkinnonOsaaPienemmänKokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.TutkinnonOsaaPienemmänKokonaisuudenSuoritus`
 */
export type TutkinnonOsaaPienemmänKokonaisuudenSuoritus = {
  $class: 'fi.oph.koski.schema.TutkinnonOsaaPienemmänKokonaisuudenSuoritus'
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

export const TutkinnonOsaaPienemmänKokonaisuudenSuoritus = (o: {
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
}): TutkinnonOsaaPienemmänKokonaisuudenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'tutkinnonosaapienempikokonaisuus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.TutkinnonOsaaPienemmänKokonaisuudenSuoritus',
  ...o
})

TutkinnonOsaaPienemmänKokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.TutkinnonOsaaPienemmänKokonaisuudenSuoritus' as const

export const isTutkinnonOsaaPienemmänKokonaisuudenSuoritus = (
  a: any
): a is TutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkinnonOsaaPienemmänKokonaisuudenSuoritus'
