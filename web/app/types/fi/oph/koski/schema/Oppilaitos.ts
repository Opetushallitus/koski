import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio
 *
 * @see `fi.oph.koski.schema.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.schema.Oppilaitos'
  oid: string
  oppilaitosnumero?: Koodistokoodiviite<'oppilaitosnumero', string>
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export const Oppilaitos = (o: {
  oid: string
  oppilaitosnumero?: Koodistokoodiviite<'oppilaitosnumero', string>
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}): Oppilaitos => ({ $class: 'fi.oph.koski.schema.Oppilaitos', ...o })

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'fi.oph.koski.schema.Oppilaitos'
