import { TutkinnonOsanOsaAlue } from './TutkinnonOsanOsaAlue'
import { LocalizedString } from '../schema/LocalizedString'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'

/**
 * TutkinnonOsa
 *
 * @see `fi.oph.koski.tutkinto.TutkinnonOsa`
 */
export type TutkinnonOsa = {
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsa'
  osaAlueet: Array<TutkinnonOsanOsaAlue>
  nimi: LocalizedString
  tunniste: Koodistokoodiviite
  yhteinen: boolean
  laajuus?: number
}

export const TutkinnonOsa = (o: {
  osaAlueet?: Array<TutkinnonOsanOsaAlue>
  nimi: LocalizedString
  tunniste: Koodistokoodiviite
  yhteinen: boolean
  laajuus?: number
}): TutkinnonOsa => ({
  $class: 'fi.oph.koski.tutkinto.TutkinnonOsa',
  osaAlueet: [],
  ...o
})

TutkinnonOsa.className = 'fi.oph.koski.tutkinto.TutkinnonOsa' as const

export const isTutkinnonOsa = (a: any): a is TutkinnonOsa =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkinnonOsa'
