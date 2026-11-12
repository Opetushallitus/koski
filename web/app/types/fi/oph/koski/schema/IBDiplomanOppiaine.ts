import {
  IBDiplomanKieliOppiaine,
  isIBDiplomanKieliOppiaine
} from './IBDiplomanKieliOppiaine'
import {
  IBDiplomanMuuOppiaine,
  isIBDiplomanMuuOppiaine
} from './IBDiplomanMuuOppiaine'

/**
 * IBDiplomanOppiaine
 *
 * @see `fi.oph.koski.schema.IBDiplomanOppiaine`
 */
export type IBDiplomanOppiaine = IBDiplomanKieliOppiaine | IBDiplomanMuuOppiaine

export const isIBDiplomanOppiaine = (a: any): a is IBDiplomanOppiaine =>
  isIBDiplomanKieliOppiaine(a) || isIBDiplomanMuuOppiaine(a)
