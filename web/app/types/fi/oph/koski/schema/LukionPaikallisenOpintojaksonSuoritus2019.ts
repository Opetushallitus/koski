import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from './LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionPaikallinenOpintojakso2019 } from './LukionPaikallinenOpintojakso2019'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Lukion paikallisen opintojakson suoritustiedot 2019
 *
 * @see `fi.oph.koski.schema.LukionPaikallisenOpintojaksonSuoritus2019`
 */
export type LukionPaikallisenOpintojaksonSuoritus2019 = {
  $class: 'fi.oph.koski.schema.LukionPaikallisenOpintojaksonSuoritus2019'
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionpaikallinenopintojakso'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionPaikallinenOpintojakso2019
  tunnustettu?: OsaamisenTunnustaminen
}

export const LukionPaikallisenOpintojaksonSuoritus2019 = (o: {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionpaikallinenopintojakso'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionPaikallinenOpintojakso2019
  tunnustettu?: OsaamisenTunnustaminen
}): LukionPaikallisenOpintojaksonSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionpaikallinenopintojakso',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionPaikallisenOpintojaksonSuoritus2019',
  ...o
})

export const isLukionPaikallisenOpintojaksonSuoritus2019 = (
  a: any
): a is LukionPaikallisenOpintojaksonSuoritus2019 =>
  a?.$class === 'LukionPaikallisenOpintojaksonSuoritus2019'
