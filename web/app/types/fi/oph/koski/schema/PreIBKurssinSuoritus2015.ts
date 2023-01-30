import { LukionArviointi } from './LukionArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBKurssi2015 } from './PreIBKurssi2015'

/**
 * PreIBKurssinSuoritus2015
 *
 * @see `fi.oph.koski.schema.PreIBKurssinSuoritus2015`
 */
export type PreIBKurssinSuoritus2015 = {
  $class: 'fi.oph.koski.schema.PreIBKurssinSuoritus2015'
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBKurssi2015
}

export const PreIBKurssinSuoritus2015 = (o: {
  arviointi?: Array<LukionArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'preibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBKurssi2015
}): PreIBKurssinSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'preibkurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PreIBKurssinSuoritus2015',
  ...o
})

PreIBKurssinSuoritus2015.className =
  'fi.oph.koski.schema.PreIBKurssinSuoritus2015' as const

export const isPreIBKurssinSuoritus2015 = (
  a: any
): a is PreIBKurssinSuoritus2015 =>
  a?.$class === 'fi.oph.koski.schema.PreIBKurssinSuoritus2015'
