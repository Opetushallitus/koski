import * as Eq from 'fp-ts/Eq'
import { localize } from '../i18n/i18n'
import {
  isLocalizedString,
  LocalizedString
} from '../types/fi/oph/koski/schema/LocalizedString'
import { Organisaatio } from '../types/fi/oph/koski/schema/Organisaatio'
import { Organisaatiohenkilö } from '../types/fi/oph/koski/schema/Organisaatiohenkilo'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from '../types/fi/oph/koski/schema/OrganisaatiohenkiloValinnaisellaTittelilla'
import { getOrganisaatioId, OrganisaatioEq } from './organisaatiot'
import { ClassOf } from './types'

export type AnyOrganisaatiohenkilö =
  | Organisaatiohenkilö
  | OrganisaatiohenkilöValinnaisellaTittelillä

export const createOrganisaatiohenkilö = (
  henkilöClass: ClassOf<AnyOrganisaatiohenkilö>,
  organisaatio: Organisaatio,
  nimi = '',
  titteli?: string | LocalizedString
): AnyOrganisaatiohenkilö =>
  henkilöClass === 'fi.oph.koski.schema.Organisaatiohenkilö'
    ? Organisaatiohenkilö({
        nimi,
        titteli: isLocalizedString(titteli) ? titteli : localize(titteli || ''),
        organisaatio
      })
    : OrganisaatiohenkilöValinnaisellaTittelillä({
        nimi,
        titteli:
          titteli === undefined
            ? undefined
            : isLocalizedString(titteli)
            ? titteli
            : localize(titteli),
        organisaatio
      })

export const castOrganisaatiohenkilö =
  <T extends AnyOrganisaatiohenkilö>(className: ClassOf<T>) =>
  (henkilö: AnyOrganisaatiohenkilö): T =>
    henkilö.$class === className
      ? (henkilö as T)
      : (createOrganisaatiohenkilö(
          className,
          henkilö.organisaatio,
          henkilö.nimi,
          henkilö.titteli
        ) as T)

export const OrganisaatiohenkilöEq: Eq.Eq<AnyOrganisaatiohenkilö> = {
  equals(x, y) {
    return (
      x.nimi === y.nimi && OrganisaatioEq.equals(x.organisaatio, y.organisaatio)
    )
  }
}
