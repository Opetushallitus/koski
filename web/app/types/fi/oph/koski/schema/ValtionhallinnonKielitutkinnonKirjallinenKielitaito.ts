import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKielitutkinnonKirjallinenKielitaito
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallinenKielitaito`
 */
export type ValtionhallinnonKielitutkinnonKirjallinenKielitaito = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallinenKielitaito'
  tunniste: Koodistokoodiviite<'vktkielitaito', 'kirjallinen'>
}

export const ValtionhallinnonKielitutkinnonKirjallinenKielitaito = (
  o: {
    tunniste?: Koodistokoodiviite<'vktkielitaito', 'kirjallinen'>
  } = {}
): ValtionhallinnonKielitutkinnonKirjallinenKielitaito => ({
  $class:
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallinenKielitaito',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'kirjallinen',
    koodistoUri: 'vktkielitaito'
  }),
  ...o
})

ValtionhallinnonKielitutkinnonKirjallinenKielitaito.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallinenKielitaito' as const

export const isValtionhallinnonKielitutkinnonKirjallinenKielitaito = (
  a: any
): a is ValtionhallinnonKielitutkinnonKirjallinenKielitaito =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKirjallinenKielitaito'
