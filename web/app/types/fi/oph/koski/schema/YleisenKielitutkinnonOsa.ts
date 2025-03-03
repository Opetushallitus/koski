import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * YleisenKielitutkinnonOsa
 *
 * @see `fi.oph.koski.schema.YleisenKielitutkinnonOsa`
 */
export type YleisenKielitutkinnonOsa = {
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsa'
  tunniste: Koodistokoodiviite<'ykisuorituksenosa', string>
}

export const YleisenKielitutkinnonOsa = (o: {
  tunniste: Koodistokoodiviite<'ykisuorituksenosa', string>
}): YleisenKielitutkinnonOsa => ({
  $class: 'fi.oph.koski.schema.YleisenKielitutkinnonOsa',
  ...o
})

YleisenKielitutkinnonOsa.className =
  'fi.oph.koski.schema.YleisenKielitutkinnonOsa' as const

export const isYleisenKielitutkinnonOsa = (
  a: any
): a is YleisenKielitutkinnonOsa =>
  a?.$class === 'fi.oph.koski.schema.YleisenKielitutkinnonOsa'
