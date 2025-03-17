import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKirjoittamisenOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKirjoittamisenOsakoe`
 */
export type ValtionhallinnonKirjoittamisenOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKirjoittamisenOsakoe'
  tunniste: Koodistokoodiviite<'vktosakoe', 'kirjoittaminen'>
}

export const ValtionhallinnonKirjoittamisenOsakoe = (
  o: {
    tunniste?: Koodistokoodiviite<'vktosakoe', 'kirjoittaminen'>
  } = {}
): ValtionhallinnonKirjoittamisenOsakoe => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonKirjoittamisenOsakoe',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'kirjoittaminen',
    koodistoUri: 'vktosakoe'
  }),
  ...o
})

ValtionhallinnonKirjoittamisenOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonKirjoittamisenOsakoe' as const

export const isValtionhallinnonKirjoittamisenOsakoe = (
  a: any
): a is ValtionhallinnonKirjoittamisenOsakoe =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonKirjoittamisenOsakoe'
