import { Koulutustoimija, isKoulutustoimija } from './Koulutustoimija'
import { OidOrganisaatio, isOidOrganisaatio } from './OidOrganisaatio'
import { Oppilaitos, isOppilaitos } from './Oppilaitos'
import { Toimipiste, isToimipiste } from './Toimipiste'

/**
 * OrganisaatioWithOid
 *
 * @see `fi.oph.koski.schema.OrganisaatioWithOid`
 */
export type OrganisaatioWithOid =
  | Koulutustoimija
  | OidOrganisaatio
  | Oppilaitos
  | Toimipiste

export const isOrganisaatioWithOid = (a: any): a is OrganisaatioWithOid =>
  isKoulutustoimija(a) ||
  isOidOrganisaatio(a) ||
  isOppilaitos(a) ||
  isToimipiste(a)
