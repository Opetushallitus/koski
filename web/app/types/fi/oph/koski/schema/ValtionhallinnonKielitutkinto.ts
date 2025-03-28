import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKielitutkinto
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinto`
 */
export type ValtionhallinnonKielitutkinto = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinto'
  tunniste: Koodistokoodiviite<'vkttutkintotaso', string>
  kieli: Koodistokoodiviite<'kieli', string>
}

export const ValtionhallinnonKielitutkinto = (o: {
  tunniste: Koodistokoodiviite<'vkttutkintotaso', string>
  kieli: Koodistokoodiviite<'kieli', string>
}): ValtionhallinnonKielitutkinto => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinto',
  ...o
})

ValtionhallinnonKielitutkinto.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinto' as const

export const isValtionhallinnonKielitutkinto = (
  a: any
): a is ValtionhallinnonKielitutkinto =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonKielitutkinto'
