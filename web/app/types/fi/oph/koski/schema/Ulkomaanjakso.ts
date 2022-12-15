import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opintoihin liittyvien ulkomaanjaksojen tiedot. Ulkomaanjakson tiedot sisältävät alku- ja loppupäivämäärät, tiedon siitä, missä maassa jakso on suoritettu, sekä kuvauksen jakson sisällöstä.
 *
 * @see `fi.oph.koski.schema.Ulkomaanjakso`
 */
export type Ulkomaanjakso = {
  $class: 'fi.oph.koski.schema.Ulkomaanjakso'
  alku: string
  loppu?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  kuvaus: LocalizedString
}

export const Ulkomaanjakso = (o: {
  alku: string
  loppu?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  kuvaus: LocalizedString
}): Ulkomaanjakso => ({ $class: 'fi.oph.koski.schema.Ulkomaanjakso', ...o })

export const isUlkomaanjakso = (a: any): a is Ulkomaanjakso =>
  a?.$class === 'Ulkomaanjakso'
