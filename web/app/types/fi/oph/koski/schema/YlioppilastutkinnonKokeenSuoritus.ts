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
  keskeytynyt?: boolean
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutusmoduuli: YlioppilasTutkinnonKoe
  tutkintokokonaisuudenTunniste?: number
  maksuton?: boolean
}

export const YlioppilastutkinnonKokeenSuoritus = (o: {
  arviointi?: Array<YlioppilaskokeenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinnonkoe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  keskeytynyt?: boolean
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutusmoduuli: YlioppilasTutkinnonKoe
  tutkintokokonaisuudenTunniste?: number
  maksuton?: boolean
}): YlioppilastutkinnonKokeenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ylioppilastutkinnonkoe',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonKokeenSuoritus',
  ...o
})

YlioppilastutkinnonKokeenSuoritus.className =
  'fi.oph.koski.schema.YlioppilastutkinnonKokeenSuoritus' as const

export const isYlioppilastutkinnonKokeenSuoritus = (
  a: any
): a is YlioppilastutkinnonKokeenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonKokeenSuoritus'
