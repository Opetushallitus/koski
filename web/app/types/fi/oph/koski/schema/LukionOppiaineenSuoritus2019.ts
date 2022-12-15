import { LukionOppiaineenArviointi2019 } from './LukionOppiaineenArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOppiaine2019 } from './LukionOppiaine2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 } from './LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019'

/**
 * Lukion oppiaineen suoritustiedot 2019
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenSuoritus2019`
 */
export type LukionOppiaineenSuoritus2019 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineenSuoritus2019'
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}

export const LukionOppiaineenSuoritus2019 = (o: {
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}): LukionOppiaineenSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  suoritettuErityisenäTutkintona: false,
  $class: 'fi.oph.koski.schema.LukionOppiaineenSuoritus2019',
  ...o
})

export const isLukionOppiaineenSuoritus2019 = (
  a: any
): a is LukionOppiaineenSuoritus2019 =>
  a?.$class === 'LukionOppiaineenSuoritus2019'
