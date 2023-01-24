import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'

/**
 * Henkilö- ja organisaatiotiedot
 *
 * @see `fi.oph.koski.schema.Organisaatiohenkilö`
 */
export type Organisaatiohenkilö = {
  $class: 'fi.oph.koski.schema.Organisaatiohenkilö'
  nimi: string
  titteli: LocalizedString
  organisaatio: Organisaatio
}

export const Organisaatiohenkilö = (o: {
  nimi: string
  titteli: LocalizedString
  organisaatio: Organisaatio
}): Organisaatiohenkilö => ({
  $class: 'fi.oph.koski.schema.Organisaatiohenkilö',
  ...o
})

Organisaatiohenkilö.className =
  'fi.oph.koski.schema.Organisaatiohenkilö' as const

export const isOrganisaatiohenkilö = (a: any): a is Organisaatiohenkilö =>
  a?.$class === 'fi.oph.koski.schema.Organisaatiohenkilö'
