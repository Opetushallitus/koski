import { Koulutustoimija, isKoulutustoimija } from './Koulutustoimija'
import { OidOrganisaatio, isOidOrganisaatio } from './OidOrganisaatio'
import { Oppilaitos, isOppilaitos } from './Oppilaitos'
import { Toimipiste, isToimipiste } from './Toimipiste'
import { Tutkintotoimikunta, isTutkintotoimikunta } from './Tutkintotoimikunta'
import { Yritys, isYritys } from './Yritys'

/**
 * Organisaatio
 *
 * @see `fi.oph.koski.schema.Organisaatio`
 */
export type Organisaatio =
  | Koulutustoimija
  | OidOrganisaatio
  | Oppilaitos
  | Toimipiste
  | Tutkintotoimikunta
  | Yritys

export const isOrganisaatio = (a: any): a is Organisaatio =>
  isKoulutustoimija(a) ||
  isOidOrganisaatio(a) ||
  isOppilaitos(a) ||
  isToimipiste(a) ||
  isTutkintotoimikunta(a) ||
  isYritys(a)
