import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opintopolun organisaatiopalvelusta löytyvä oppilaitos-tyyppinen organisaatio
 *
 * @see `fi.oph.koski.schema.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.schema.Oppilaitos'
  oppilaitosnumero?: Koodistokoodiviite<'oppilaitosnumero', string>
  oppilaitostyyppi?: Koodistokoodiviite<'oppilaitostyyppi', string>
  kotipaikka?: Koodistokoodiviite<'kunta', string>
  oid: string
  nimi?: LocalizedString
}

export const Oppilaitos = (o: {
  oppilaitosnumero?: Koodistokoodiviite<'oppilaitosnumero', string>
  oppilaitostyyppi?: Koodistokoodiviite<'oppilaitostyyppi', string>
  kotipaikka?: Koodistokoodiviite<'kunta', string>
  oid: string
  nimi?: LocalizedString
}): Oppilaitos => ({ $class: 'fi.oph.koski.schema.Oppilaitos', ...o })

Oppilaitos.className = 'fi.oph.koski.schema.Oppilaitos' as const

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'fi.oph.koski.schema.Oppilaitos'
