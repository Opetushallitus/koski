import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { RakenneOsa } from './RakenneOsa'

/**
 * SuoritustapaJaRakenne
 *
 * @see `fi.oph.koski.tutkinto.SuoritustapaJaRakenne`
 */
export type SuoritustapaJaRakenne = {
  $class: 'fi.oph.koski.tutkinto.SuoritustapaJaRakenne'
  suoritustapa: Koodistokoodiviite
  rakenne?: RakenneOsa
}

export const SuoritustapaJaRakenne = (o: {
  suoritustapa: Koodistokoodiviite
  rakenne?: RakenneOsa
}): SuoritustapaJaRakenne => ({
  $class: 'fi.oph.koski.tutkinto.SuoritustapaJaRakenne',
  ...o
})

SuoritustapaJaRakenne.className =
  'fi.oph.koski.tutkinto.SuoritustapaJaRakenne' as const

export const isSuoritustapaJaRakenne = (a: any): a is SuoritustapaJaRakenne =>
  a?.$class === 'fi.oph.koski.tutkinto.SuoritustapaJaRakenne'
