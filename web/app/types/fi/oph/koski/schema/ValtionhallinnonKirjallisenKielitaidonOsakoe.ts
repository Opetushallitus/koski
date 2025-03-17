import {
  ValtionhallinnonKirjoittamisenOsakoe,
  isValtionhallinnonKirjoittamisenOsakoe
} from './ValtionhallinnonKirjoittamisenOsakoe'
import {
  ValtionhallinnonTekstinYmmärtämisenOsakoe,
  isValtionhallinnonTekstinYmmärtämisenOsakoe
} from './ValtionhallinnonTekstinYmmartamisenOsakoe'

/**
 * ValtionhallinnonKirjallisenKielitaidonOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKirjallisenKielitaidonOsakoe`
 */
export type ValtionhallinnonKirjallisenKielitaidonOsakoe =
  | ValtionhallinnonKirjoittamisenOsakoe
  | ValtionhallinnonTekstinYmmärtämisenOsakoe

export const isValtionhallinnonKirjallisenKielitaidonOsakoe = (
  a: any
): a is ValtionhallinnonKirjallisenKielitaidonOsakoe =>
  isValtionhallinnonKirjoittamisenOsakoe(a) ||
  isValtionhallinnonTekstinYmmärtämisenOsakoe(a)
