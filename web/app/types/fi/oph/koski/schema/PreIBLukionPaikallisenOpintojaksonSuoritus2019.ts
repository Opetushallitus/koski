import { LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 } from './LukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBPaikallinenOpintojakso2019 } from './PreIBPaikallinenOpintojakso2019'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * PreIBLukionPaikallisenOpintojaksonSuoritus2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionPaikallisenOpintojaksonSuoritus2019`
 */
export type PreIBLukionPaikallisenOpintojaksonSuoritus2019 = {
  $class: 'fi.oph.koski.schema.PreIBLukionPaikallisenOpintojaksonSuoritus2019'
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionpaikallinenopintojakso'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBPaikallinenOpintojakso2019
  tunnustettu?: OsaamisenTunnustaminen
}

export const PreIBLukionPaikallisenOpintojaksonSuoritus2019 = (o: {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionpaikallinenopintojakso'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBPaikallinenOpintojakso2019
  tunnustettu?: OsaamisenTunnustaminen
}): PreIBLukionPaikallisenOpintojaksonSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionpaikallinenopintojakso',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PreIBLukionPaikallisenOpintojaksonSuoritus2019',
  ...o
})

export const isPreIBLukionPaikallisenOpintojaksonSuoritus2019 = (
  a: any
): a is PreIBLukionPaikallisenOpintojaksonSuoritus2019 =>
  a?.$class === 'PreIBLukionPaikallisenOpintojaksonSuoritus2019'
