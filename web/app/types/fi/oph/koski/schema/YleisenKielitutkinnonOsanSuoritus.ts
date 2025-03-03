import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YleisenKielitutkinnonOsa } from './YleisenKielitutkinnonOsa'
import { YleisenKielitutkinnonOsanArviointi } from './YleisenKielitutkinnonOsanArviointi'

/**
 * YleisenKielitutkinnonOsanSuoritus
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonOsanSuoritus`
 */
export type YleisenKielitutkinnonOsanSuoritus = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsanSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'yleisenkielitutkinnonosa'>
  koulutusmoduuli: YleisenKielitutkinnonOsa
  arviointi?: Array<YleisenKielitutkinnonOsanArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const YleisenKielitutkinnonOsanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'yleisenkielitutkinnonosa'>
  koulutusmoduuli: YleisenKielitutkinnonOsa
  arviointi?: Array<YleisenKielitutkinnonOsanArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): YleisenKielitutkinnonOsanSuoritus => ({
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsanSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'yleisenkielitutkinnonosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

YleisenKielitutkinnonOsanSuoritus.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonOsanSuoritus' as const

export const isYleisenKielitutkinnonOsanSuoritus = (
  a: any
): a is YleisenKielitutkinnonOsanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonOsanSuoritus'
