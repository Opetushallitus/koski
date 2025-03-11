import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * YleisenKielitutkinnonOsakoe
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonOsakoe`
 */
export type YleisenKielitutkinnonOsakoe = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsakoe'
  tunniste: Koodistokoodiviite<'ykisuorituksenosa', string>
}

export const YleisenKielitutkinnonOsakoe = (o: {
  tunniste: Koodistokoodiviite<'ykisuorituksenosa', string>
}): YleisenKielitutkinnonOsakoe => ({
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsakoe',
  ...o
})

YleisenKielitutkinnonOsakoe.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonOsakoe' as const

export const isYleisenKielitutkinnonOsakoe = (
  a: any
): a is YleisenKielitutkinnonOsakoe =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonOsakoe'
