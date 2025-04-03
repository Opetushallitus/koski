import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonPuhumisenOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonPuhumisenOsakoe`
 */
export type ValtionhallinnonPuhumisenOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonPuhumisenOsakoe'
  tunniste: Koodistokoodiviite<'vktosakoe', 'puhuminen'>
}

export const ValtionhallinnonPuhumisenOsakoe = (
  o: {
    tunniste?: Koodistokoodiviite<'vktosakoe', 'puhuminen'>
  } = {}
): ValtionhallinnonPuhumisenOsakoe => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonPuhumisenOsakoe',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'puhuminen',
    koodistoUri: 'vktosakoe'
  }),
  ...o
})

ValtionhallinnonPuhumisenOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonPuhumisenOsakoe' as const

export const isValtionhallinnonPuhumisenOsakoe = (
  a: any
): a is ValtionhallinnonPuhumisenOsakoe =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonPuhumisenOsakoe'
