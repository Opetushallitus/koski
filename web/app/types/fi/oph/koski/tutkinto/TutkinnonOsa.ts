import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * TutkinnonOsa
 *
 * @see `fi.oph.koski.tutkinto.TutkinnonOsa`
 */
export type TutkinnonOsa = {
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsa'
  tunniste: Koodistokoodiviite
  nimi: LocalizedString
}

export const TutkinnonOsa = (o: {
  tunniste: Koodistokoodiviite
  nimi: LocalizedString
}): TutkinnonOsa => ({ $class: 'fi.oph.koski.tutkinto.TutkinnonOsa', ...o })

TutkinnonOsa.className = 'fi.oph.koski.tutkinto.TutkinnonOsa' as const

export const isTutkinnonOsa = (a: any): a is TutkinnonOsa =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkinnonOsa'
