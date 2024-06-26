import { LocalizedString } from '../schema/LocalizedString'
import { RakenneOsa } from './RakenneOsa'
import { TutkinnonOsanLaajuus } from './TutkinnonOsanLaajuus'

/**
 * RakenneModuuli
 *
 * @see `fi.oph.koski.tutkinto.RakenneModuuli`
 */
export type RakenneModuuli = {
  $class: 'fi.oph.koski.tutkinto.RakenneModuuli'
  nimi: LocalizedString
  osat: Array<RakenneOsa>
  määrittelemätön: boolean
  laajuus?: TutkinnonOsanLaajuus
}

export const RakenneModuuli = (o: {
  nimi: LocalizedString
  osat?: Array<RakenneOsa>
  määrittelemätön: boolean
  laajuus?: TutkinnonOsanLaajuus
}): RakenneModuuli => ({
  $class: 'fi.oph.koski.tutkinto.RakenneModuuli',
  osat: [],
  ...o
})

RakenneModuuli.className = 'fi.oph.koski.tutkinto.RakenneModuuli' as const

export const isRakenneModuuli = (a: any): a is RakenneModuuli =>
  a?.$class === 'fi.oph.koski.tutkinto.RakenneModuuli'
