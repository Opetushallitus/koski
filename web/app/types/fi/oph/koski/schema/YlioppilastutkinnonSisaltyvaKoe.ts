import { YlioppilasTutkinnonKoe } from './YlioppilasTutkinnonKoe'
import { YlioppilastutkinnonTutkintokerta } from './YlioppilastutkinnonTutkintokerta'

/**
 * Tiedot aiemmin suoritetusta kokeesta, joka on sisällytetty uuteen ylioppilastutkintoon
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonSisältyväKoe`
 */
export type YlioppilastutkinnonSisältyväKoe = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonSisältyväKoe'
  koulutusmoduuli: YlioppilasTutkinnonKoe
  tutkintokerta: YlioppilastutkinnonTutkintokerta
}

export const YlioppilastutkinnonSisältyväKoe = (o: {
  koulutusmoduuli: YlioppilasTutkinnonKoe
  tutkintokerta: YlioppilastutkinnonTutkintokerta
}): YlioppilastutkinnonSisältyväKoe => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonSisältyväKoe',
  ...o
})

YlioppilastutkinnonSisältyväKoe.className =
  'fi.oph.koski.schema.YlioppilastutkinnonSisältyväKoe' as const

export const isYlioppilastutkinnonSisältyväKoe = (
  a: any
): a is YlioppilastutkinnonSisältyväKoe =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonSisältyväKoe'
