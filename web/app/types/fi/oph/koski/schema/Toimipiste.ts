import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Opintopolun organisaatiopalvelusta löytyvä toimipiste-tyyppinen organisaatio
 *
 * @see `fi.oph.koski.schema.Toimipiste`
 */
export type Toimipiste = {
  $class: 'fi.oph.koski.schema.Toimipiste'
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export const Toimipiste = (o: {
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}): Toimipiste => ({ $class: 'fi.oph.koski.schema.Toimipiste', ...o })

export const isToimipiste = (a: any): a is Toimipiste =>
  a?.$class === 'fi.oph.koski.schema.Toimipiste'
