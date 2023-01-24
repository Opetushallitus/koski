import { LukionOppiaineenArviointi } from './LukionOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukioonValmistavanKoulutuksenOppiaine } from './LukioonValmistavanKoulutuksenOppiaine'
import { LukioonValmistavanKurssinSuoritus } from './LukioonValmistavanKurssinSuoritus'

/**
 * Lukioon valmistavan koulutuksen oppiaineen suoritustiedot LUVA-koulutuksessa
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenOppiaineenSuoritus`
 */
export type LukioonValmistavanKoulutuksenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOppiaineenSuoritus'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenOppiaine
  osasuoritukset?: Array<LukioonValmistavanKurssinSuoritus>
}

export const LukioonValmistavanKoulutuksenOppiaineenSuoritus = (o: {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'luvaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenOppiaine
  osasuoritukset?: Array<LukioonValmistavanKurssinSuoritus>
}): LukioonValmistavanKoulutuksenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'luvaoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOppiaineenSuoritus',
  ...o
})

LukioonValmistavanKoulutuksenOppiaineenSuoritus.className =
  'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOppiaineenSuoritus' as const

export const isLukioonValmistavanKoulutuksenOppiaineenSuoritus = (
  a: any
): a is LukioonValmistavanKoulutuksenOppiaineenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOppiaineenSuoritus'
