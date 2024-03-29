import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ammatillisen näytön suorituspaikka
 *
 * @see `fi.oph.koski.schema.NäytönSuorituspaikka`
 */
export type NäytönSuorituspaikka = {
  $class: 'fi.oph.koski.schema.NäytönSuorituspaikka'
  tunniste: Koodistokoodiviite<'ammatillisennaytonsuorituspaikka', string>
  kuvaus: LocalizedString
}

export const NäytönSuorituspaikka = (o: {
  tunniste: Koodistokoodiviite<'ammatillisennaytonsuorituspaikka', string>
  kuvaus: LocalizedString
}): NäytönSuorituspaikka => ({
  $class: 'fi.oph.koski.schema.NäytönSuorituspaikka',
  ...o
})

NäytönSuorituspaikka.className =
  'fi.oph.koski.schema.NäytönSuorituspaikka' as const

export const isNäytönSuorituspaikka = (a: any): a is NäytönSuorituspaikka =>
  a?.$class === 'fi.oph.koski.schema.NäytönSuorituspaikka'
