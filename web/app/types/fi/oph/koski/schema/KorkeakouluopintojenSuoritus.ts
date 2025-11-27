import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus } from './KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * KorkeakouluopintojenSuoritus
 *
 * @see `fi.oph.koski.schema.KorkeakouluopintojenSuoritus`
 */
export type KorkeakouluopintojenSuoritus = {
  $class: 'fi.oph.koski.schema.KorkeakouluopintojenSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenkorkeakouluopintoja'
  >
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const KorkeakouluopintojenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenkorkeakouluopintoja'
  >
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): KorkeakouluopintojenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenkorkeakouluopintoja',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.KorkeakouluopintojenSuoritus',
  ...o
})

KorkeakouluopintojenSuoritus.className =
  'fi.oph.koski.schema.KorkeakouluopintojenSuoritus' as const

export const isKorkeakouluopintojenSuoritus = (
  a: any
): a is KorkeakouluopintojenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.KorkeakouluopintojenSuoritus'
