import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Opintopolun organisaatiopalvelusta löytyvä organisaatio, jonka OID-tunniste on tiedossa
 *
 * @see `fi.oph.koski.schema.OidOrganisaatio`
 */
export type OidOrganisaatio = {
  $class: 'fi.oph.koski.schema.OidOrganisaatio'
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export const OidOrganisaatio = (o: {
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}): OidOrganisaatio => ({ $class: 'fi.oph.koski.schema.OidOrganisaatio', ...o })

export const isOidOrganisaatio = (a: any): a is OidOrganisaatio =>
  a?.$class === 'fi.oph.koski.schema.OidOrganisaatio'
