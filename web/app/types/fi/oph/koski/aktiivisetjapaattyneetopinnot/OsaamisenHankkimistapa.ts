import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * OsaamisenHankkimistapa
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapa`
 */
export type OsaamisenHankkimistapa = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapa'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const OsaamisenHankkimistapa = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): OsaamisenHankkimistapa => ({
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapa',
  ...o
})

OsaamisenHankkimistapa.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapa' as const

export const isOsaamisenHankkimistapa = (a: any): a is OsaamisenHankkimistapa =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapa'
