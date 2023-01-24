import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from './LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBLukionModuuliMuissaOpinnoissa2019 } from './PreIBLukionModuuliMuissaOpinnoissa2019'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * PreIBLukionModuulinSuoritusMuissaOpinnoissa2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuulinSuoritusMuissaOpinnoissa2019`
 */
export type PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 = {
  $class: 'fi.oph.koski.schema.PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionModuuliMuissaOpinnoissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export const PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 = (o: {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionModuuliMuissaOpinnoissa2019
  tunnustettu?: OsaamisenTunnustaminen
}): PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionvaltakunnallinenmoduuli',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PreIBLukionModuulinSuoritusMuissaOpinnoissa2019',
  ...o
})

PreIBLukionModuulinSuoritusMuissaOpinnoissa2019.className =
  'fi.oph.koski.schema.PreIBLukionModuulinSuoritusMuissaOpinnoissa2019' as const

export const isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019 = (
  a: any
): a is PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 =>
  a?.$class ===
  'fi.oph.koski.schema.PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
