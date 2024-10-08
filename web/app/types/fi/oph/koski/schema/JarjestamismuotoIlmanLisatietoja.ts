import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Järjestämismuoto ilman lisätietoja
 *
 * @see `fi.oph.koski.schema.JärjestämismuotoIlmanLisätietoja`
 */
export type JärjestämismuotoIlmanLisätietoja = {
  $class: 'fi.oph.koski.schema.JärjestämismuotoIlmanLisätietoja'
  tunniste: Koodistokoodiviite<'jarjestamismuoto', string>
}

export const JärjestämismuotoIlmanLisätietoja = (o: {
  tunniste: Koodistokoodiviite<'jarjestamismuoto', string>
}): JärjestämismuotoIlmanLisätietoja => ({
  $class: 'fi.oph.koski.schema.JärjestämismuotoIlmanLisätietoja',
  ...o
})

JärjestämismuotoIlmanLisätietoja.className =
  'fi.oph.koski.schema.JärjestämismuotoIlmanLisätietoja' as const

export const isJärjestämismuotoIlmanLisätietoja = (
  a: any
): a is JärjestämismuotoIlmanLisätietoja =>
  a?.$class === 'fi.oph.koski.schema.JärjestämismuotoIlmanLisätietoja'
