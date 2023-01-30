import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Esiopetuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.Esiopetus`
 */
export type Esiopetus = {
  $class: 'fi.oph.koski.schema.Esiopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '001101' | '001102'>
  kuvaus?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const Esiopetus = (o: {
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '001101' | '001102'>
  kuvaus?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}): Esiopetus => ({ $class: 'fi.oph.koski.schema.Esiopetus', ...o })

Esiopetus.className = 'fi.oph.koski.schema.Esiopetus' as const

export const isEsiopetus = (a: any): a is Esiopetus =>
  a?.$class === 'fi.oph.koski.schema.Esiopetus'
