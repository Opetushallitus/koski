import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * OrganisaatioHierarkia
 *
 * @see `fi.oph.koski.organisaatio.OrganisaatioHierarkia`
 */
export type OrganisaatioHierarkia = {
  $class: 'fi.oph.koski.organisaatio.OrganisaatioHierarkia'
  oppilaitostyyppi?: string
  parentOidPath: Array<string>
  kielikoodit: Array<string>
  kotipaikka?: Koodistokoodiviite
  yTunnus?: string
  oid: string
  aktiivinen: boolean
  nimi: LocalizedString
  organisaatiotyypit: Array<string>
  children: Array<OrganisaatioHierarkia>
  oppilaitosnumero?: Koodistokoodiviite
}

export const OrganisaatioHierarkia = (o: {
  oppilaitostyyppi?: string
  parentOidPath?: Array<string>
  kielikoodit?: Array<string>
  kotipaikka?: Koodistokoodiviite
  yTunnus?: string
  oid: string
  aktiivinen: boolean
  nimi: LocalizedString
  organisaatiotyypit?: Array<string>
  children?: Array<OrganisaatioHierarkia>
  oppilaitosnumero?: Koodistokoodiviite
}): OrganisaatioHierarkia => ({
  parentOidPath: [],
  kielikoodit: [],
  organisaatiotyypit: [],
  $class: 'fi.oph.koski.organisaatio.OrganisaatioHierarkia',
  children: [],
  ...o
})

OrganisaatioHierarkia.className =
  'fi.oph.koski.organisaatio.OrganisaatioHierarkia' as const

export const isOrganisaatioHierarkia = (a: any): a is OrganisaatioHierarkia =>
  a?.$class === 'fi.oph.koski.organisaatio.OrganisaatioHierarkia'
