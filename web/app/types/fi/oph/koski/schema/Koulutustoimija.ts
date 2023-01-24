import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Opintopolun organisaatiopalvelusta löytyvä koulutustoimija-tyyppinen, oppilaitoksen ylätasolla oleva organisaatio. Tiedon syötössä tietoa ei tarvita; organisaation tiedot haetaan Organisaatiopalvelusta
 *
 * @see `fi.oph.koski.schema.Koulutustoimija`
 */
export type Koulutustoimija = {
  $class: 'fi.oph.koski.schema.Koulutustoimija'
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export const Koulutustoimija = (o: {
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}): Koulutustoimija => ({ $class: 'fi.oph.koski.schema.Koulutustoimija', ...o })

Koulutustoimija.className = 'fi.oph.koski.schema.Koulutustoimija' as const

export const isKoulutustoimija = (a: any): a is Koulutustoimija =>
  a?.$class === 'fi.oph.koski.schema.Koulutustoimija'
