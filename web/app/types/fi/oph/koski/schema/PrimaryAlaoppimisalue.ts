import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PrimaryAlaoppimisalue
 *
 * @see `fi.oph.koski.schema.PrimaryAlaoppimisalue`
 */
export type PrimaryAlaoppimisalue = {
  $class: 'fi.oph.koski.schema.PrimaryAlaoppimisalue'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiprimaryalaoppimisalue',
    string
  >
}

export const PrimaryAlaoppimisalue = (o: {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiprimaryalaoppimisalue',
    string
  >
}): PrimaryAlaoppimisalue => ({
  $class: 'fi.oph.koski.schema.PrimaryAlaoppimisalue',
  ...o
})

export const isPrimaryAlaoppimisalue = (a: any): a is PrimaryAlaoppimisalue =>
  a?.$class === 'PrimaryAlaoppimisalue'
