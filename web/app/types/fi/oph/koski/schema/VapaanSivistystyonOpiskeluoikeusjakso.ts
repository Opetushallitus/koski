import {
  OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso,
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso
} from './OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import {
  VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso,
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso
} from './VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import {
  VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso,
  isVapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso
} from './VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso
} from './VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'

/**
 * VapaanSivistystyönOpiskeluoikeusjakso
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeusjakso`
 */
export type VapaanSivistystyönOpiskeluoikeusjakso =
  | OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso
  | VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso
  | VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso

export const isVapaanSivistystyönOpiskeluoikeusjakso = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeusjakso =>
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(a)
