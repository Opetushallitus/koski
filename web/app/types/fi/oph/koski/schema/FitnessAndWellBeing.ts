import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * FitnessAndWellBeing
 *
 * @see `fi.oph.koski.schema.FitnessAndWellBeing`
 */
export type FitnessAndWellBeing = {
  $class: 'fi.oph.koski.schema.FitnessAndWellBeing'
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'HAWB'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export const FitnessAndWellBeing = (
  o: {
    tunniste?: Koodistokoodiviite<'oppiaineetinternationalschool', 'HAWB'>
    taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  } = {}
): FitnessAndWellBeing => ({
  $class: 'fi.oph.koski.schema.FitnessAndWellBeing',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'HAWB',
    koodistoUri: 'oppiaineetinternationalschool'
  }),
  ...o
})

export const isFitnessAndWellBeing = (a: any): a is FitnessAndWellBeing =>
  a?.$class === 'FitnessAndWellBeing'
