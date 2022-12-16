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
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
}

export const KorkeakouluopintojenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenkorkeakouluopintoja'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
}): KorkeakouluopintojenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenkorkeakouluopintoja',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.KorkeakouluopintojenSuoritus',
  ...o
})

export const isKorkeakouluopintojenSuoritus = (
  a: any
): a is KorkeakouluopintojenSuoritus =>
  a?.$class === 'KorkeakouluopintojenSuoritus'
