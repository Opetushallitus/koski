import {
  ValtionhallinnonPuheenYmmärtämisenOsakoe,
  isValtionhallinnonPuheenYmmärtämisenOsakoe
} from './ValtionhallinnonPuheenYmmartamisenOsakoe'
import {
  ValtionhallinnonTekstinYmmärtämisenOsakoe,
  isValtionhallinnonTekstinYmmärtämisenOsakoe
} from './ValtionhallinnonTekstinYmmartamisenOsakoe'

/**
 * ValtionhallinnonYmmärtämisenKielitaidonOsakoe
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonYmmärtämisenKielitaidonOsakoe`
 */
export type ValtionhallinnonYmmärtämisenKielitaidonOsakoe =
  | ValtionhallinnonPuheenYmmärtämisenOsakoe
  | ValtionhallinnonTekstinYmmärtämisenOsakoe

export const isValtionhallinnonYmmärtämisenKielitaidonOsakoe = (
  a: any
): a is ValtionhallinnonYmmärtämisenKielitaidonOsakoe =>
  isValtionhallinnonPuheenYmmärtämisenOsakoe(a) ||
  isValtionhallinnonTekstinYmmärtämisenOsakoe(a)
