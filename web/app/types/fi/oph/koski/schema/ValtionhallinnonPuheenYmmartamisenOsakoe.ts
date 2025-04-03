import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonPuheenYmmärtämisenOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonPuheenYmmärtämisenOsakoe`
 */
export type ValtionhallinnonPuheenYmmärtämisenOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonPuheenYmmärtämisenOsakoe'
  tunniste: Koodistokoodiviite<'vktosakoe', 'puheenymmartaminen'>
}

export const ValtionhallinnonPuheenYmmärtämisenOsakoe = (
  o: {
    tunniste?: Koodistokoodiviite<'vktosakoe', 'puheenymmartaminen'>
  } = {}
): ValtionhallinnonPuheenYmmärtämisenOsakoe => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonPuheenYmmärtämisenOsakoe',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'puheenymmartaminen',
    koodistoUri: 'vktosakoe'
  }),
  ...o
})

ValtionhallinnonPuheenYmmärtämisenOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonPuheenYmmärtämisenOsakoe' as const

export const isValtionhallinnonPuheenYmmärtämisenOsakoe = (
  a: any
): a is ValtionhallinnonPuheenYmmärtämisenOsakoe =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonPuheenYmmärtämisenOsakoe'
