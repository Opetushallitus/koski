import { LukionArviointi } from './LukionArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukioonValmistavanKoulutuksenKurssi } from './LukioonValmistavanKoulutuksenKurssi'

/**
 * LukioonValmistavanKurssinSuoritus
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKurssinSuoritus`
 */
export type LukioonValmistavanKurssinSuoritus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavanKurssinSuoritus'
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvakurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
}

export const LukioonValmistavanKurssinSuoritus = (o: {
  arviointi?: Array<LukionArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'luvakurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
}): LukioonValmistavanKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'luvakurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukioonValmistavanKurssinSuoritus',
  ...o
})

export const isLukioonValmistavanKurssinSuoritus = (
  a: any
): a is LukioonValmistavanKurssinSuoritus =>
  a?.$class === 'LukioonValmistavanKurssinSuoritus'
