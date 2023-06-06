import { OsaamisenHankkimistapa } from './OsaamisenHankkimistapa'

/**
 * OsaamisenHankkimistapajakso
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapajakso`
 */
export type OsaamisenHankkimistapajakso = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapajakso'
  alku: string
  loppu?: string
  osaamisenHankkimistapa: OsaamisenHankkimistapa
}

export const OsaamisenHankkimistapajakso = (o: {
  alku: string
  loppu?: string
  osaamisenHankkimistapa: OsaamisenHankkimistapa
}): OsaamisenHankkimistapajakso => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapajakso',
  ...o
})

OsaamisenHankkimistapajakso.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapajakso' as const

export const isOsaamisenHankkimistapajakso = (
  a: any
): a is OsaamisenHankkimistapajakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.OsaamisenHankkimistapajakso'
