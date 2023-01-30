import { LocalizedString } from './LocalizedString'

/**
 * SynteettinenKoodiviite
 *
 * @see `fi.oph.koski.schema.SynteettinenKoodiviite`
 */
export type SynteettinenKoodiviite = {
  $class: 'fi.oph.koski.schema.SynteettinenKoodiviite'
  koodiarvo: string
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri: string
}

export const SynteettinenKoodiviite = (o: {
  koodiarvo: string
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri: string
}): SynteettinenKoodiviite => ({
  $class: 'fi.oph.koski.schema.SynteettinenKoodiviite',
  ...o
})

SynteettinenKoodiviite.className =
  'fi.oph.koski.schema.SynteettinenKoodiviite' as const

export const isSynteettinenKoodiviite = (a: any): a is SynteettinenKoodiviite =>
  a?.$class === 'fi.oph.koski.schema.SynteettinenKoodiviite'
