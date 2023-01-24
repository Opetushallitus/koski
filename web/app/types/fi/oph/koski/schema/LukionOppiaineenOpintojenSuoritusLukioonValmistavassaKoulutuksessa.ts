import { LukionOppiaineenArviointi } from './LukionOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOppiaine2015 } from './LukionOppiaine2015'
import { LukionKurssinSuoritus2015 } from './LukionKurssinSuoritus2015'

/**
 * Lukion oppiaineen opintojen suoritustiedot LUVA-koulutuksessa
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa`
 */
export type LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =
  {
    $class: 'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa'
    arviointi?: Array<LukionOppiaineenArviointi>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2015
    osasuoritukset?: Array<LukionKurssinSuoritus2015>
  }

export const LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =
  (o: {
    arviointi?: Array<LukionOppiaineenArviointi>
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2015
    osasuoritukset?: Array<LukionKurssinSuoritus2015>
  }): LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'luvalukionoppiaine',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa',
    ...o
  })

LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa.className =
  'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa' as const

export const isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =
  (
    a: any
  ): a is LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =>
    a?.$class ===
    'fi.oph.koski.schema.LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa'
