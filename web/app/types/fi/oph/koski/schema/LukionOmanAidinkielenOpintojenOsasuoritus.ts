import { LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi } from './LukionOmanAidinkielenOpinnonOsasuorituksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOmanÄidinkielenOpinto } from './LukionOmanAidinkielenOpinto'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * LukionOmanÄidinkielenOpintojenOsasuoritus
 *
 * @see `fi.oph.koski.schema.LukionOmanÄidinkielenOpintojenOsasuoritus`
 */
export type LukionOmanÄidinkielenOpintojenOsasuoritus = {
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpintojenOsasuoritus'
  arviointi?: Array<LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOmanÄidinkielenOpinto
  tunnustettu?: OsaamisenTunnustaminen
}

export const LukionOmanÄidinkielenOpintojenOsasuoritus = (o: {
  arviointi?: Array<LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOmanÄidinkielenOpinto
  tunnustettu?: OsaamisenTunnustaminen
}): LukionOmanÄidinkielenOpintojenOsasuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionvaltakunnallinenmoduuli',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpintojenOsasuoritus',
  ...o
})

LukionOmanÄidinkielenOpintojenOsasuoritus.className =
  'fi.oph.koski.schema.LukionOmanÄidinkielenOpintojenOsasuoritus' as const

export const isLukionOmanÄidinkielenOpintojenOsasuoritus = (
  a: any
): a is LukionOmanÄidinkielenOpintojenOsasuoritus =>
  a?.$class === 'fi.oph.koski.schema.LukionOmanÄidinkielenOpintojenOsasuoritus'
