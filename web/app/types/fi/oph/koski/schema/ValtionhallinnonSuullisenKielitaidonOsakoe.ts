import {
  ValtionhallinnonPuheenYmmärtämisenOsakoe,
  isValtionhallinnonPuheenYmmärtämisenOsakoe
} from './ValtionhallinnonPuheenYmmartamisenOsakoe'
import {
  ValtionhallinnonPuhumisenOsakoe,
  isValtionhallinnonPuhumisenOsakoe
} from './ValtionhallinnonPuhumisenOsakoe'

/**
 * ValtionhallinnonSuullisenKielitaidonOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonSuullisenKielitaidonOsakoe`
 */
export type ValtionhallinnonSuullisenKielitaidonOsakoe =
  | ValtionhallinnonPuheenYmmärtämisenOsakoe
  | ValtionhallinnonPuhumisenOsakoe

export const isValtionhallinnonSuullisenKielitaidonOsakoe = (
  a: any
): a is ValtionhallinnonSuullisenKielitaidonOsakoe =>
  isValtionhallinnonPuheenYmmärtämisenOsakoe(a) ||
  isValtionhallinnonPuhumisenOsakoe(a)
