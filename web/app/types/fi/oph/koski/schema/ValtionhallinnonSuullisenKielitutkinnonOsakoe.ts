import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonSuullisenKielitutkinnonOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonSuullisenKielitutkinnonOsakoe`
 */
export type ValtionhallinnonSuullisenKielitutkinnonOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonSuullisenKielitutkinnonOsakoe'
  tunniste: Koodistokoodiviite<'vktosakoe', 'puhuminen' | 'puheenymmartaminen'>
}

export const ValtionhallinnonSuullisenKielitutkinnonOsakoe = (o: {
  tunniste: Koodistokoodiviite<'vktosakoe', 'puhuminen' | 'puheenymmartaminen'>
}): ValtionhallinnonSuullisenKielitutkinnonOsakoe => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonSuullisenKielitutkinnonOsakoe',
  ...o
})

ValtionhallinnonSuullisenKielitutkinnonOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonSuullisenKielitutkinnonOsakoe' as const

export const isValtionhallinnonSuullisenKielitutkinnonOsakoe = (
  a: any
): a is ValtionhallinnonSuullisenKielitutkinnonOsakoe =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonSuullisenKielitutkinnonOsakoe'
