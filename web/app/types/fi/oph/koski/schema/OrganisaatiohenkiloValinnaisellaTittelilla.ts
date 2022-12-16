import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'

/**
 * Henkilö- ja organisaatiotiedot, mahdollisesti titteli
 *
 * @see `fi.oph.koski.schema.OrganisaatiohenkilöValinnaisellaTittelillä`
 */
export type OrganisaatiohenkilöValinnaisellaTittelillä = {
  $class: 'fi.oph.koski.schema.OrganisaatiohenkilöValinnaisellaTittelillä'
  nimi: string
  titteli?: LocalizedString
  organisaatio: Organisaatio
}

export const OrganisaatiohenkilöValinnaisellaTittelillä = (o: {
  nimi: string
  titteli?: LocalizedString
  organisaatio: Organisaatio
}): OrganisaatiohenkilöValinnaisellaTittelillä => ({
  $class: 'fi.oph.koski.schema.OrganisaatiohenkilöValinnaisellaTittelillä',
  ...o
})

export const isOrganisaatiohenkilöValinnaisellaTittelillä = (
  a: any
): a is OrganisaatiohenkilöValinnaisellaTittelillä =>
  a?.$class === 'OrganisaatiohenkilöValinnaisellaTittelillä'
