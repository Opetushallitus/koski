import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe`
 */
export type ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe'
  tunniste: Koodistokoodiviite<
    'vktosakoe',
    'puheenymmartaminen' | 'tekstinymmartaminen'
  >
}

export const ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe = (o: {
  tunniste: Koodistokoodiviite<
    'vktosakoe',
    'puheenymmartaminen' | 'tekstinymmartaminen'
  >
}): ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe => ({
  $class:
    'fi.oph.koski.schema.ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe',
  ...o
})

ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe' as const

export const isValtionhallinnonYmmärtämisenKielitutkinnonOsakoe = (
  a: any
): a is ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonYmmärtämisenKielitutkinnonOsakoe'
