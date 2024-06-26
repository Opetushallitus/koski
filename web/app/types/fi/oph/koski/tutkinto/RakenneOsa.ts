import { RakenneModuuli, isRakenneModuuli } from './RakenneModuuli'
import { TutkinnonOsa, isTutkinnonOsa } from './TutkinnonOsa'

/**
 * RakenneOsa
 *
 * @see `fi.oph.koski.tutkinto.RakenneOsa`
 */
export type RakenneOsa = RakenneModuuli | TutkinnonOsa

export const isRakenneOsa = (a: any): a is RakenneOsa =>
  isRakenneModuuli(a) || isTutkinnonOsa(a)
