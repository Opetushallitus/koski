import { YlioppilaskokeenArviointi } from './YlioppilaskokeenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { YlioppilasTutkinnonKoe } from './YlioppilasTutkinnonKoe'
import { YlioppilastutkinnonTutkintokerta } from './YlioppilastutkinnonTutkintokerta'

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
  koulutusmoduuli: YlioppilasTutkinnonKoe
  tutkintokokonaisuudenTunniste?: number
  maksuton?: boolean
  keskeytynyt?: boolean
  tutkintokerta: YlioppilastutkinnonTutkintokerta
}

export const YlioppilastutkinnonKokeenSuoritus = (o: {
  arviointi?: Array<YlioppilaskokeenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinnonkoe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: YlioppilasTutkinnonKoe
  tutkintokokonaisuudenTunniste?: number
  maksuton?: boolean
  keskeytynyt?: boolean
  tutkintokerta: YlioppilastutkinnonTutkintokerta
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
