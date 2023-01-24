import { LukionOppiaineenArviointi2019 } from './LukionOppiaineenArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PreIBLukionOppiaine2019 } from './PreIBLukionOppiaine2019'
import { PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 } from './PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019'

/**
 * Lukion oppiaineen suoritus Pre-IB-opinnoissa 2019
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenPreIBSuoritus2019`
 */
export type LukionOppiaineenPreIBSuoritus2019 = {
  $class: 'fi.oph.koski.schema.LukionOppiaineenPreIBSuoritus2019'
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionOppiaine2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}

export const LukionOppiaineenPreIBSuoritus2019 = (o: {
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionOppiaine2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}): LukionOppiaineenPreIBSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  suoritettuErityisenäTutkintona: false,
  $class: 'fi.oph.koski.schema.LukionOppiaineenPreIBSuoritus2019',
  ...o
})

LukionOppiaineenPreIBSuoritus2019.className =
  'fi.oph.koski.schema.LukionOppiaineenPreIBSuoritus2019' as const

export const isLukionOppiaineenPreIBSuoritus2019 = (
  a: any
): a is LukionOppiaineenPreIBSuoritus2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionOppiaineenPreIBSuoritus2019'
