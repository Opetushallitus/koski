import { YlioppilaskokeenArviointi } from './YlioppilaskokeenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YlioppilastutkinnonTutkintokerta } from './YlioppilastutkinnonTutkintokerta'
import { YlioppilasTutkinnonKoe } from './YlioppilasTutkinnonKoe'

/**
 * YlioppilastutkinnonKokeenSuoritus
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonKokeenSuoritus`
 */
export type YlioppilastutkinnonKokeenSuoritus = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonKokeenSuoritus'
  arviointi?: Array<YlioppilaskokeenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinnonkoe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutusmoduuli: YlioppilasTutkinnonKoe
}

export const YlioppilastutkinnonKokeenSuoritus = (o: {
  arviointi?: Array<YlioppilaskokeenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinnonkoe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutusmoduuli: YlioppilasTutkinnonKoe
}): YlioppilastutkinnonKokeenSuoritus => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonKokeenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinnonkoe',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isYlioppilastutkinnonKokeenSuoritus = (
  a: any
): a is YlioppilastutkinnonKokeenSuoritus =>
  a?.$class === 'YlioppilastutkinnonKokeenSuoritus'
