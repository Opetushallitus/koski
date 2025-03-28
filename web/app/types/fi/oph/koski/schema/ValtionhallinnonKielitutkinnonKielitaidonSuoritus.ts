import {
  ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus,
  isValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus
} from './ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus'
import {
  ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus,
  isValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus
} from './ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus'
import {
  ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus,
  isValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus
} from './ValtionhallinnonKielitutkinnonYmmartamisenKielitaidonSuoritus'

/**
 * ValtionhallinnonKielitutkinnonKielitaidonSuoritus
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonKielitaidonSuoritus`
 */
export type ValtionhallinnonKielitutkinnonKielitaidonSuoritus =
  | ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus
  | ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus
  | ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus

export const isValtionhallinnonKielitutkinnonKielitaidonSuoritus = (
  a: any
): a is ValtionhallinnonKielitutkinnonKielitaidonSuoritus =>
  isValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus(a) ||
  isValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus(a) ||
  isValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus(a)
