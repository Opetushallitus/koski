import {
  HenkilövahvistusPaikkakunnalla,
  isHenkilövahvistusPaikkakunnalla
} from './HenkilovahvistusPaikkakunnalla'
import {
  HenkilövahvistusValinnaisellaPaikkakunnalla,
  isHenkilövahvistusValinnaisellaPaikkakunnalla
} from './HenkilovahvistusValinnaisellaPaikkakunnalla'
import {
  HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla,
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
} from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'
import {
  Organisaatiovahvistus,
  isOrganisaatiovahvistus
} from './Organisaatiovahvistus'
import {
  Päivämäärävahvistus,
  isPäivämäärävahvistus
} from './Paivamaaravahvistus'

/**
 * Vahvistus
 *
 * @see `fi.oph.koski.schema.Vahvistus`
 */
export type Vahvistus =
  | HenkilövahvistusPaikkakunnalla
  | HenkilövahvistusValinnaisellaPaikkakunnalla
  | HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
  | Organisaatiovahvistus
  | Päivämäärävahvistus

export const isVahvistus = (a: any): a is Vahvistus =>
  isHenkilövahvistusPaikkakunnalla(a) ||
  isHenkilövahvistusValinnaisellaPaikkakunnalla(a) ||
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(a) ||
  isOrganisaatiovahvistus(a) ||
  isPäivämäärävahvistus(a)
