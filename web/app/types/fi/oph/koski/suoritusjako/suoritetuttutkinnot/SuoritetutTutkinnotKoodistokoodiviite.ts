import { LocalizedString } from '../../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKoodistokoodiviite
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoodistokoodiviite`
 */
export type SuoritetutTutkinnotKoodistokoodiviite = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoodistokoodiviite'
  koodistoVersio?: number
  koodiarvo: string
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri?: string
}

export const SuoritetutTutkinnotKoodistokoodiviite = (o: {
  koodistoVersio?: number
  koodiarvo: string
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri?: string
}): SuoritetutTutkinnotKoodistokoodiviite => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoodistokoodiviite',
  ...o
})

SuoritetutTutkinnotKoodistokoodiviite.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoodistokoodiviite' as const

export const isSuoritetutTutkinnotKoodistokoodiviite = (
  a: any
): a is SuoritetutTutkinnotKoodistokoodiviite =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoodistokoodiviite'
