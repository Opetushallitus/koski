import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PrimaryLapsiOppimisalue
 *
 * @see `fi.oph.koski.schema.PrimaryLapsiOppimisalue`
 */
export type PrimaryLapsiOppimisalue = {
  $class: 'fi.oph.koski.schema.PrimaryLapsiOppimisalue'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkilapsioppimisalue',
    string
  >
}

export const PrimaryLapsiOppimisalue = (o: {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkilapsioppimisalue',
    string
  >
}): PrimaryLapsiOppimisalue => ({
  $class: 'fi.oph.koski.schema.PrimaryLapsiOppimisalue',
  ...o
})

export const isPrimaryLapsiOppimisalue = (
  a: any
): a is PrimaryLapsiOppimisalue =>
  a?.$class === 'fi.oph.koski.schema.PrimaryLapsiOppimisalue'
