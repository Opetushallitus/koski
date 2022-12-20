import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ylioppilastutkinnon kokeen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.YlioppilasTutkinnonKoe`
 */
export type YlioppilasTutkinnonKoe = {
  $class: 'fi.oph.koski.schema.YlioppilasTutkinnonKoe'
  tunniste: Koodistokoodiviite<'koskiyokokeet', string>
}

export const YlioppilasTutkinnonKoe = (o: {
  tunniste: Koodistokoodiviite<'koskiyokokeet', string>
}): YlioppilasTutkinnonKoe => ({
  $class: 'fi.oph.koski.schema.YlioppilasTutkinnonKoe',
  ...o
})

export const isYlioppilasTutkinnonKoe = (a: any): a is YlioppilasTutkinnonKoe =>
  a?.$class === 'fi.oph.koski.schema.YlioppilasTutkinnonKoe'
