import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { AmmatillisenTutkinnonOsaaPienempiKokonaisuus } from './AmmatillisenTutkinnonOsaaPienempiKokonaisuus'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Muiden kuin yhteisten tutkinnon osien osasuoritukset
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus`
 */
export type AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosaapienempikokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
}

export const AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosaapienempikokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
}): AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillisentutkinnonosaapienempikokonaisuus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus',
  ...o
})

AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus' as const

export const isAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
