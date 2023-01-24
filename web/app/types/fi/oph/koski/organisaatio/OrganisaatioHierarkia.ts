import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * OrganisaatioHierarkia
 *
 * @see `fi.oph.koski.organisaatio.OrganisaatioHierarkia`
 */
export type OrganisaatioHierarkia = {
  $class: 'fi.oph.koski.organisaatio.OrganisaatioHierarkia'
  children: Array<OrganisaatioHierarkia>
  oppilaitosnumero?: Koodistokoodiviite
  oppilaitostyyppi?: string
  kielikoodit: Array<string>
  kotipaikka?: Koodistokoodiviite
  yTunnus?: string
  oid: string
  aktiivinen: boolean
  nimi: LocalizedString
  organisaatiotyypit: Array<string>
}

export const OrganisaatioHierarkia = (o: {
  children?: Array<OrganisaatioHierarkia>
  oppilaitosnumero?: Koodistokoodiviite
  oppilaitostyyppi?: string
  kielikoodit?: Array<string>
  kotipaikka?: Koodistokoodiviite
  yTunnus?: string
  oid: string
  aktiivinen: boolean
  nimi: LocalizedString
  organisaatiotyypit?: Array<string>
}): OrganisaatioHierarkia => ({
  children: [],
  kielikoodit: [],
  organisaatiotyypit: [],
  $class: 'fi.oph.koski.organisaatio.OrganisaatioHierarkia',
  ...o
})

OrganisaatioHierarkia.className =
  'fi.oph.koski.organisaatio.OrganisaatioHierarkia' as const

export const isOrganisaatioHierarkia = (a: any): a is OrganisaatioHierarkia =>
  a?.$class === 'fi.oph.koski.organisaatio.OrganisaatioHierarkia'
