import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YleisenKielitutkinnonOsakoe } from './YleisenKielitutkinnonOsakoe'
import { YleisenKielitutkinnonOsakokeenArviointi } from './YleisenKielitutkinnonOsakokeenArviointi'

/**
 * YleisenKielitutkinnonOsakokeenSuoritus
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenSuoritus`
 */
export type YleisenKielitutkinnonOsakokeenSuoritus = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'yleisenkielitutkinnonosa'>
  koulutusmoduuli: YleisenKielitutkinnonOsakoe
  arviointi?: Array<YleisenKielitutkinnonOsakokeenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const YleisenKielitutkinnonOsakokeenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'yleisenkielitutkinnonosa'>
  koulutusmoduuli: YleisenKielitutkinnonOsakoe
  arviointi?: Array<YleisenKielitutkinnonOsakokeenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): YleisenKielitutkinnonOsakokeenSuoritus => ({
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'yleisenkielitutkinnonosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

YleisenKielitutkinnonOsakokeenSuoritus.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenSuoritus' as const

export const isYleisenKielitutkinnonOsakokeenSuoritus = (
  a: any
): a is YleisenKielitutkinnonOsakokeenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonOsakokeenSuoritus'
