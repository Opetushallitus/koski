import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKirjallisenKielitutkinnonOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKirjallisenKielitutkinnonOsakoe`
 */
export type ValtionhallinnonKirjallisenKielitutkinnonOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKirjallisenKielitutkinnonOsakoe'
  tunniste: Koodistokoodiviite<
    'vktosakoe',
    'kirjoittaminen' | 'tekstinymmartaminen'
  >
}

export const ValtionhallinnonKirjallisenKielitutkinnonOsakoe = (o: {
  tunniste: Koodistokoodiviite<
    'vktosakoe',
    'kirjoittaminen' | 'tekstinymmartaminen'
  >
}): ValtionhallinnonKirjallisenKielitutkinnonOsakoe => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonKirjallisenKielitutkinnonOsakoe',
  ...o
})

ValtionhallinnonKirjallisenKielitutkinnonOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonKirjallisenKielitutkinnonOsakoe' as const

export const isValtionhallinnonKirjallisenKielitutkinnonOsakoe = (
  a: any
): a is ValtionhallinnonKirjallisenKielitutkinnonOsakoe =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKirjallisenKielitutkinnonOsakoe'
