import { LukionOppiaineenArviointi } from './LukionOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBOppiaine2015 } from './PreIBOppiaine2015'
import { PreIBKurssinSuoritus2015 } from './PreIBKurssinSuoritus2015'

/**
 * Pre-IB-oppiaineiden suoritusten tiedot
 *
 * @see `fi.oph.koski.schema.PreIBOppiaineenSuoritus2015`
 */
export type PreIBOppiaineenSuoritus2015 = {
  $class: 'fi.oph.koski.schema.PreIBOppiaineenSuoritus2015'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preiboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBOppiaine2015
  osasuoritukset?: Array<PreIBKurssinSuoritus2015>
}

export const PreIBOppiaineenSuoritus2015 = (o: {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'preiboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBOppiaine2015
  osasuoritukset?: Array<PreIBKurssinSuoritus2015>
}): PreIBOppiaineenSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'preiboppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PreIBOppiaineenSuoritus2015',
  ...o
})

export const isPreIBOppiaineenSuoritus2015 = (
  a: any
): a is PreIBOppiaineenSuoritus2015 =>
  a?.$class === 'PreIBOppiaineenSuoritus2015'
