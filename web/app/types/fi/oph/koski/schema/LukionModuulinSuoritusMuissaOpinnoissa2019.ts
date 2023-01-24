import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from './LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionModuuliMuissaOpinnoissa2019 } from './LukionModuuliMuissaOpinnoissa2019'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Lukion moduulin suoritustiedot muissa opinnoissa 2019
 *
 * @see `fi.oph.koski.schema.LukionModuulinSuoritusMuissaOpinnoissa2019`
 */
export type LukionModuulinSuoritusMuissaOpinnoissa2019 = {
  $class: 'fi.oph.koski.schema.LukionModuulinSuoritusMuissaOpinnoissa2019'
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionModuuliMuissaOpinnoissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export const LukionModuulinSuoritusMuissaOpinnoissa2019 = (o: {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionModuuliMuissaOpinnoissa2019
  tunnustettu?: OsaamisenTunnustaminen
}): LukionModuulinSuoritusMuissaOpinnoissa2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionvaltakunnallinenmoduuli',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionModuulinSuoritusMuissaOpinnoissa2019',
  ...o
})

LukionModuulinSuoritusMuissaOpinnoissa2019.className =
  'fi.oph.koski.schema.LukionModuulinSuoritusMuissaOpinnoissa2019' as const

export const isLukionModuulinSuoritusMuissaOpinnoissa2019 = (
  a: any
): a is LukionModuulinSuoritusMuissaOpinnoissa2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionModuulinSuoritusMuissaOpinnoissa2019'
