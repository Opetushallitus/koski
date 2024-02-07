import * as Eq from 'fp-ts/Eq'
import * as string from 'fp-ts/string'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Koulutustoimija } from '../types/fi/oph/koski/schema/Koulutustoimija'
import { OidOrganisaatio } from '../types/fi/oph/koski/schema/OidOrganisaatio'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { Organisaatio } from '../types/fi/oph/koski/schema/Organisaatio'
import { Toimipiste } from '../types/fi/oph/koski/schema/Toimipiste'
import { isTutkintotoimikunta } from '../types/fi/oph/koski/schema/Tutkintotoimikunta'
import { isYritys } from '../types/fi/oph/koski/schema/Yritys'
import { intersects } from './fp/arrays'
import { isKoodistoOf } from './types'

const Oppilaitostyyppi = {
  koulutustoimija: 'KOULUTUSTOIMIJA',
  oppilaitos: 'OPPILAITOS',
  toimipiste: 'TOIMIPISTE',
  oppisopimustoimipiste: 'OPPISOPIMUSTOIMIPISTE',
  varhaiskasvatuksenToimipaikka: 'VARHAISKASVATUKSEN_TOIMIPAIKKA',
  varhaiskasvatuksenJärjestäjä: 'VARHAISKASVATUKSEN_JARJESTAJA',
  kunta: 'KUNTA'
}

const organisaatiotyyppiIsIncludedIn =
  (tyypit: string[]) => (org: OrganisaatioHierarkia) =>
    intersects(string.Eq)(tyypit)(org.organisaatiotyypit)

export const isOppilaitos = organisaatiotyyppiIsIncludedIn([
  Oppilaitostyyppi.oppilaitos,
  Oppilaitostyyppi.oppisopimustoimipiste,
  Oppilaitostyyppi.varhaiskasvatuksenToimipaikka
])

export const isKoulutustoimija = organisaatiotyyppiIsIncludedIn([
  Oppilaitostyyppi.koulutustoimija,
  Oppilaitostyyppi.varhaiskasvatuksenJärjestäjä
])

export const isToimipiste = organisaatiotyyppiIsIncludedIn([
  Oppilaitostyyppi.toimipiste
])

export const toOrganisaatio = (org: OrganisaatioHierarkia): Organisaatio => {
  const seed = {
    oid: org.oid,
    nimi: org.nimi,
    kotipaikka: isKoodistoOf('kunta')(org.kotipaikka)
      ? org.kotipaikka
      : undefined
  }

  return isOppilaitos(org)
    ? Oppilaitos({
        ...seed,
        oppilaitosnumero: isKoodistoOf('oppilaitosnumero')(org.oppilaitosnumero)
          ? org.oppilaitosnumero
          : undefined
      })
    : isKoulutustoimija(org)
      ? Koulutustoimija({
          ...seed,
          yTunnus: org.yTunnus
        })
      : isToimipiste(org)
        ? Toimipiste(seed)
        : OidOrganisaatio(seed)
}

export const getOrganisaatioOid = (org: Organisaatio): string | undefined =>
  isYritys(org) || isTutkintotoimikunta(org) ? undefined : org.oid

export const getOrganisaatioId = (org: Organisaatio): string =>
  isYritys(org)
    ? org.yTunnus
    : isTutkintotoimikunta(org)
      ? org.tutkintotoimikunnanNumero
      : org.oid

export const getOrganisaationKotipaikka = (
  org: Organisaatio
): Koodistokoodiviite<'kunta'> | undefined =>
  isYritys(org) || isTutkintotoimikunta(org) ? undefined : org.kotipaikka

export const OrganisaatioEq: Eq.Eq<Organisaatio> = {
  equals(x, y) {
    return getOrganisaatioId(x) === getOrganisaatioId(y)
  }
}
