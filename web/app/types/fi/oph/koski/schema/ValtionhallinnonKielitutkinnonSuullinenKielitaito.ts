import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKielitutkinnonSuullinenKielitaito
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullinenKielitaito`
 */
export type ValtionhallinnonKielitutkinnonSuullinenKielitaito = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullinenKielitaito'
  tunniste: Koodistokoodiviite<'vktkielitaito', 'suullinen'>
}

export const ValtionhallinnonKielitutkinnonSuullinenKielitaito = (
  o: {
    tunniste?: Koodistokoodiviite<'vktkielitaito', 'suullinen'>
  } = {}
): ValtionhallinnonKielitutkinnonSuullinenKielitaito => ({
  $class:
    'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullinenKielitaito',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'suullinen',
    koodistoUri: 'vktkielitaito'
  }),
  ...o
})

ValtionhallinnonKielitutkinnonSuullinenKielitaito.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullinenKielitaito' as const

export const isValtionhallinnonKielitutkinnonSuullinenKielitaito = (
  a: any
): a is ValtionhallinnonKielitutkinnonSuullinenKielitaito =>
  a?.$class ===
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonSuullinenKielitaito'
