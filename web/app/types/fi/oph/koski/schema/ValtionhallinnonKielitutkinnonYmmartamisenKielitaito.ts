import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito`
 */
export type ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito'
  tunniste: Koodistokoodiviite<'vktkielitaito', 'ymmartaminen'>
}

export const ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito = (
  o: {
    tunniste?: Koodistokoodiviite<'vktkielitaito', 'ymmartaminen'>
  } = {}
): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito => ({
  $class:
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'ymmartaminen',
    koodistoUri: 'vktkielitaito'
  }),
  ...o
})

ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito' as const

export const isValtionhallinnonKielitutkinnonYmmärtämisenKielitaito = (
  a: any
): a is ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito'
