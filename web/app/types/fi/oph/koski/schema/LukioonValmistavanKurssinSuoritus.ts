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
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const LukioonValmistavanKurssinSuoritus = (o: {
  arviointi?: Array<LukionArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'luvakurssi'>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): LukioonValmistavanKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'luvakurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukioonValmistavanKurssinSuoritus',
  ...o
})

LukioonValmistavanKurssinSuoritus.className =
  'fi.oph.koski.schema.LukioonValmistavanKurssinSuoritus' as const

export const isLukioonValmistavanKurssinSuoritus = (
  a: any
): a is LukioonValmistavanKurssinSuoritus =>
  a?.$class === 'fi.oph.koski.schema.LukioonValmistavanKurssinSuoritus'
