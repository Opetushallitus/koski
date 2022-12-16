import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from './LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionModuuliOppiaineissa2019 } from './LukionModuuliOppiaineissa2019'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Lukion moduulin suoritustiedot oppiaineissa 2019
 *
 * @see `fi.oph.koski.schema.LukionModuulinSuoritusOppiaineissa2019`
 */
export type LukionModuulinSuoritusOppiaineissa2019 = {
  $class: 'fi.oph.koski.schema.LukionModuulinSuoritusOppiaineissa2019'
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionModuuliOppiaineissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export const LukionModuulinSuoritusOppiaineissa2019 = (o: {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionModuuliOppiaineissa2019
  tunnustettu?: OsaamisenTunnustaminen
}): LukionModuulinSuoritusOppiaineissa2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionvaltakunnallinenmoduuli',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionModuulinSuoritusOppiaineissa2019',
  ...o
})

export const isLukionModuulinSuoritusOppiaineissa2019 = (
  a: any
): a is LukionModuulinSuoritusOppiaineissa2019 =>
  a?.$class === 'LukionModuulinSuoritusOppiaineissa2019'
