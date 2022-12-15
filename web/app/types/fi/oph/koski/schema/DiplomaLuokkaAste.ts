import {
  IBDiplomaLuokkaAste,
  isIBDiplomaLuokkaAste
} from './IBDiplomaLuokkaAste'
import {
  ISHDiplomaLuokkaAste,
  isISHDiplomaLuokkaAste
} from './ISHDiplomaLuokkaAste'

/**
 * DiplomaLuokkaAste
 *
 * @see `fi.oph.koski.schema.DiplomaLuokkaAste`
 */
export type DiplomaLuokkaAste = IBDiplomaLuokkaAste | ISHDiplomaLuokkaAste

export const isDiplomaLuokkaAste = (a: any): a is DiplomaLuokkaAste =>
  isIBDiplomaLuokkaAste(a) || isISHDiplomaLuokkaAste(a)
