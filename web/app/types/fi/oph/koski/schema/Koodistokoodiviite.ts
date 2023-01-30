import { LocalizedString } from './LocalizedString'

/**
 * Koodistokoodiviite
 *
 * @see `fi.oph.koski.schema.Koodistokoodiviite`
 */
export type Koodistokoodiviite<
  U extends string = string,
  A extends string = string
> = {
  $class: 'fi.oph.koski.schema.Koodistokoodiviite'
  koodistoVersio?: number
  koodiarvo: A
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri: U
}

export const Koodistokoodiviite = <
  U extends string = string,
  A extends string = string
>(o: {
  koodistoVersio?: number
  koodiarvo: A
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri: U
}): Koodistokoodiviite<U, A> => ({
  $class: 'fi.oph.koski.schema.Koodistokoodiviite',
  ...o
})

Koodistokoodiviite.className = 'fi.oph.koski.schema.Koodistokoodiviite' as const

export const isKoodistokoodiviite = (a: any): a is Koodistokoodiviite =>
  a?.$class === 'fi.oph.koski.schema.Koodistokoodiviite'
