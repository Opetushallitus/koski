import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from './LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBLukionModuuliOppiaineissa2019 } from './PreIBLukionModuuliOppiaineissa2019'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * PreIBLukionModuulinSuoritusOppiaineissa2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuulinSuoritusOppiaineissa2019`
 */
export type PreIBLukionModuulinSuoritusOppiaineissa2019 = {
  $class: 'fi.oph.koski.schema.PreIBLukionModuulinSuoritusOppiaineissa2019'
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionModuuliOppiaineissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export const PreIBLukionModuulinSuoritusOppiaineissa2019 = (o: {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionModuuliOppiaineissa2019
  tunnustettu?: OsaamisenTunnustaminen
}): PreIBLukionModuulinSuoritusOppiaineissa2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionvaltakunnallinenmoduuli',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PreIBLukionModuulinSuoritusOppiaineissa2019',
  ...o
})

export const isPreIBLukionModuulinSuoritusOppiaineissa2019 = (
  a: any
): a is PreIBLukionModuulinSuoritusOppiaineissa2019 =>
  a?.$class ===
  'fi.oph.koski.schema.PreIBLukionModuulinSuoritusOppiaineissa2019'
