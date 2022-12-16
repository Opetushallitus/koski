import { Lukiodiplomit2019, isLukiodiplomit2019 } from './Lukiodiplomit2019'
import {
  MuutLukionSuoritukset2019,
  isMuutLukionSuoritukset2019
} from './MuutLukionSuoritukset2019'
import {
  TemaattisetOpinnot2019,
  isTemaattisetOpinnot2019
} from './TemaattisetOpinnot2019'

/**
 * MuutSuorituksetTaiVastaavat2019
 *
 * @see `fi.oph.koski.schema.MuutSuorituksetTaiVastaavat2019`
 */
export type MuutSuorituksetTaiVastaavat2019 =
  | Lukiodiplomit2019
  | MuutLukionSuoritukset2019
  | TemaattisetOpinnot2019

export const isMuutSuorituksetTaiVastaavat2019 = (
  a: any
): a is MuutSuorituksetTaiVastaavat2019 =>
  isLukiodiplomit2019(a) ||
  isMuutLukionSuoritukset2019(a) ||
  isTemaattisetOpinnot2019(a)
