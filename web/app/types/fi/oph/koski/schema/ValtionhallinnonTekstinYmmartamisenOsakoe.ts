import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonTekstinYmmärtämisenOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonTekstinYmmärtämisenOsakoe`
 */
export type ValtionhallinnonTekstinYmmärtämisenOsakoe = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonTekstinYmmärtämisenOsakoe'
  tunniste: Koodistokoodiviite<'vktosakoe', 'tekstinymmartaminen'>
}

export const ValtionhallinnonTekstinYmmärtämisenOsakoe = (
  o: {
    tunniste?: Koodistokoodiviite<'vktosakoe', 'tekstinymmartaminen'>
  } = {}
): ValtionhallinnonTekstinYmmärtämisenOsakoe => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonTekstinYmmärtämisenOsakoe',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'tekstinymmartaminen',
    koodistoUri: 'vktosakoe'
  }),
  ...o
})

ValtionhallinnonTekstinYmmärtämisenOsakoe.className =
  'fi.oph.koski.schema.ValtionhallinnonTekstinYmmärtämisenOsakoe' as const

export const isValtionhallinnonTekstinYmmärtämisenOsakoe = (
  a: any
): a is ValtionhallinnonTekstinYmmärtämisenOsakoe =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonTekstinYmmärtämisenOsakoe'
