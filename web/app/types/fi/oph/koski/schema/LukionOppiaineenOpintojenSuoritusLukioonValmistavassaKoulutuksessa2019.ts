import { LukionOppiaineenArviointi2019 } from './LukionOppiaineenArviointi2019'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOppiaine2019 } from './LukionOppiaine2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 } from './LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019'

/**
 * Lukion oppiaineen 2019 opintojen suoritustiedot LUVA-koulutuksessa
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019`
 */
export type LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =
  {
    $class: 'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019'
    arviointi?: Array<LukionOppiaineenArviointi2019>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine2019'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suoritettuErityisenäTutkintona: boolean
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2019
    osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
  }

export const LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =
  (o: {
    arviointi?: Array<LukionOppiaineenArviointi2019>
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine2019'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suoritettuErityisenäTutkintona?: boolean
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2019
    osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
  }): LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'luvalukionoppiaine2019',
      koodistoUri: 'suorituksentyyppi'
    }),
    suoritettuErityisenäTutkintona: false,
    $class:
      'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019',
    ...o
  })

export const isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =
  (
    a: any
  ): a is LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =>
    a?.$class ===
    'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019'
