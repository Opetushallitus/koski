import { LukionOppiaineenArviointi } from './LukionOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOppiaine2015 } from './LukionOppiaine2015'
import { LukionKurssinSuoritus2015 } from './LukionKurssinSuoritus2015'

/**
 * Lukion oppiaineen suoritustiedot
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenSuoritus2015`
 */
export type LukionOppiaineenSuoritus2015 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineenSuoritus2015'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}

export const LukionOppiaineenSuoritus2015 = (o: {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}): LukionOppiaineenSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionOppiaineenSuoritus2015',
  ...o
})

export const isLukionOppiaineenSuoritus2015 = (
  a: any
): a is LukionOppiaineenSuoritus2015 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppiaineenSuoritus2015'
